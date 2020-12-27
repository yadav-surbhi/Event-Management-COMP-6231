package aspackage.clientServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import aspackage.OperationsApp.*;
import aspackage.beans.EventInformation;
import aspackage.utility.DataStructureAdapter;
import aspackage.utility.FileLogger;
import ipconfig.IPConfig;
import vspackage.bean.Header;
import vspackage.bean.Protocol;
import vspackage.server.MethodImpl;
import vspackage.tools.Adapter;

public class MTLServer {
	public static MTL exportedObj;

	public static void main(String args[]) {
		try {
			exportedObj = new MTL();
			System.out.println("MTL Server ready and waiting ...");
			ExecutorService executor = Executors.newCachedThreadPool();
			executor.execute(new Runnable() {

				@Override
				public void run() {
					listenUDP();
				}
			});

			executor.execute(new Runnable() {

				@Override
				public void run() {
					System.setProperty("java.net.preferIPv4Stack", "true");
					receiveUDPMessageulticast();
				}
			});
			
			executor.shutdown();

		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

	}

	private static String parseRequest(String input, DatagramPacket request) throws IOException {
		DataStructureAdapter ds = new DataStructureAdapter();
		String toReturn = input;
		if (!input.contains(";" + Util.Success) && !input.contains(";" + Util.Failure)) {
			String[] inputArray = input.split(Util.SEMI_COLON);
			switch (inputArray[0].trim()) {
			case Util.BOOK_EVENT:
				toReturn = exportedObj.bookEvent(inputArray[1].trim(), inputArray[2].trim(), inputArray[3].trim());
				break;
			case Util.BOOK_EVENT1:
				toReturn = exportedObj.bookEvent(inputArray[1].trim(), inputArray[2].trim(), inputArray[3].trim());
				break;
			case Util.CANCEL_EVENT:
				toReturn = exportedObj.cancelEvent(inputArray[1].trim(), inputArray[2].trim(), inputArray[3].trim());
				break;
			case Util.CANCEL_EVENT1:
				toReturn = exportedObj.cancelEvent(inputArray[1].trim(), inputArray[2].trim(), inputArray[3].trim());
				break;
			case Util.Get_Booking_Schedule:
				toReturn = exportedObj.udpCallforGetSchedule(inputArray[1].trim());
				break;
			case Util.Get_Booking_Schedule1:
				toReturn = exportedObj.getBookingSchedule(inputArray[1].trim());

				break;
			case Util.List_Event_Availability:
				toReturn = getfreeEvents(inputArray[1].trim());
				break;
			case Util.List_Event_Availability1:
				toReturn = exportedObj.listEventAvailability(inputArray[1].trim());
				break;
			case Util.Booking_Exist:
				toReturn = exportedObj.booking_exist(inputArray[3].trim(), inputArray[1].trim(), inputArray[2].trim());
				break;
			case Util.Capasity_Exist:
				toReturn = exportedObj.capasity_exist(inputArray[1].trim(), inputArray[2].trim());
				break;

			case Util.Can_Book:
				toReturn = exportedObj.get_Customerbook(inputArray[1].trim(), inputArray[2].trim());
				break;

			case Util.RE:
				toReturn = "success";
				exportedObj.removeFromCustBook(inputArray[1].trim(), inputArray[2].trim());
				break;

			case Util.REM_EVENT:
				toReturn = exportedObj.removeEvent(inputArray[1].trim(), inputArray[2].trim());
				break;
			case Util.ADD_EVENT:
				toReturn = exportedObj.addEvent(inputArray[1].trim(), inputArray[2].trim(),
						Integer.parseInt(inputArray[3].trim()));
				break;
			case Util.Swap_event:
				toReturn = exportedObj.swapEvent(inputArray[1].trim(), inputArray[2].trim(), inputArray[3].trim(),
						inputArray[4].trim(), inputArray[5].trim());
				break;

			case Util.SYNC:
				Header data = null;
				Gson gson = new Gson();
				String content = new String(request.getData());
				data = gson.fromJson(content, Header.class);
				exportedObj.customerBook = ds
						.convertCustomerMap((HashMap<String, HashMap<String, List<String>>>) data.getEventCus());
				exportedObj.eventBook = ds.convertEventMap(data.getEventMap());
				toReturn = "success";
				break;

			case Util.SYNC_REQUEST:
				Map<String, HashMap<String, Integer>> eventMap = ds
						.convertEventMapToHeaderFormat(exportedObj.eventBook);
				Map<String, HashMap<String, List<String>>> eventCus = ds
						.convertCustomerMapToHeaderFormat(exportedObj.customerBook);
				vspackage.server.Util.unicastOneWay(request.getAddress().getHostAddress(), request.getPort(),
						new Header(Protocol.SYNC, eventMap, eventCus));
				break;
			default:
				break;

			}
		}
		return toReturn;

	}

	private static String getfreeEvents(String inputArray) {
		ArrayList<HashMap<String, EventInformation>> templist = new ArrayList<HashMap<String, EventInformation>>();
		ArrayList<String> listofEvents = new ArrayList<String>();
		HashMap<String, EventInformation> tempmap = new HashMap<String, EventInformation>();
		if (exportedObj.eventBook.containsKey(inputArray)) {
			templist = exportedObj.eventBook.get(inputArray);
			for (HashMap<String, EventInformation> m : templist) {
				for (String s : m.keySet()) {
					// if(m.get(s).getCapasity()!=0) {
					listofEvents.add(s + " " + m.get(s).getCapasity());
					// }
				}

			}
		}
		if (listofEvents.isEmpty()) {
			listofEvents.add("No events available in " + Util.MTLCITY + " city.");
		}
		return listofEvents.toString();
	}

	private static void listenUDP() {
		DatagramSocket aSocketTOR = null;
		String requestMsg = "";
		try {
			aSocketTOR = new DatagramSocket(2002);
			System.out.println("UDP MTL Server:");
			byte[] buffer = new byte[Util.BUFFER_SIZE];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocketTOR.receive(request);

				System.out.println("Request received on MTL Server: " + new String((request.getData())));
				requestMsg = new String((request.getData()));

				if (requestMsg.contains("protocol_type")) {
					requestMsg = new String(Adapter.objectToString(request.getData()));
				}
				String replyStr = parseRequest(requestMsg, request).trim();
				System.out.println("Reply" + replyStr);
				buffer = new byte[Util.BUFFER_SIZE];
				byte[] replyBuff = replyStr.getBytes();
				DatagramPacket reply = null;
				if (requestMsg.contains(Util.BOOK_EVENT1) || requestMsg.contains(Util.Get_Booking_Schedule1)
						|| requestMsg.contains(Util.ADD_EVENT) || requestMsg.contains(Util.CANCEL_EVENT1)
						|| requestMsg.contains(Util.Swap_event) || requestMsg.contains(Util.REM_EVENT)
						|| requestMsg.contains(Util.List_Event_Availability1)) {
					reply = new DatagramPacket(replyBuff, replyStr.length(),
							InetAddress.getByName(IPConfig.getProperty("fe_addr")), 61001);
					aSocketTOR.send(reply);
				} else if (requestMsg.contains(Util.Booking_Exist) || requestMsg.contains(Util.Capasity_Exist)
						|| requestMsg.contains(Util.Can_Book) || requestMsg.contains(Util.RE)
						|| requestMsg.contains(Util.Get_Booking_Schedule)
						|| requestMsg.contains(Util.List_Event_Availability) || requestMsg.contains(Util.CANCEL_EVENT)
						|| requestMsg.contains(Util.BOOK_EVENT)) {
					reply = new DatagramPacket(replyBuff, replyStr.length(), request.getAddress(), request.getPort());
					aSocketTOR.send(reply);

				} else {
					// do nothing
				}

			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocketTOR != null)
				aSocketTOR.close();
		}

	}
	
	
	private static void receiveUDPMessageulticast() {
		DatagramSocket aSocketTOR = null;
		String requestMsg = "";
		try {
			byte[] buffer = new byte[Util.BUFFER_SIZE];
			MulticastSocket socket=new MulticastSocket(4321);
		    InetAddress group=InetAddress.getByName("239.0.0.0");
		    socket.joinGroup(group);
			while (true) {
				System.out.println("Waiting for multicast message...");
		         DatagramPacket packet=new DatagramPacket(buffer,
		 	            buffer.length);
		 	    socket.receive(packet);
				System.out.println("Request Received On Server: " + new String((packet.getData())));
				requestMsg = new String((packet.getData()));

				if (requestMsg.contains("protocol_type")) {
					requestMsg = new String(Adapter.objectToString(packet.getData()));
				}
				String replyStr = parseRequest(requestMsg, packet).trim();
				System.out.println("Reply:" + replyStr);
				buffer = new byte[Util.BUFFER_SIZE];
				byte[] replyBuff = replyStr.getBytes();
				DatagramPacket reply = null; 
				if (requestMsg.contains(Util.BOOK_EVENT1) || requestMsg.contains(Util.Get_Booking_Schedule1)
						|| requestMsg.contains(Util.ADD_EVENT) || requestMsg.contains(Util.CANCEL_EVENT1)
						|| requestMsg.contains(Util.Swap_event) || requestMsg.contains(Util.REM_EVENT)
						|| requestMsg.contains(Util.List_Event_Availability1)) {

					reply = new DatagramPacket(replyBuff, replyStr.length(),
							InetAddress.getByName(IPConfig.getProperty("fe_addr")), 61002);
					socket.send(reply);
				}
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
//			socket.close();
		}
	}
}
