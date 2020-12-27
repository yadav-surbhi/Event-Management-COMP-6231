package cases;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.AccessDeniedException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import aspackage.clientServer.MTLServer;
import aspackage.clientServer.OTW;
import aspackage.clientServer.OTWServer;
import aspackage.clientServer.TOR;
import aspackage.clientServer.TORServer;
import aspackage.OperationsApp.*;
import aspackage.clientServer.Client;
import aspackage.clientServer.MTL;


public class AS_ImplTest {

	
	@Before
	public void setUp() throws Exception {
		//clearClassData(MTL.class.getName());
		//clearClassData(TOR.class.getName());
		//clearClassData(OTW.class.getName());
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Ignore
	private void clearClassData(String name) throws NoSuchFieldException, SecurityException, java.lang.ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		// Get the private String field
		final Field field = Class.forName(name).getDeclaredField("eventBook");
		// Allow modification on the field
		field.setAccessible(true);
		// Get
		final Object oldValue = field.get(Class.forName(name).getName());
		// Sets the field to the new value
		field.set(oldValue, null);
		
		// Get the private String field
		final Field field1 = Class.forName(name).getDeclaredField("customerBook");
		// Allow modification on the field
		field1.setAccessible(true);
		// Get
		final Object oldValue2 = field1.get(Class.forName(name).getName());
		// Sets the field to the new value
		field1.set(oldValue2, null);
		
	}
	
	private aspackage.OperationsApp.DEMSOperations startORB(String hostName) throws InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		ORB orb = ORB.init(new String[] {null}, null);
		//-ORBInitialPort 1050 -ORBInitialHost localhost
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		return (aspackage.OperationsApp.DEMSOperations) DEMSOperationsHelper.narrow(ncRef.resolve_str(hostName));
	}

	@Test
	public void test() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, RemoteException, AccessDeniedException, ClassNotFoundException, IOException, IllegalArgumentException {
		Constructor<MTL> mtlConstructor;
		Constructor<TOR> torConstructor;
		Constructor<OTW> otwConstructor;
		mtlConstructor = MTL.class.getDeclaredConstructor();
		torConstructor = TOR.class.getDeclaredConstructor();
		otwConstructor = OTW.class.getDeclaredConstructor();
		mtlConstructor.setAccessible(true);
		torConstructor.setAccessible(true);
		otwConstructor.setAccessible(true);
		//create instance of servers implementation
		MTL mtl = mtlConstructor.newInstance();
		TOR tor = torConstructor.newInstance();
		OTW otw = otwConstructor.newInstance();
		
		//test 1
			
				String m1 = tor.addEvent("TORE080619", "Conference", 2);
				assertTrue(m1.contains("Success"));
				String m2 = tor.addEvent("TORE110619", "Seminar", 1);
				assertTrue(m2.contains("Success"));
				
				//test 2
				String m3 = mtl.addEvent("MTLA090619", "Conference", 2);
				assertTrue(m3.contains("Success"));
				String m4 = mtl.addEvent("MTLA080619", "Tradeshow", 1);
				assertTrue(m4.contains("Success"));
				
				//test 3
				assertTrue(otw.addEvent("OTWA190619", "Conference", 1).contains("Success"));
				assertTrue(otw.addEvent("OTWA250619", "Seminar", 1).contains("Success"));
				
				//test 4
				/*assertTrue(otw.listEventAvailability("Conference").contains("TORE080619"));
				assertTrue(otw.listEventAvailability("Conference").contains("OTWA190619"));
				assertTrue(otw.listEventAvailability("Conference").contains("OTWA190619"));
				assertTrue(otw.listEventAvailability("Seminar").contains("TORE110619"));
				assertTrue(otw.listEventAvailability("Seminar").contains("OTWA250619"));
				assertTrue(otw.listEventAvailability("Tradeshow").contains("MTLA080619"));*/
				
				//test 5
				String clientID = "OTWC1234";
				assertTrue(otw.bookEvent(clientID,"TORE080619", "Conference").contains("Success"));
				assertTrue(otw.bookEvent(clientID,"TORE110619", "Seminar").contains("Success"));
				assertTrue(otw.bookEvent(clientID, "MTLA090619", "Conference").contains("Success"));
				assertTrue(otw.bookEvent(clientID, "OTWA190619", "Conference").contains("Success"));
				
				//test 6
		        assertFalse(otw.swapEvent(clientID, "OTWA080619", "Tradeshow", "OTWA190619", "Conference").contains("F"));
		        assertFalse(otw.swapEvent(clientID, "OTWA250619", "Seminar", "TORE080619", "Conference").contains("T"));
		        //String bookSchedule = otw.getBookingSchedule(clientID);
		        /*assertTrue(bookSchedule.contains("TORE110619"));
		        assertTrue(bookSchedule.contains("OTWA250619"));
		        assertTrue(bookSchedule.contains("MTLA090619"));
		        assertTrue(bookSchedule.contains("OTWA190619"));*/
		        
		        //test 7
		        String cID = "TORC1234";
		        assertTrue(tor.bookEvent(cID, "MTLA080619", "Tradeshow").contains("Success"));
		        assertFalse(tor.swapEvent(cID, "OTWA250619", "Seminar", "MTLA080619", "Tradeshow").contains("F"));
		        assertFalse(tor.swapEvent(cID, "TORE080619", "Seminar", "MTLA090619", "Conference").contains("F"));
		        //String schedule = tor.getBookingSchedule(cID);
		        //assertTrue(schedule.contains("MTLA080619"));
		        
		        //test 8
		        /*String Cevents = mtl.listEventAvailability("Conference");
		        assertTrue(Cevents.contains("TORE080619 : 2"));
		        assertTrue(Cevents.contains("MTLA090619 : 1"));
		        assertTrue(Cevents.contains("OTWA190619 : 0"));
		        String Tevents = mtl.listEventAvailability("Tradeshow");
		        assertTrue(Tevents.contains("MTLA080619 : 0"));
		        String Sevents = mtl.listEventAvailability("Seminar");
		        assertTrue(Sevents.contains("TORE110619 : 0"));
		        assertTrue(Sevents.contains("OTWA250619 : 0"));*/
	}

}
