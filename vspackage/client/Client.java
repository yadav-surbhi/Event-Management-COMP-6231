package vspackage.client;

import java.io.IOException;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FEApp.FEMethodHelper;
import vspackage.RemoteMethodApp.RemoteMethodHelper;
import vspackage.bean.Protocol;
import vspackage.tools.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class Client.
 */
public class Client {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		boolean invalidID = true;
		String id = null;
		while(invalidID) {
			System.out.print("Enter your ID:");
			Scanner scan = new Scanner(System.in);
			id = scan.nextLine();
			invalidID = validateID(id);
		}
		
		//get city code from id
		id = id.toUpperCase();
		String cityCode = id.substring(0, 3);
		//get client type from id
		char type = id.charAt(3);
			
		while(true) {
			showOperationMenu(type);
			System.out.print("Enter a number to choose an operation:");
			Scanner scan = new Scanner(System.in);
			int option = 0;
			while(true) {
				try { 
					option = Integer.parseInt(scan.nextLine());
					if (type == 'C'){
						if(option > 4){
							throw new NumberFormatException();
						}
					}else if (type == 'M') {
						if(option >=1 && option <=4 ) {
							boolean notValidID = true;
							String cid ="";
							System.out.print("Enter customer ID:");
							while(notValidID) {
								cid = scan.nextLine();
								notValidID = validateID(id);
								if(!cid.substring(0, 3).equalsIgnoreCase(cityCode)) {
									notValidID = true;
									System.out.print("Customer has to be in the same city. Retry:");
								}else {
									id = cid;
								}
							}
						}
					}
					break;
				}catch( NumberFormatException e) {
					System.out.print("ERROR: Incorret input. Enter a number:");
				}
			}
			 
			String hostname = "";
			int port = 0;
			
			//create a skeleton of the client to register on server side
			
//			if(cityCode.equalsIgnoreCase("TOR")) {
//
//				hostname = "TOR";
////				connectServer(hostname, args, option, id);
//				connectServer(hostname, option, id);
//			} else if(cityCode.equalsIgnoreCase("MTL")) {
//
//				hostname = "MTL";
////				connectServer(hostname, args, option, id);
//				connectServer(hostname, option, id);
//			} else if(cityCode.equalsIgnoreCase("OTW")) {
//
//				hostname = "OTW";
////				connectServer(hostname, args, option, id);
//				connectServer(hostname, option, id);
//			}
			
			if(cityCode.equalsIgnoreCase(" ")) {
				
				hostname = "TOR";
				
			} else if(cityCode.equalsIgnoreCase("MTL")) {
				
				hostname = "MTL";
				
			} else if(cityCode.equalsIgnoreCase("OTW")) {
				
				hostname = "OTW";
			}
			
			connectFEServer(option, id);
		}
		
	}

	/**
	 * Show city menu.
	 */
	private static void showCityMenu() {
		System.out.println("Below are 3 cities where an event can take place:");
		System.out.println("-- Toronto (TOR)");
		System.out.println("-- Montreal (MTL)");
		System.out.println("-- Ottawa (OTW)");
		
	}

	/**
	 * Show event menu.
	 */
	private static void showEventTypeMenu() {
		System.out.println("Below are event types:");
		System.out.println("-- Conference");
		System.out.println("-- Tradeshow");
		System.out.println("-- Seminar");
		
	}

	/**
	 * Connect FE server CORBA.
	 *
	 * @param hostName the host name (default is localhost)
	 * @param port the server port
	 * @param option type of operation
	 * @param cusID the customer ID
	 */
	private static void connectFEServer(int option, String cusID) {
		//create a logger for this client
		Logger logger = new Logger(cusID, true);
		try {
			ORB orb = ORB.init(new String[] {null}, null);
			//-ORBInitialPort 1050 -ORBInitialHost localhost
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			FEApp.FEMethod h = (FEApp.FEMethod) FEMethodHelper.narrow(ncRef.resolve_str("FE"));
	         logger.log(2, "Lookup completed");
	         System.out.println("Lookup completed " );
	         
	         Scanner scan = new Scanner(System.in);
	         // invoke the remote method
	         if(option == Protocol.BOOK_EVENT) { //book an event
	        	String eventType = inputEventType();
	     		String eventID = inputEventID();
	     		logger.log(2, "booking an event for " + eventType + eventID);
	        	String status = h.bookEvent(cusID, eventID, eventType);
	        	System.out.println(status);
	        	logger.log(2, status);
	         } else if(option == Protocol.GET_SCHEDULE_EVENT) { //get booking event
	        	 String eventList = h.getBookingSchedule(cusID);
	        	 System.out.println("\n" +eventList);
	        	 logger.log(2, "getting booked event");
	        	 
	        	 logger.log(2, eventList);
	        	 
	         
	         } else if(option == Protocol.CANCEL_EVENT) { //cancel event
	        	 String eventID = inputEventID();
	        	 String eventType = inputEventType();
	        	 String status = h.cancelEvent(cusID, eventID, eventType);
	        	 logger.log(2, "canceling event " + eventID);
	        	 System.out.println(status);
	        	 logger.log(2, status);
	         
	         } else if(option == Protocol.ADD_EVENT) { //add new event
	        	 logger.log(2, "adding an event");
	        	 Object[] results = createEvent();
	        	 String cusCity = cusID.substring(0, 3);
	        	 String eventCity = ((String)results[1]).substring(0,3);
	        	 String status = "";
	        	 if(cusCity.equalsIgnoreCase(eventCity)) {
		        	 status = h.addEvent((String)results[1], (String)results[0], (Integer)results[2]);

	        	 }else {
	        		 status=  "Enter correct event id";
	        	 }
	        	 System.out.println(status);
	        	 logger.log(2, status);
	         
	         } else if(option == Protocol.REMOVE_EVENT) { //remove an event
	        	 String eventID = inputEventID();
	        	 String eventType = inputEventType();
	        	 String status = h.removeEvent(eventID, eventType);
	        	 logger.log(2, "removing an event " + eventType + " " + eventID);
	        	 System.out.println(status);
	        	 logger.log(2, status);
	        	 
	         } else if(option == Protocol.EVENT_AVAILABLITY) { //list event availability
	        	 logger.log(2, "listing event availability");
	        	 String eventType = inputEventType();
        	 	String result = h.listEventAvailability(cusID, eventType);
        	 	System.out.println("\n"+result);
        	 	logger.log(2, result);
	         } else if(option == Protocol.SWAP_EVENT) {//swap event
	        	 String oldEventType = inputOldEventType();
	        	 String oldEventID = inputOldEventID();
	        	 String newEventType = inputNewEventType();
	        	 String newEventID = inputNewEventID();
	        	 String status = h.swapEvent(cusID, newEventID, newEventType, oldEventID, oldEventType);
	        	 logger.log(2, "swapping events ");
	        	 System.out.println(status);
	        	 logger.log(2, status);
	         }
	         
	         
	      }
	      catch (Exception e) {
	         System.out.println("Exception in Client: " + e);
	         e.printStackTrace();
	         
	         try {
				logger.log(0, "Exception in Client: " + e.getMessage());
			} catch (IOException e1) {
				System.out.println("Error while trying to log in Client");
				e1.printStackTrace();
			}
	      } 
		
	}
		
	/**
	 * Connect server CORBA.
	 *
	 * @param hostName the host name (default is localhost)
	 * @param port the server port
	 * @param option type of operation
	 * @param cusID the customer ID
	 */
	private static void connectServer(String hostName, int option, String cusID) {
		//create a logger for this client
		Logger logger = new Logger(cusID, true);
		try {
			ORB orb = ORB.init(new String[] {null}, null);
			//-ORBInitialPort 1050 -ORBInitialHost localhost
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			vspackage.RemoteMethodApp.RemoteMethod h = (vspackage.RemoteMethodApp.RemoteMethod) RemoteMethodHelper.narrow(ncRef.resolve_str(hostName));
	         logger.log(2, "Lookup completed");
	         System.out.println("Lookup completed " );
	         
	         Scanner scan = new Scanner(System.in);
	         // invoke the remote method
	         if(option == Protocol.BOOK_EVENT) { //book an event
	        	String eventType = inputEventType();
	     		String eventID = inputEventID();
	     		logger.log(2, "booking an event for " + eventType + eventID);
	        	String status = h.bookEvent(cusID, eventID, eventType);
	        	System.out.println(status);
	        	logger.log(2, status);
	         } else if(option == Protocol.GET_SCHEDULE_EVENT) { //get booking event
	        	 String eventList = h.getBookingSchedule(cusID);
	        	 System.out.println("\n" +eventList);
	        	 logger.log(2, "getting booked event");
	        	 
	        	 logger.log(2, eventList);
	        	 
	         
	         } else if(option == Protocol.CANCEL_EVENT) { //cancel event
	        	 String eventID = inputEventID();
	        	 String eventType = inputEventType();
	        	 String status = h.cancelEvent(cusID, eventID, eventType);
	        	 logger.log(2, "canceling event " + eventID);
	        	 System.out.println(status);
	        	 logger.log(2, status);
	         
	         } else if(option == Protocol.ADD_EVENT) { //add new event
	        	 logger.log(2, "adding an event");
	        	 Object[] results = createEvent();
	        	 String cusCity = cusID.substring(0, 3);
	        	 String eventCity = ((String)results[1]).substring(0,3);
	        	 String status = "";
	        	 if(cusCity.equalsIgnoreCase(eventCity)) {
		        	 status = h.addEvent((String)results[1], (String)results[0], (Integer)results[2]);

	        	 }else {
	        		 status=  "Enter correct event id";
	        	 }
	        	 System.out.println(status);
	        	 logger.log(2, status);
	         
	         } else if(option == Protocol.REMOVE_EVENT) { //remove an event
	        	 String eventID = inputEventID();
	        	 String eventType = inputEventType();
	        	 String status = h.removeEvent(eventID, eventType);
	        	 logger.log(2, "removing an event " + eventType + " " + eventID);
	        	 System.out.println(status);
	        	 logger.log(2, status);
	        	 
	         } else if(option == Protocol.EVENT_AVAILABLITY) { //list event availability
	        	 logger.log(2, "listing event availability");
	        	 String eventType = inputEventType();
        	 	String result = h.listEventAvailability(eventType);
        	 	System.out.println("\n"+result);
        	 	logger.log(2, result);
	         } else if(option == Protocol.SWAP_EVENT) {//swap event
	        	 String oldEventType = inputOldEventType();
	        	 String oldEventID = inputOldEventID();
	        	 String newEventType = inputNewEventType();
	        	 String newEventID = inputNewEventID();
	        	 String status = h.swapEvent(cusID, newEventID, newEventType, oldEventID, oldEventType);
	        	 logger.log(2, "swapping events ");
	        	 System.out.println(status);
	        	 logger.log(2, status);
	         }
	         
	         
	      }
	      catch (Exception e) {
	         System.out.println("Exception in Client: " + e);
	         e.printStackTrace();
	         
	         try {
				logger.log(0, "Exception in Client: " + e.getMessage());
			} catch (IOException e1) {
				System.out.println("Error while trying to log in Client");
				e1.printStackTrace();
			}
	      } 
		
	}


	/**
	 * Input event type.
	 *
	 * @return the string
	 */
	private static String inputEventType() {
		//choose event type
		Scanner scan = new Scanner(System.in);
    	showEventTypeMenu();
 		System.out.print("Enter event type:");
 		String eventType = "";
 		while(true) {
 			eventType = scan.nextLine();
 			if(eventType.equalsIgnoreCase("conference") || eventType.equalsIgnoreCase("tradeshow")
 				|| eventType.equalsIgnoreCase("seminar"))
 				break;
 			else
 				System.out.print("ERROR: Incorret input. Retry: ");
 		}
		return eventType;
	}
	
	private static String inputOldEventType() {
		//choose event type
		Scanner scan = new Scanner(System.in);
    	showEventTypeMenu();
 		System.out.print("Enter old event type:");
 		String eventType = "";
 		while(true) {
 			eventType = scan.nextLine();
 			if(eventType.equalsIgnoreCase("conference") || eventType.equalsIgnoreCase("tradeshow")
 				|| eventType.equalsIgnoreCase("seminar"))
 				break;
 			else
 				System.out.print("ERROR: Incorret input. Retry: ");
 		}
		return eventType;
	}
	
	private static String inputNewEventType() {
		//choose event type
		Scanner scan = new Scanner(System.in);
    	showEventTypeMenu();
 		System.out.print("Enter new event type:");
 		String eventType = "";
 		while(true) {
 			eventType = scan.nextLine();
 			if(eventType.equalsIgnoreCase("conference") || eventType.equalsIgnoreCase("tradeshow")
 				|| eventType.equalsIgnoreCase("seminar"))
 				break;
 			else
 				System.out.print("ERROR: Incorret input. Retry: ");
 		}
		return eventType;
	}

	/**
	 * Input event ID.
	 *
	 * @return the string
	 */
	private static String inputEventID() {
		//enter event ID
		Scanner scan = new Scanner(System.in);
 		String eventID = "";
 		System.out.print("Enter event ID:");
 		while(true) {
 			eventID = scan.nextLine();
 			if(validEventID(eventID))
 				break;
 			else System.out.print("Invalid event ID. Retry: ");
 		}
		return eventID;
	}
	
	private static String inputOldEventID() {
		//enter event ID
		Scanner scan = new Scanner(System.in);
 		String eventID = "";
 		System.out.print("Enter old event ID:");
 		while(true) {
 			eventID = scan.nextLine();
 			if(validEventID(eventID))
 				break;
 			else System.out.print("Invalid event ID. Retry: ");
 		}
		return eventID;
	}
	
	private static String inputNewEventID() {
		//enter event ID
		Scanner scan = new Scanner(System.in);
 		String eventID = "";
 		System.out.print("Enter new event ID:");
 		while(true) {
 			eventID = scan.nextLine();
 			if(validEventID(eventID))
 				break;
 			else System.out.print("Invalid event ID. Retry: ");
 		}
		return eventID;
	}

	/**
	 * Creates the event.
	 *
	 * @return the object[]
	 */
	private static Object[] createEvent() {
		Scanner scan = new Scanner(System.in);
		showEventTypeMenu();
		System.out.print("Enter event type:");
		String eventType = "";
		while(true) {
			eventType = scan.nextLine();
			if(eventType.equalsIgnoreCase("conference") || eventType.equalsIgnoreCase("tradeshow")
				|| eventType.equalsIgnoreCase("seminar"))
				break;
			else
				System.out.print("ERROR: Incorret input. Enter a number:");
		}
		
		
		String place = "";
		while(true) {
			showCityMenu();
			System.out.print("Enter a place code:");
			place = scan.nextLine();
			if(place.equalsIgnoreCase("TOR") || place.equalsIgnoreCase("OTW") 
				|| place.equalsIgnoreCase("MTL")) {
				break;
			} else {
				System.out.print("ERROR: Incorret input. Enter a place code:");
			}
		}
		
		String time = "";
		System.out.println("Enter a letter to choose the time(M | A | E)):");
		while(true) {

			List<String> timeEvent = Arrays.asList("M", "A", "E");
			time = scan.nextLine();
			if(!timeEvent.contains(time)) {
				continue;
			} else break;
		}
		
		System.out.print("Enter a 6-digit number(DDMMYY):");
		String eventCode = "";
		while(true) {
			try {
				eventCode = scan.nextLine();
				break;
			}catch(NumberFormatException e) {
				System.out.print("ERROR: Incorret input. Enter a 4-digit number:");
			}
		}
		
		int cap = 0;
		System.out.print("Enter a capacity for event:");
		while(true) {
			try {
				cap = Integer.parseInt(scan.nextLine());
				break;
			}catch(NumberFormatException e) {
				System.out.print("Invalid input. Retry: ");
			}
			
		}
		String eventID = place + time + eventCode;
		Object[] results= {eventType, eventID, cap };
		
		return results;
	}

	/**
	 * Valid event ID.
	 *
	 * @param id the id
	 * @return true, if successful
	 */
	private static boolean validEventID(String id) {
		
		List<String> cityCodeList = Arrays.asList("TOR", "MTL", "OTW");
		List<Character> timeCodeList = Arrays.asList('A', 'M', 'E');
		if(id.length() != 10) {
			return false;
		}
		id = id.toUpperCase();
		String cityCode = id.substring(0, 3);
		
		//if(!cityCode.equalsIgnoreCase("TOR") ||!cityCode.equalsIgnoreCase("MTL") || !cityCode.equalsIgnoreCase("OTW")) {
		if(!cityCodeList.contains(cityCode)) {
		
			return false;
		}
		
		
		char type = id.charAt(3);
		
		//if(type != 'A' || type != 'M' || type != 'E') {
		if(!timeCodeList.contains(type)) {
			return false;
		}
		
		String uniqueID = id.substring(4, id.length());
		if(!isNumeric(uniqueID)) {
			return false;
		}
		return true;
	}

	/**
	 * Show operation menu.
	 *
	 * @param type the client type (customer or manager)
	 */
	private static void showOperationMenu(char type) {
		//let user choose operations
		System.out.print("Below are operations you can perform:\n");
		
		System.out.println(Protocol.BOOK_EVENT+"/ Book an event");
		System.out.println(Protocol.GET_SCHEDULE_EVENT+"/ Get booking schedule");
		System.out.println(Protocol.CANCEL_EVENT+"/ Cancel an event");
		System.out.println(Protocol.SWAP_EVENT+"/ Swap Event");
		//print additional options for manager
		if(type == 'M') {
			System.out.println(Protocol.ADD_EVENT+"/ Add an Event");
			System.out.println(Protocol.REMOVE_EVENT+"/ Remove an Event");
			System.out.println(Protocol.EVENT_AVAILABLITY+"/ List Event Availability");
		}
		
	}

	/**
	 * Validate client ID.
	 *
	 * @param id the client id
	 * @return true, if valid
	 */
	private static boolean validateID(String id) {
		if(id.length() != 8) {
			return false;
		}
		id = id.toUpperCase();
		String cityCode = id.substring(0, 3);
		if(!cityCode.equalsIgnoreCase("TOR") ||!cityCode.equalsIgnoreCase("MTL") || !cityCode.equalsIgnoreCase("OTW")) {
			return false;
		}
		
		char type = id.charAt(3);
		if(type != 'C' || type != 'M') {
			return false;
		}
		
		String uniqueID = id.substring(4, id.length());
		if(!isNumeric(uniqueID)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if is numeric.
	 *
	 * @param strNum the str num
	 * @return true, if is numeric
	 */
	private static boolean isNumeric(String strNum) {
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException | NullPointerException nfe) {
	        return false;
	    }
	    return true;
	    
	}

	

}
