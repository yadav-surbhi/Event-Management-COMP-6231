package aspackage.clientServer;

import java.net.MalformedURLException;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import aspackage.OperationsApp.DEMSOperations;
import aspackage.OperationsApp.DEMSOperationsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import aspackage.utility.FileLogger;

import java.io.*;

/**
 * @author apoorvasharma
 *
 */
public class Client {

	private HashMap<String, String> registeryInfo;
	private HashMap<String, Object> serverInfo;
	private Map<String, String> requestParameters;
	private String serverResponse = null;
	private Scanner scan;
	private static String clientID= null;
	private static DEMSOperations demsOBJ =null;
	

	Client() {
		registeryInfo = new HashMap<String, String>();
		scan = new Scanner(System.in);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		ORB orb = ORB.init(args, null);
		Client clOb = new Client();
//		clOb.init();
//		System.out.print("TORM100100".substring(6, 10));
		String userID = null;
		System.out.print("Enter your 8 digit user id: ");
		userID = clOb.scan.nextLine();
		
		while (!clOb.validIdCheck(userID)) {
			System.out.println("Enter Valid id");
			System.out.print("Enter your 8 digit user id: ");
			userID = clOb.scan.nextLine();
		}
		clientID= userID;
       
		try {
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			demsOBJ = (DEMSOperations) DEMSOperationsHelper.narrow(ncRef.resolve_str(userID.substring(0, 3)));
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			System.exit(0);

		}
		String message = null;

		while (true) {
			if (userID.charAt(3) == 'M') {
				message = clOb.managerDisplay().trim();

			} else {
				message = clOb.customerDisplay(userID);
			}

			if (message != null) {
				System.out.println(message);
			}
			System.out.println();
		}

	}

	private boolean validIdCheck(String userID) {
		boolean toReturn = true;
		if (userID.length() != 8) {
			toReturn = false;
		}
		if (!userID.startsWith("MTL") && !userID.startsWith("OTW") && !userID.startsWith("TOR")) {
			toReturn = false;
		}

		if (userID.charAt(3) != 'C' && userID.charAt(3) != 'M') {
			toReturn = false;
		}

		return toReturn;

	}
	
	public boolean validCustomerIDCheck(String customerID) {
		boolean toReturn = true;
		if(customerID.length() !=8) {
			toReturn= false;
		}
		if(!customerID.startsWith("MTL") && !customerID.startsWith("OTW") && !customerID.startsWith("TOR")) {
			toReturn = false;
		}
		
		if(customerID.charAt(3) != 'C') {
			toReturn= false;
		}
		return toReturn;
	}

	private boolean validEventIDCheck(String eventID) {
		boolean toReturn = true;
		if (eventID.length() != 10) {
			toReturn = false;
		}

		if (!eventID.startsWith("MTL") && !eventID.startsWith("OTW") && !eventID.startsWith("TOR")) {
			toReturn = false;
		}

		if (eventID.charAt(3) != 'M' && eventID.charAt(3) != 'A' && eventID.charAt(3) != 'E') {
			toReturn = false;
		}
		return toReturn;
	}

	private boolean validEventTypeCheck(String eventType) {
		boolean toReturn = true;
		if (!eventType.equalsIgnoreCase("conference") && !eventType.equalsIgnoreCase("seminar")
				&& !eventType.equalsIgnoreCase("trade show") && !eventType.equalsIgnoreCase("tradeshow")) {
			toReturn = false;
		}
		return toReturn;
	}
	
	private boolean validChoiceCheck(int choice) {
		boolean toReturn = true;
		if(choice !=1 && choice !=2 && choice != 3 && choice != 4 && choice != 5) {
			toReturn = false;
		}
		return toReturn;
	}
	
	private boolean validateSwap(String newId,String oldId, String newType,String oldType) {
		boolean toReturn = true;
		if(newId.equalsIgnoreCase(oldId) && oldType.equalsIgnoreCase(newType)) {
			toReturn = false;
		}
		return toReturn;
	}


	private String managerDisplay()  {
		System.out.println("What would like to do today? Select the appropiate choice.");
		System.out.println("Select '1' to Add an Event");
		System.out.println("Select '2' to Remove an Event");
		System.out.println("Select '3' to to List out all the events");
		System.out.println("Select '4' to perform operations on behalf of customer");
		System.out.println("Select '5' to exit from the system");

		int choice = Integer.parseInt(scan.nextLine());
		while(!validChoiceCheck(choice)) {
			System.out.println("Enter a valid choice.");
			System.out.println("What would like to do today? Select the appropiate choice.");
			System.out.println("Select '1' to Add an Event");
			System.out.println("Select '2' to Remove an Event");
			System.out.println("Select '3' to to List out all the events");
			System.out.println("Select '4' to perform operations on behalf of customer");
			System.out.println("Select '5' to exit from the system");
			choice = Integer.parseInt(scan.nextLine());
		}
		String evid, evt, message = null;

		switch (choice) {
		case 1:
			System.out.print("Enter the event id: ");
			evid = scan.nextLine();

			while (!validEventIDCheck(evid)) {
				System.out.println("Enter Valid Event ID:");
				System.out.print("Enter the event id: ");
				evid = scan.nextLine();
			}

			System.out.print("Enter the event type: ");
			evt = scan.nextLine();

			while (!validEventTypeCheck(evt)) {
				System.out.println("Enter valid Event Type");
				System.out.print("Enter the event type: ");
				evt = scan.nextLine();
			}

			System.out.print("Enter the event capacity: ");
			int cap = Integer.parseInt(scan.nextLine());

			while (cap < 0) {
				System.out.println("Enter valid event capacity");
				System.out.print("Enter the event capacity: ");
				cap = Integer.parseInt(scan.nextLine());
			}
			message = demsOBJ.addEvent(evid, evt, cap);
			serverResponse = new String();
			requestParameters =new HashMap<String,String>();
			requestParameters.put("eventId",evid);
			requestParameters.put("eventType",evt);
			requestParameters.put("bookingCapacity",String.valueOf(cap));
			serverResponse= message;
			logStatus(Util.ADD_EVENT,clientID,requestParameters, serverResponse);
			break;
		case 2:
			System.out.print("Enter the event id: ");
			evid = scan.nextLine();

			while (!validEventIDCheck(evid)) {
				System.out.println("Enter Valid Event ID");
				System.out.print("Enter the event id: ");
				evid = scan.nextLine();
			}

			System.out.print("Enter the event type: ");
			evt = scan.nextLine();

			while (!validEventTypeCheck(evt)) {
				System.out.println("Enter valid Event Type");
				System.out.print("Enter the event type: ");
				evt = scan.nextLine();
			}

			message = demsOBJ.removeEvent(evid, evt);
			serverResponse = new String();
			requestParameters =new HashMap<String,String>();
			requestParameters.put("eventId",evid);
			requestParameters.put("eventType",evt);
			serverResponse= message;
			logStatus(Util.REM_EVENT,clientID,requestParameters,serverResponse);
			break;
		case 3:
			System.out.print("Enter the event type: ");
			evt = scan.nextLine();

			while (!validEventTypeCheck(evt)) {
				System.out.println("Enter valid Event Type");
				System.out.print("Enter the event type: ");
				evt = scan.nextLine();
			}

			message = demsOBJ.listEventAvailability(evt);
			serverResponse = new String();
			requestParameters =new HashMap<String,String>();
			requestParameters.put("eventId",evt);
			serverResponse= message;
			logStatus(Util.LIST_EVENT,clientID,requestParameters, serverResponse);
			
			break;
		case 4:
			System.out.print("Enter the Customer id: ");
			String cid = scan.nextLine();

			while (!validCustomerIDCheck(cid)) {
				System.out.print("Enter Valid id: ");
				System.out.println("Enter your customer's 8 digit ID: ");
				cid = scan.nextLine();
			}
			String n_clientID= cid;
			serverResponse = new String();
			requestParameters =new HashMap<String,String>();
			requestParameters.put("CustomerID",n_clientID);
			serverResponse= "Manager is doing an operation on behalf of customer";
			logStatus(Util.LIST_EVENT,clientID,requestParameters, serverResponse);
			message =  customerDisplay(cid);
			break;
		case 5: 
			System.exit(0);
		default:
			System.out.println("Invalid choice! Please try again");

		}
		return message.trim();

	}

	private String customerDisplay(String custid)  {
		System.out.println("What would like to do today? Select the appropiate choice.");
		System.out.println("Select '1' to book an Event");
		System.out.println("Select '2' to get booking schedule");
		System.out.println("Select '3' to cancel an events");
		System.out.println("Select '4' to swap events");
		System.out.println("Select '5' to exit.");
		String evid, evt, message = null;
		int choice = Integer.parseInt(scan.nextLine());
		while(!validChoiceCheck(choice)) {
			System.out.println("Enter a valid choice.");
			System.out.println("What would like to do today? Select the appropiate choice.");
			System.out.println("Select '1' to book an Event");
			System.out.println("Select '2' to get booking schedule");
			System.out.println("Select '3' to cancel an events");
			System.out.println("Select '4' to swap events");
			System.out.println("Select '5' to exit.");
			choice = Integer.parseInt(scan.nextLine());
		}
		switch (choice) {
		case 1:
			System.out.print("Enter the event id: ");
			evid = scan.nextLine();

			while (!validEventIDCheck(evid)) {
				System.out.println("Enter Valid Event ID");
				System.out.print("Enter the event id: ");
				evid = scan.nextLine();
			}

			System.out.print("Enter the event type: ");
			evt = scan.nextLine();

			while (!validEventTypeCheck(evt)) {
				System.out.println("Enter valid Event Type");
				System.out.print("Enter the event type: ");
				evt = scan.nextLine();
			}

			message = demsOBJ.bookEvent(custid, evid, evt);
			serverResponse = new String();
			requestParameters =new HashMap<String,String>();
			requestParameters.put("eventID",evid);
			requestParameters.put("eventType",evt);
			serverResponse= message;
			logStatus(Util.BOOK_EVENT,clientID,requestParameters, serverResponse);
			break;
		case 2:
			message = demsOBJ.getBookingSchedule(custid);
			serverResponse = new String();
			requestParameters =new HashMap<String,String>();
			requestParameters.put("bookingSchedule of customer",clientID);
			serverResponse= message;
			logStatus(Util.Get_Booking_Schedule,clientID,requestParameters, serverResponse);
			break;
		case 3:
			System.out.print("Enter the event id: ");
			evid = scan.nextLine();

			while (!validEventIDCheck(evid)) {
				System.out.println("Enter Valid Event ID");
				System.out.print("Enter the event id: ");
				evid = scan.nextLine();
			}

			System.out.print("Enter the event type: ");
			evt = scan.nextLine();

			while (!validEventTypeCheck(evt)) {
				System.out.println("Enter valid Event Type");
				System.out.println("Enter the event type: ");
				evt = scan.nextLine();
			}

			message = demsOBJ.cancelEvent(custid, evid, evt);
			serverResponse = new String();
			requestParameters =new HashMap<String,String>();
			requestParameters.put("eventID",evid);
			requestParameters.put("eventType",evt);
			serverResponse= message;
			logStatus(Util.CANCEL_EVENT,clientID, requestParameters, serverResponse);
			break;
		case 4:
			String oldevid,oldevType;
			System.out.print("Enter the new event id: ");
			evid = scan.nextLine();
			while(!validEventIDCheck(evid)) {
				System.out.println("Enter Valid Event ID: ");
				System.out.print("Enter the new event id: ");
				evid = scan.nextLine();
			}
			
			System.out.print("Enter the new event type: ");
			evt = scan.nextLine();
			while(!validEventTypeCheck(evt)) {
				System.out.println("Enter Valid Event Type: ");
				System.out.print("Enter the new event type: ");
				evt = scan.nextLine();
			}
			System.out.print("Enter the old event id: ");
			oldevid = scan.nextLine();
			while(!validEventIDCheck(oldevid)) {
				System.out.println("Enter Valid Event ID: ");
				System.out.print("Enter the old event id: ");
				oldevid = scan.nextLine();
			}
			
			System.out.print("Enter the old event type: ");
			oldevType = scan.nextLine();
			while(!validEventTypeCheck(oldevType)) {
				System.out.println("Enter Valid Event Type: ");
				System.out.print("Enter the old event type: ");
				oldevType = scan.nextLine();
			}
			
			if(!validateSwap(evid,oldevid,evt,oldevType)) {
				System.out.println("New and old events are same. Enter different valid events to swap.");
				System.out.print("Enter the new event id: ");
				evid = scan.nextLine();
				while(!validEventIDCheck(evid)) {
					System.out.println("Enter Valid Event ID: ");
					System.out.print("Enter the new event id: ");
					evid = scan.nextLine();
				}
				
				System.out.print("Enter the new event type: ");
				evt = scan.nextLine();
				while(!validEventTypeCheck(evt)) {
					System.out.println("Enter Valid Event Type: ");
					System.out.print("Enter the new event type: ");
					evt = scan.nextLine();
				}
				System.out.print("Enter the old event id: ");
				oldevid = scan.nextLine();
				while(!validEventIDCheck(oldevid)) {
					System.out.println("Enter Valid Event ID: ");
					System.out.print("Enter the old event id: ");
					oldevid = scan.nextLine();
				}
				
				System.out.print("Enter the old event type: ");
				oldevType = scan.nextLine();
				while(!validEventTypeCheck(oldevType)) {
					System.out.println("Enter Valid Event Type: ");
					System.out.print("Enter the old event type: ");
					oldevType = scan.nextLine();
				}
			}
			
		    message =demsOBJ.swapEvent(custid, evid, evt, oldevid, oldevType);
		    serverResponse = new String();
			requestParameters =new HashMap<String,String>();
			requestParameters.put("Customer ID",custid);
			requestParameters.put("New Event ID",evid);
			requestParameters.put("New Event Type",evt);
			requestParameters.put("Old Event ID",oldevid);
			requestParameters.put("Old Event Type",oldevType);
			serverResponse= message;
			logStatus(Util.Swap_event,custid,requestParameters, serverResponse);
			break;
		case 5:
			System.exit(0);
		default:
			System.out.println("Invalid choice! Please try again");

		}
		return message.trim();
	}
	
	private void logStatus(String requestType,String clientID,Map<String, String> requestParameters, String serverResonse) {
			 String filePath= "src/aspackage/logFiles/"+clientID;
		     FileLogger log = new FileLogger(filePath,requestType,clientID,requestParameters, serverResonse);
		     log.writeFilesForClient();
	}
}
