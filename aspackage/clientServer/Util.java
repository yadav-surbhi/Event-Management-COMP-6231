package aspackage.clientServer;

import java.util.HashMap;

public class Util {
		public static final int BUFFER_SIZE =1000000;
		public static final String TORCITY ="TORONTO";
		public static final String MTLCITY ="Montreal";
		public static final String OTWCITY ="OTTAWA";
		public static final String COMPLETED ="Completed";
		public static final String SEMI_COLON =";";
		public static final String BOOK_EVENT="bookEvent";
		public static final String NO_SUCH_METHOD="NO_SUCH_METHOD";
		public static final String NO_SUCH_Event="NO_SUCH_METHOD";
		public static final String SEPERATOR=";*;";
		public static final String MTL ="MTL";
		public static final String TOR ="TOR";
		public static final String OTW ="OTW";
		public static final String List_Event_Availability ="listEventAvailability";
		public static final String List_Event_Availability1 ="listEventAvailability1";
		public static final String ADD_EVENT= "Add_Event";
		public static final String REM_EVENT= "Remove_Event";
		public static final String ON_BEHALF= "Operation_on_behalf_of_Customer";
		public static final String LIST_EVENT= "List_ALL_Events";
		public static final String CANCEL_EVENT ="CE";
		public static final String Get_Booking_Schedule ="getBookingSchedule";
		public static final String Get_Booking_Schedule1 ="getBookingSchedule1";
		public static final String UDPCALL = "UDP CALL";
		public static HashMap<String,String> getServer = new HashMap<String,String>();
		public static final String Booking_Cancelled= Util.Success+" Booking Cancelled.";
		public static final String bookingSuccessMsg =Util.Success+" Event is booked. To check booking schedule later select option '2' from the menu.";
		public static final String Swap_event ="swapEvent";
		public static final String Booking_Exist ="booking_exist";
		public static final String Capasity_Exist ="capasity_exist";
		public static final String Can_Book ="can_book";
		public static final String RE = "remove_event";
		public static final String Success = "Success";
		public static final String Failure = "Failure";
		public static final String SYNC = "SYNC";
		public static final String SYNC_REQUEST ="SYNC_REQUEST";
		public static final String BOOK_EVENT1 = "bookEvent1";
		public static final String CANCEL_EVENT1 = "CE1";
}