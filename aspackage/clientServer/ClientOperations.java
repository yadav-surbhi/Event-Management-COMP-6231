package aspackage.clientServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * @author apoorvasharma
 *@deprecated
 */
public interface ClientOperations extends Remote {
	
	
	String addEvent(String eventId, String eventType, int bookingCapacity)throws RemoteException;
	String bookEvent(String customerId, String eventId, String eventType)throws RemoteException;
	String getBookingSchedule(String customerId) throws RemoteException;
	String cancelEvent(String customerId, String eventId, String eventType) throws RemoteException;
	String removeEvent(String eventId, String eventType) throws RemoteException;
	String listEventAvailability(String eventType)throws RemoteException;
	String swapEvent (String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType)throws RemoteException;


}
