package aspackage.clientServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.omg.CORBA.ORB;
import aspackage.OperationsApp.DEMSOperationsPOA;

import aspackage.beans.EventInformation;
import aspackage.utility.FileLogger;
import aspackage.utility.UtilConstants;

public class TOR extends DEMSOperationsPOA {
	private ORB orb;
	HashMap<String, ArrayList<HashMap<String, EventInformation>>> eventBook = new HashMap<String, ArrayList<HashMap<String, EventInformation>>>();
	HashMap<String, ArrayList<EventInformation>> customerBook = new HashMap<String, ArrayList<EventInformation>>();
	HashMap<String, String> eventCust = new HashMap<String, String>();
	BlockingQueue<HashMap<String, String>> msgQueue = new LinkedBlockingDeque<>();

	private Map<String, String> requestParameters;
	private String requestStatus = null;
	private String serverResponse = null;
	private EventInformation eventRec;

	protected TOR() {
		super();
		addEvent("TORM100100", "Seminar", 10);
		addEvent("TORM100100", "Trade Show", 10);
		addEvent("TORM100100", "Conference", 10);
		addEvent("TORM110100", "Trade Show", 10);
		
	}

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	public synchronized String udpSend(int port, String msg) {
		String replymsg = null;
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] message = msg.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, port);
			aSocket.send(request);// request sent out
			System.out.println("Request message sent from the client is : " + new String(request.getData()));
			String resp = new String(request.getData());
			byte[] buffer = new byte[Util.BUFFER_SIZE];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);// reply received and will populate reply packet now.
			System.out.println("Reply received from the server is: " + new String(reply.getData()));
			replymsg = new String(reply.getData());

			// logStatus(Util.UDPCALL, requestParameters, "UDP Status", replymsg);

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {

			if (aSocket != null)
				aSocket.close();// now all resources used by the socket are returned to the OS, so that there is
								// no
			// resource leakage, therefore, close the socket after it's use is completed to
			// release resources.
		}
		return replymsg;

	}

	@Override
	public synchronized String addEvent(String eventId, String eventType, int bookingCapacity) {
		try {
			serverResponse = new String();
			requestParameters = new HashMap<String, String>();
			requestParameters.put("eventId", eventId);
			requestParameters.put("eventType", eventType);
			requestParameters.put("bookingCapacity", String.valueOf(bookingCapacity));
			
			String tmp= eventId.substring(0, 3);
			if(tmp.equalsIgnoreCase("TOR")) {
				eventRec = new EventInformation();
				// check if event exists
				ArrayList<HashMap<String, EventInformation>> templist = new ArrayList<HashMap<String, EventInformation>>();
				HashMap<String, EventInformation> tempmap = new HashMap<String, EventInformation>();
				boolean ispresent = false;
				if (eventBook.containsKey(eventType)) {
					templist = eventBook.get(eventType);
					for (HashMap<String, EventInformation> m : templist) {
						if (m.containsKey(eventId)) {
							tempmap = m;
							eventRec = m.get(eventId);
							ispresent = true;
						}
					}
				}
				if (ispresent) {
					templist.remove(tempmap);
				}

				// if (eventRec == null) {
				eventRec.setEventId(eventId);
				eventRec.setEventType(eventType);
				eventRec.setEventTime(Character.toString(eventId.charAt(3)));
				eventRec.setEventlocation(eventId.substring(0, 3));
				eventRec.setEventDate(eventId.substring(4, 10)); // need to change

				// }
				eventRec.setCapasity(bookingCapacity);// validation rules for capacity needs to be implemented @@ surbhi
				tempmap.put(eventId, eventRec);
				templist.add(tempmap);
				eventBook.put(eventType, templist);
				if (ispresent) {
					serverResponse = "Existing event updated.";
				} else {
					serverResponse = "New event added.";
				}
				requestStatus = Util.Success;	
			}
			else {
				requestStatus=Util.Failure;
				serverResponse = "Customer can add events in Toronto city only. Try Again.";
			}

			
		} catch (Exception e) {
			requestStatus = Util.Failure;
			serverResponse = "Request was not completed. Error Message :" + e.getMessage();
		} finally {
			logStatus("addEvent", requestParameters, requestStatus, serverResponse);

		}
		return serverResponse+Util.SEMI_COLON+requestStatus.trim();

	}
	
	public synchronized boolean can_book(String customerId,String date) {
		boolean toReturn =false;
		String resp ="0",resp2="0";
		String cust = customerId.substring(0, 3);
		if(Util.MTL.equalsIgnoreCase(cust)) {
			resp = udpSend(2003, Util.Can_Book + Util.SEMI_COLON + customerId + Util.SEMI_COLON
					+ date).trim();
		}else {
			resp = udpSend(2002, Util.Can_Book + Util.SEMI_COLON + customerId + Util.SEMI_COLON
					+ date).trim();
		}
		
		resp2 = get_Customerbook(customerId,date);
		
		int totalCount = Integer.parseInt(resp)+Integer.parseInt(resp2);
		
		if(totalCount>=3) {
			toReturn=true;
		}
		return toReturn;
	}
	
	public synchronized String get_Customerbook(String customerId,String mnthYr) {
		int counter =0;
		ArrayList<EventInformation> custbook = new ArrayList<EventInformation>();
		if(customerBook.containsKey(customerId)) {
			custbook= customerBook.get(customerId);
		}
		if(!custbook.isEmpty()) {
			for(EventInformation ev:custbook) {
				if(ev.getEventId().substring(6, 10).equals(mnthYr)) {
					counter++;
				}
			}
		}
		return String.valueOf(counter);
		
	}
	

	@Override
	public synchronized String bookEvent(String customerId, String eventId, String eventType) {

		serverResponse = new String();
		requestParameters = new HashMap<String, String>();
		requestParameters.put("eventId", eventId);
		requestParameters.put("eventType", eventType);
		requestParameters.put("customerId", String.valueOf(customerId));

		String eventLoc = eventId.substring(0, 3);
		String cust = customerId.substring(0, 3);

		if (!Util.TOR.equalsIgnoreCase(eventLoc)) {
			switch (eventLoc) {
			case Util.MTL:
				System.out.println(Util.BOOK_EVENT + Util.SEMI_COLON + customerId + Util.SEMI_COLON + eventId
						+ Util.SEMI_COLON + eventType);
				serverResponse = udpSend(2002, Util.BOOK_EVENT + Util.SEMI_COLON + customerId + Util.SEMI_COLON
						+ eventId + Util.SEMI_COLON + eventType).trim();
				break;
			case Util.OTW:
				serverResponse = udpSend(2003, Util.BOOK_EVENT + Util.SEMI_COLON + customerId + Util.SEMI_COLON
						+ eventId + Util.SEMI_COLON + eventType).trim();
			}

		} else {
			try {
				int eventSize = 0;
				boolean validateMonth = false;
				if (!cust.equalsIgnoreCase(eventLoc)) {
					if (customerBook.containsKey(customerId)) {
						validateMonth = can_book(customerId,eventId.substring(6, 10));
					}
				}

//				if (eventSize > 3) {
//					
//				}
				boolean alreadyBooked = false;
				if (customerBook.containsKey(customerId)) {
					ArrayList<EventInformation> evnt = new ArrayList<EventInformation>();
					evnt = customerBook.get(customerId);
					for (EventInformation evt : evnt) {
						if (evt.getEventId().equalsIgnoreCase(eventId)
								&& evt.getEventType().equalsIgnoreCase(eventType)) {
							alreadyBooked = true;
						}

					}
				}
				eventRec = new EventInformation();
				ArrayList<EventInformation> eventInfo;
				ArrayList<HashMap<String, EventInformation>> templist = new ArrayList<HashMap<String, EventInformation>>();
				HashMap<String, EventInformation> tempmap = new HashMap<String, EventInformation>();
				
				boolean ispresent = false;
				boolean isAllBooked = true;
				if (!eventBook.isEmpty()) {
					if (eventBook.containsKey(eventType)) {
						templist = eventBook.get(eventType);
						for (HashMap<String, EventInformation> m : templist) {
							tempmap = m;
							if (m.containsKey(eventId)) {
								int cap = m.get(eventId).getCapasity();
								if (cap != 0) {
									eventRec = m.get(eventId);
									// templist.remove(m);
									isAllBooked = false;
								}
								ispresent = true;
							}
						}
					}
				}

				if (!isAllBooked && !alreadyBooked && !validateMonth) {
					if (customerBook.containsKey(customerId)) {
						customerBook.get(customerId).add(eventRec);
					} else {
						eventInfo = new ArrayList<EventInformation>();
						eventInfo.add(eventRec);
						customerBook.put(customerId, eventInfo);
					}
//					templist.remove(tempmap); 
					int cap = eventRec.getCapasity() - 1;
					eventRec.setCapasity(cap);
//					tempmap.put(eventId, eventRec);
//					templist.add(tempmap);
					eventBook.put(eventType, templist);
					eventCust.put(eventRec.getEventId()+Util.SEMI_COLON+eventRec.getEventType(), customerId);
					serverResponse = Util.Success+" Event is booked successfully. To check booking schedule later select option '2' from the menu."
							+ "Remaining Capasity -" + eventRec.getCapasity();
					requestStatus = Util.Success;
				} else {
					serverResponse = Util.Failure+" Event cannot be booked. Either the capacity is full or User already has a entry.";
					requestStatus = Util.Failure;
				}

				if (!ispresent) {
					serverResponse = Util.Failure+"Event does not exist. Ensure you have correct event id.";
					requestStatus = Util.Failure;
				}

				if (validateMonth) {
					serverResponse = Util.Failure+"Event cannot be booked. Max month limit.";
					requestStatus = Util.Failure;
				}
				
			} catch (Exception e) {
				requestStatus = Util.Failure;
				serverResponse = Util.Failure+"Request was not completed. Error Message :" + e;
			} finally {
				logStatus(Util.BOOK_EVENT, requestParameters, requestStatus, serverResponse);
			}
		}
		return (serverResponse).trim();
	}

	@Override
	public synchronized String getBookingSchedule(String customerId) {
		serverResponse = new String();
		serverResponse = "You have no event in TOR";
		String[] response = new String[4];
		String resp = "";
		String resp1 = "";
		String resp2 = "";
		try {
			if (customerBook.containsKey(customerId)) {
				resp = "Events in Toronto" + "\n";
				ArrayList<EventInformation> temp = customerBook.get(customerId);
				for (EventInformation rec : customerBook.get(customerId)) {
					System.out.println("Added event record: " + rec.getEventId());
					resp = resp + "Event ID " + rec.getEventId() + " Event Type " + rec.getEventType() + " "
							+ " EVENT Date " + rec.getEventDate() + " EVENT Time " + rec.getEventTime() + "\n";
				}
				response[0] = resp;
			}
			try {
				resp1 = udpSend(2002, Util.Get_Booking_Schedule + Util.SEMI_COLON + customerId);
				response[1] = resp1;
			} catch (Exception e) {
				response[1] = "Could not get events Details for MTL";
			}
			try {
				resp2 = udpSend(2003, Util.Get_Booking_Schedule + Util.SEMI_COLON + customerId);
				response[2] = resp2;
			} catch (Exception e) {
				response[3] = "Could not get events Details for OTW";
			}
			if(response[0]==null) {
				response[0]=serverResponse;
			}
			serverResponse = response[0] + "\n" + response[1].trim() + "\n" + response[2].trim();
			requestStatus =Util.Success;
		} catch (Exception e) {
			requestStatus = Util.Failure;
			serverResponse = "Request was not completed. Error Message :" + e.getMessage();
			response[3] = serverResponse;

		} finally {

			requestParameters = new HashMap<String, String>();
			requestParameters.put("customerId", String.valueOf(customerId));
			logStatus(Util.Get_Booking_Schedule, requestParameters, requestStatus, serverResponse);
		}
		return (serverResponse+Util.SEMI_COLON+requestStatus).trim();
	}

	@Override
	public synchronized String cancelEvent(String customerId, String eventId, String eventType) {
		serverResponse = "It entered but nothing happened";
		String eventLoc = eventId.substring(0, 3);
		if (!Util.TOR.equalsIgnoreCase(eventLoc)) {
			switch (eventLoc) {
			case Util.MTL:
				System.out.println(Util.CANCEL_EVENT + Util.SEMI_COLON + customerId + Util.SEMI_COLON + eventId
						+ Util.SEMI_COLON + eventType);
				serverResponse = udpSend(2002, Util.CANCEL_EVENT + Util.SEMI_COLON + customerId + Util.SEMI_COLON
						+ eventId + Util.SEMI_COLON + eventType).trim();
				break;
			case Util.OTW:
				serverResponse = udpSend(2003, Util.CANCEL_EVENT + Util.SEMI_COLON + customerId + Util.SEMI_COLON
						+ eventId + Util.SEMI_COLON + eventType).trim();
			}

		} else {
			try {
				boolean isRemoved = false;
				ArrayList<EventInformation> to = new ArrayList<EventInformation>();
				ArrayList<EventInformation> from = new ArrayList<EventInformation>();
				if (customerBook.containsKey(customerId)) {
					from = customerBook.get(customerId);
					for (EventInformation rec : customerBook.get(customerId)) {
						if (rec.getEventId().equalsIgnoreCase(eventId)
								&& rec.getEventType().equalsIgnoreCase(eventType)) {
							to.add(rec);
							isRemoved = true;
						}
					}
				}

				ArrayList<HashMap<String, EventInformation>> templist = new ArrayList<HashMap<String, EventInformation>>();
				HashMap<String, EventInformation> tempmap = new HashMap<String, EventInformation>();

				if (isRemoved) {
					from.removeAll(to);
					templist = eventBook.get(eventType);
					for (HashMap<String, EventInformation> m : templist) {
						if (m.containsKey(eventId)) {
							tempmap = m;
							eventRec = m.get(eventId);

						}
					}
					templist.remove(tempmap);
					int cap = eventRec.getCapasity();
					eventRec.setCapasity(cap + 1);
					tempmap.put(eventId, eventRec);
					templist.add(tempmap);
					eventBook.put(eventType, templist);
					customerBook.put(customerId, from);
					eventCust.remove(eventId+Util.SEMI_COLON+eventType);
					serverResponse = Util.Success+" Booking Cancelled successfully.";
					requestStatus = Util.Success;
				} else {
					serverResponse =  Util.Failure+" Booking does not exist";
					requestStatus = Util.Failure;
				}
				
			} catch (Exception e) {
				requestStatus = Util.Failure;
			} finally {
				requestParameters = new HashMap<String, String>();
				requestParameters.put("eventId", eventId);
				requestParameters.put("eventType", eventType);
				requestParameters.put("customerId", String.valueOf(customerId));
				logStatus(Util.CANCEL_EVENT, requestParameters, requestStatus, serverResponse);
			}
		}
		return (serverResponse).trim();
	}

	@Override
	public synchronized String removeEvent(String eventId, String eventType) {

		ArrayList<HashMap<String, EventInformation>> templist = new ArrayList<HashMap<String, EventInformation>>();
		HashMap<String, EventInformation> tempmap = new HashMap<String, EventInformation>();

		try {
			boolean ispresent = false;
			if (eventBook.containsKey(eventType)) {
				templist = eventBook.get(eventType);
				for (HashMap<String, EventInformation> m : templist) {
					if (m.containsKey(eventId)) {
						removeFromCustBook(eventId,eventType);
						udpSend(2003, Util.RE + Util.SEMI_COLON
								+ eventId + Util.SEMI_COLON + eventType);
						udpSend(2002, Util.RE + Util.SEMI_COLON
								+ eventId + Util.SEMI_COLON + eventType);
						templist.remove(m);
						ispresent = true;
						break;
					}
				}
				eventBook.put(eventType, templist);
			}

			if (ispresent) {
//				removeFromCustBook(eventId,eventType);
//				udpSend(2003, Util.RE + Util.SEMI_COLON
//						+ eventId + Util.SEMI_COLON + eventType);
//				udpSend(2002, Util.RE + Util.SEMI_COLON
//						+ eventId + Util.SEMI_COLON + eventType);
				
				serverResponse = "Event removed successfully";
				requestStatus = Util.Success;
			} else {
				serverResponse = "Event was not available.";
				requestStatus = Util.Failure;
			}
			
		} catch (Exception e) {
			requestStatus =Util.Failure;
			serverResponse = "Request was not completed. Error Message :" + e.getMessage();
		} finally {
			requestParameters = new HashMap<String, String>();
			requestParameters.put("eventId", eventId);
			requestParameters.put("eventType", eventType);
			logStatus("removeEvent", requestParameters, requestStatus, serverResponse);
		}
		return serverResponse+Util.SEMI_COLON+requestStatus;
	}
	//eventCust
	public synchronized void removeFromCustBook(String eventId,String eventType) {
		if(eventCust.containsKey(eventId+Util.SEMI_COLON+eventType)) {
			boolean isRemoved = false;
			ArrayList<EventInformation> to = new ArrayList<EventInformation>();
			ArrayList<EventInformation> from = new ArrayList<EventInformation>();
			String customerid =eventCust.get(eventId+Util.SEMI_COLON+eventType);
			if (customerBook.containsKey(customerid)){
				from = customerBook.get(customerid);
				for (EventInformation rec : customerBook.get(customerid)) {
					if (rec.getEventId().equalsIgnoreCase(eventId)
							&& rec.getEventType().equalsIgnoreCase(eventType)) {
						to.add(rec);
						isRemoved = true;
					}
				}
			}
			if (isRemoved) {
				from.removeAll(to);
				if(from.isEmpty()) {
					customerBook.remove(customerid);
					eventCust.remove(eventId+Util.SEMI_COLON+eventType);
					
				}else {
					customerBook.put(customerid, from);
				}
			}
//			cancelEvent(eventCust.get(eventId+Util.SEMI_COLON+eventType), eventId, eventType);
		}
	}

	@Override
	public synchronized String listEventAvailability(String eventType) {
		ArrayList<String> listofEvents = new ArrayList<String>();
		String[] response = new String[3];
		try {
			response[0] = udpSend(2002, Util.List_Event_Availability + Util.SEMI_COLON + eventType).trim();
		} catch (Exception e) {
			response[0] = "MTL Error";
		}
		try {
			response[1] = udpSend(2003, Util.List_Event_Availability + Util.SEMI_COLON + eventType).trim();
		} catch (Exception e) {
			response[1] = "OTW Error";
		}
		try {
			ArrayList<HashMap<String, EventInformation>> templist = new ArrayList<HashMap<String, EventInformation>>();
			HashMap<String, EventInformation> tempmap = new HashMap<String, EventInformation>();
			if (eventBook.containsKey(eventType)) {
				templist = eventBook.get(eventType);
				for (HashMap<String, EventInformation> m : templist) {
					for (String s : m.keySet()) {
//						if (m.get(s).getCapasity() != 0) {
							listofEvents.add(s + " " + m.get(s).getCapasity());
//						}
					}

				}
			}
			if(listofEvents.isEmpty()) {
				listofEvents.add("No events available in "+Util.TORCITY+" city.");
			}
			response[2] = listofEvents.toString();
			serverResponse = response[2] + " " + response[0] + " " + response[1];
			requestStatus=Util.Success;

		} catch (Exception e) {
			requestStatus=Util.Failure;

		} finally {
			requestParameters = new HashMap<String, String>();
			requestParameters.put("eventType", eventType);
			logStatus("removeEvent", requestParameters, requestStatus, serverResponse);
		}
		return serverResponse+Util.SEMI_COLON+requestStatus;
	}

	public synchronized String udpCallforGetSchedule(String customerId) {
		String serverResponse1 = new String();
		serverResponse1 = "You have no event in TOR";
		ArrayList<String> listofEvents = new ArrayList<String>();
		if (customerBook.containsKey(customerId)) {
			serverResponse1 = "Events in TOR " + "\n";
			ArrayList<EventInformation> temp = customerBook.get(customerId);
			for (EventInformation rec : customerBook.get(customerId)) {
				serverResponse1 = serverResponse1 + "Event ID " + rec.getEventId() + " Event Type " + rec.getEventType()
						+ " " + " EVENT Date " + rec.getEventDate() + " EVENT Time " + rec.getEventTime() + "\n";
			}
		}
		requestParameters = new HashMap<String, String>();
		requestParameters.put("customerId", customerId);
		logStatus("UDP Call: " + Util.Get_Booking_Schedule, requestParameters, "Sucess", serverResponse1);
		return serverResponse1;

	}

	@Override
	public synchronized String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID,
			String oldEventType) {
		String message1 = "", message2 = "";
		try {
		String bookingExist = booking_exist(customerID, oldEventID, oldEventType);
		String canBook = capasity_exist(newEventID, newEventType);
		if (bookingExist.equalsIgnoreCase("T") && canBook.equalsIgnoreCase("T")) {
			message1 = cancelEvent(customerID, oldEventID, oldEventType);
			if (message1.trim().contains(Util.Success)) {
				message2 = bookEvent(customerID, newEventID, newEventType);
				if (!message2.trim().contains(Util.Success)) {
					bookEvent(customerID, oldEventID, oldEventType);
					message2 = "Swap Failure.";
					requestStatus=Util.Failure;
				} else {
					message2 = "Swap Success";
					requestStatus = Util.Success;
				}

			}else{
				message2 = "Swap Failure.";
				requestStatus=Util.Failure;
			};

		}else {
			message2 = "Swap Failure.";
			requestStatus=Util.Failure;
		}
		
		}catch(Exception e) {
			requestStatus=Util.Failure;
			message2 = "Swap Failure";
		}finally {
			requestParameters = new HashMap<String, String>();
			requestParameters.put("new eventId", newEventID);
			requestParameters.put("new eventType", newEventType);
			requestParameters.put("old eventId", oldEventID);
			requestParameters.put("old eventType", oldEventID);
			requestParameters.put("customerId", customerID);
			logStatus(Util.Swap_event, requestParameters, requestStatus, message2);
		}
		return message2.trim()+Util.SEMI_COLON+requestStatus;
	}

	public synchronized String booking_exist(String custID, String eventID, String eventType) {
		String eventLoc = eventID.substring(0, 3);
		String toReturn = "F";
		if (!Util.TOR.equalsIgnoreCase(eventLoc)) {
			switch (eventLoc) {
			case Util.MTL:
				toReturn = udpSend(2002, Util.Booking_Exist + Util.SEMI_COLON + eventID + Util.SEMI_COLON + eventType
						+ Util.SEMI_COLON + custID);
				break;
			case Util.OTW:
				toReturn = udpSend(2003, Util.Booking_Exist + Util.SEMI_COLON + eventID + Util.SEMI_COLON + eventType
						+ Util.SEMI_COLON + custID);
			}

		} else {
			ArrayList<EventInformation> temp = new ArrayList<EventInformation>();
			temp = customerBook.containsKey(custID) ? customerBook.get(custID) : null;
			if (temp != null) {

				for (EventInformation rec : temp) {
					if (rec.getEventType().equalsIgnoreCase(eventType) && rec.getEventId().equalsIgnoreCase(eventID)) {
						toReturn = "T";
						break;
					}

				}
			}
		}

		return toReturn.trim();

	}

	public synchronized String capasity_exist(String eventID, String eventType) {
		String eventLoc = eventID.substring(0, 3);
		String toReturn = "F";
		if (!Util.TOR.equalsIgnoreCase(eventLoc)) {
			switch (eventLoc) {
			case Util.MTL:
				toReturn = udpSend(2002, Util.Capasity_Exist + Util.SEMI_COLON + eventID + Util.SEMI_COLON + eventType);
				break;
			case Util.OTW:
				toReturn = udpSend(2003, Util.Capasity_Exist + Util.SEMI_COLON + eventID + Util.SEMI_COLON + eventType);
			}

		} else {
			ArrayList<HashMap<String, EventInformation>> temp = new ArrayList<HashMap<String, EventInformation>>();
			temp = eventBook.containsKey(eventType) ? eventBook.get(eventType) : null;
			if (temp != null) {

				for (HashMap<String, EventInformation> map : temp) {
					if (map.containsKey(eventID)) {
						if (map.get(eventID).getCapasity() > 0) {
							toReturn = "T".trim();
						}
					}

				}
			}
		}
		return toReturn.trim();

	}

	private synchronized void logStatus(String requestType, Map<String, String> requestParameters, String requestStatus,
			String serverResonse) {
		FileLogger log = new FileLogger("src/aspackage/logFiles/TOR.txt", requestType, requestParameters, requestStatus,
				serverResonse);
		log.writeFiles();
	}

	@Override
	public void shutdown() {
		orb.shutdown(false);

	}
}