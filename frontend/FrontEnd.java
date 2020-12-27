package frontend;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONObject;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


import FEApp.FEMethodHelper;
import FEApp.FEMethodPOA;
import extension.Clock;
import ipconfig.IPConfig;
import vspackage.bean.Header;
import vspackage.bean.Protocol;


public class FrontEnd extends FEMethodPOA implements Serializable, Clock{
	
	private static final long serialVersionUID = 1L;
	public static boolean autoTimeout = false;
	public static int timeoutLimit = 10000;
	
	
	private Map<String, Integer> clock = new HashMap<String, Integer>();
	private Map<String, Integer> incorrectCount;
	
	public FrontEnd() {
		
		super();
		incorrectCount = new HashMap<String, Integer>();
			
		try {
			
			String autoTimeoutSetting = IPConfig.getProperty("auto_timeout").toLowerCase();
			
			if(autoTimeoutSetting.trim().contains("true"))
				autoTimeout = true;
			
		} catch (IOException e) {

			System.out.println("Unable to check auto timeout setting....");
		}
		
	}
	
	
	private Queue<String> getMessages() throws NumberFormatException, IOException {
		
		Queue<String> queue = new LinkedList<String>();
		
		ReceiveFromHost fromHostOne = new ReceiveFromHost(
				Integer.parseInt(IPConfig.getProperty("fe_waiting_reply_one_host")),  
				queue, Thread.currentThread());
		
		ReceiveFromHost fromHostTwo = new ReceiveFromHost(
				Integer.parseInt(IPConfig.getProperty("fe_waiting_reply_two_host")),  
				queue, Thread.currentThread());
		
		ReceiveFromHost fromHostThree = new ReceiveFromHost(
				Integer.parseInt(IPConfig.getProperty("fe_waiting_reply_three_host")),  
				queue, Thread.currentThread());
		
		ReceiveFromHost fromHostFour = new ReceiveFromHost(
				Integer.parseInt(IPConfig.getProperty("fe_waiting_reply_four_host")),  
				queue, Thread.currentThread());
		
		Thread one = new Thread(fromHostOne);

		Thread two = new Thread(fromHostTwo);
		Thread three = new Thread(fromHostThree);
		Thread four = new Thread(fromHostFour);
		
		ExecutorService service = Executors.newCachedThreadPool();
		service.execute(one);
		service.execute(two);
		service.execute(three);
		service.execute(four);
		
		service.shutdown();
		
		try {
			
			if(!FrontEnd.autoTimeout)
				Thread.sleep(Integer.parseInt(IPConfig.getProperty("timeout_for_rm")));
			
			else
				Thread.sleep(FrontEnd.timeoutLimit);
			
		} catch(InterruptedException e) {
			
			System.out.print("Received all the messages from the server....");
		}
		
		return detectCrashAddr(queue);
		
	}

	
	public Queue<String> detectCrashAddr(Queue<String> queue) throws IOException {
		
		String hostOne = IPConfig.getProperty("host1");
		String hostTwo = IPConfig.getProperty("host2");
		String hostThree = IPConfig.getProperty("host3");
		String hostFour = IPConfig.getProperty("host4");
		
		List<String> allAddr = new ArrayList<String>();
		allAddr.add(hostOne.trim());
		allAddr.add(hostTwo.trim());
		allAddr.add(hostThree.trim());
		allAddr.add(hostFour.trim());
		
		String port = "";
		
		for(String str : queue) {
			
			if(!str.contains("crash")) {
				
				String[] temp = str.split(":");
				port = temp[2];
				
				allAddr.remove(temp[1].trim().replace("/", ""));
			}
		}
		
		queue.removeAll(Arrays.asList("crashed"));
		
		for(String str : allAddr) {
			queue.add("crashed:" + str + ":" + port);
		}
		
	
		return queue;
		
	}
	
	// Idea : Receive method waits for the reply from all the servers
	// and then store the results in the queue. We use the queue to 
	// get the correct result 
	private Map<String, List<String>> verify(Queue<String> allqueue) {
		
		Queue<String> queue = new LinkedList<String>(allqueue);
		Map<String, Integer> count = new HashMap<String, Integer>();
		
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		
		List<String> successServerNames = new ArrayList<String>();
		List<String> failServerNames = new ArrayList<String>();
		List<String> crashServerNames = new ArrayList<String>();
		List<String> incorrectServerNames = new ArrayList<String>();
		Map<String,String> msgs = new HashMap<String, String>();
		String msg="";
		for(String str : queue) {
			System.out.
			println(str);
			if(str.toLowerCase().contains("success")) {
				//successCount++;
				count.put("success", count.getOrDefault("success", 0) + 1);
				String[] temp = str.split(":");
				msgs.put("success", temp[0].trim());
				successServerNames.add(temp[1].replace("/","").trim() + ":" + temp[2].trim());		
				
			} else if(str.toLowerCase().contains("incorrect")) { 
//				incorrectCount++;
				count.put("incorrect", count.getOrDefault("incorrect", 0) + 1);
				String[] temp = str.split(":");
				msgs.put("incorrect",temp[0].trim());

				incorrectServerNames.add(temp[1].replace("/","").trim() + ":" + temp[2].trim());
				
			} else if(str.toLowerCase().contains("fail")) {
				//failCount++;
				count.put("fail", count.getOrDefault("fail", 0) + 1);
				String[] temp = str.split(":");
				msgs.put("fail",  temp[0].trim());

				failServerNames.add(temp[1].replace("/","").trim() + ":" + temp[2].trim());
				
			} else if(str.toLowerCase().contains("crash")) {
				//crashCount++;
				count.put("crash", count.getOrDefault("crash"
						+ "", 0) + 1);
				String[] temp = str.split(":");

				msgs.put("crash", "crash");

				crashServerNames.add(temp[1].replace("/","").trim() + ":" + temp[2].trim());
				
			}
			
		}
		
		Map<String, Integer> sortedCount = new TreeMap<String, Integer>();
		
		count.entrySet().stream().
		sorted(Entry.comparingByValue()).
		forEach(action -> sortedCount.put(action.getKey(), action.getValue()));
		
		String result = "";
		System.out.println("sortedCount"+sortedCount.toString());
		int counttemp =0;
		for(String str : sortedCount.keySet()) {
			if(sortedCount.get(str)>counttemp) {
				counttemp = sortedCount.get(str);
				result = str;
			}
		}
			
		
		List<String> storeResult = new ArrayList<String>();
		storeResult.add(msgs.get(result));
		map.put("result", storeResult);
		map.put("success", successServerNames);
		map.put("fail", failServerNames);
		map.put("crash", crashServerNames);
		map.put("incorrect", incorrectServerNames);
		System.out.println("map"+map.toString());
		return map;
		
	}
	
	
	public synchronized String addEvent (String eventID, String eventType, int bookingCapacity) {
		
		Header header = new Header();
		header.setCapacity(bookingCapacity);
		header.setEventID(eventID);
		header.setEventType(eventType);
		header.setFromServer(eventID.substring(0, 3));
		header.setToServer(eventID.substring(0, 3));
		header.setUserID(null);
		header.setNewEventID(null);
		header.setNewEventType(null);
		header.setProtocol(Protocol.ADD_EVENT);
		
		Queue<String> queue = null;
		
		try {
			
			SendToSequencer sender = new SendToSequencer(header);
			sender.send();
			
			// Grabbing replies from all the servers.
			queue = getMessages();
			
			Map<String, List<String>> map = verify(queue);
			
			
			if(map.get("fail").size() > 0 || map.get("success").size() > 0 ||
					map.get("incorrect").size() > 0) {
				
				Header faultHeader = new Header(Protocol.FE_TO_HOST_FAULT, map.get("fail"), 
						map.get("incorrect"), map.get("crash"));
				
				MulticastRM multicast = new MulticastRM(faultHeader);
				multicast.multicast();
			}
			
			
			return map.get("result").get(0);
			
		} catch(Exception e) {
			
			e.printStackTrace();
	
		}	
		
		
		
		// TODO verification part;
		
		
		return null;
	}


	public synchronized String removeEvent (String eventID, String eventType) {
		
		Header header = new Header();
		header.setCapacity(0);
		header.setEventID(eventID);
		header.setEventType(eventType);
		header.setFromServer(eventID.substring(0, 3));
		header.setToServer(eventID.substring(0, 3));
		header.setUserID(null);
		header.setNewEventID(null);
		header.setNewEventType(null);
		header.setProtocol(Protocol.REMOVE_EVENT);
		
		Queue<String> queue = null;
		
		try {
			
			SendToSequencer sender = new SendToSequencer(header);
			sender.send();
			
			// Grabbing replies from all the servers.
			queue = getMessages();
			
			Map<String, List<String>> map = verify(queue);
			
			// TODO send message to rm.
			
//			String fault = map.get("success").size() > map.get("failed").size() ? "failed" : "success";
//			if(map.get(fault).size() > 0) {
//				
//				MulticastRM multicast = new MulticastRM(map.get(fault));
//				multicast.multicast();
//				
//			}
//			List<String> inCorrectServers = new ArrayList<String>();
//			keep count of incorrect results of each host/server
//			List<String> incorrectList = map.get("incorrect");
//			for(String s : incorrectList) {
//				incorrectCount.put(s, incorrectCount.getOrDefault(s, 0) + 1);
//				if(incorrectCount.get(s) >= 3) {
//					incorrectList.add(s);
//					//reset count
//					incorrectCount.put(s, 0);
//				}
//			}
			if(map.get("fail").size() > 0 || map.get("success").size() > 0 ||
					map.get("incorrect").size() > 0) {
				
				Header faultHeader = new Header(Protocol.FE_TO_HOST_FAULT, map.get("fail"), 
						map.get("incorrect"), map.get("crash"));
				
				MulticastRM multicast = new MulticastRM(faultHeader);
				multicast.multicast();
			}
			
			
			return map.get("result").get(0);
			
		} catch(Exception e) {
			
			e.printStackTrace();
	
		}	
		
		return null;
	}


	public synchronized String listEventAvailability (String userID, String eventType) {
		
		Header header = new Header();
		header.setCapacity(0);
		header.setEventID(null);
		header.setEventType(eventType);
		header.setFromServer(userID.substring(0, 3));
		header.setToServer(null);
		header.setUserID(userID);
		header.setNewEventID(null);
		header.setNewEventType(null);
		header.setProtocol(Protocol.EVENT_AVAILABLITY);
		
		//TODO send and receive
		Queue<String> queue = null;
		
		try {
			
			SendToSequencer sender = new SendToSequencer(header);
			sender.send();
			
			// Grabbing replies from all the servers.
			queue = getMessages();
			
			Map<String, List<String>> map = verify(queue);
			
//			
//			String fault = map.get("success").size() > map.get("failed").size() ? "failed" : "success";
//			if(map.get(fault).size() > 0) {
//				
//				MulticastRM multicast = new MulticastRM(map.get(fault));
//				multicast.multicast();
//				
//			}
			
			if(map.get("fail").size() > 0 || map.get("success").size() > 0 ||
					map.get("incorrect").size() > 0) {
				
				Header faultHeader = new Header(Protocol.FE_TO_HOST_FAULT, map.get("fail"), 
						map.get("incorrect"), map.get("crash"));
				
				MulticastRM multicast = new MulticastRM(faultHeader);
				multicast.multicast();
			}
			
			
			return queue.peek();
			
		} catch(Exception e) {
			
			e.printStackTrace();
	
		}
		return null;
	}


	public synchronized String bookEvent (String customerID, String eventID, String eventType) {
		
		Header header = new Header();
		header.setCapacity(0);
		header.setEventID(eventID);
		header.setEventType(eventType);
		header.setFromServer(customerID.substring(0, 3));
		header.setToServer(eventID.substring(0, 3));
		header.setUserID(customerID);
		header.setNewEventID(null);
		header.setNewEventType(null);
		header.setProtocol(Protocol.BOOK_EVENT);
		
		
		Queue<String> queue = null;
		
		try {
			
			SendToSequencer sender = new SendToSequencer(header);
			sender.send();
			
			// Grabbing replies from all the servers.
			queue = getMessages();
			
			Map<String, List<String>> map = verify(queue);
		
			
//			String fault = map.get("success").size() > map.get("failed").size() ? "failed" : "success";
//			if(map.get(fault).size() > 0) {
//				
//				MulticastRM multicast = new MulticastRM(map.get(fault));
//				multicast.multicast();
//				
//			}
			
			if(map.get("fail").size() > 0 || map.get("success").size() > 0 ||
					map.get("incorrect").size() > 0) {
				
				Header faultHeader = new Header(Protocol.FE_TO_HOST_FAULT, map.get("fail"), 
						map.get("incorrect"), map.get("crash"));
				
				MulticastRM multicast = new MulticastRM(faultHeader);
				multicast.multicast();
			}
			
			return map.get("result").get(0);
			
		} catch(Exception e) {
			
			e.printStackTrace();
	
		}	
		
		return null;
	}


	public synchronized String getBookingSchedule (String customerID) {
		
		Header header = new Header();
		header.setCapacity(0);
		header.setEventID(null);
		header.setEventType(null);
		header.setFromServer(customerID.substring(0, 3));
		header.setToServer(null);
		header.setUserID(customerID);
		header.setNewEventID(null);
		header.setNewEventType(null);
		header.setProtocol(Protocol.GET_SCHEDULE_EVENT);
		
		Queue<String> queue = null;
		
		try {
			
			SendToSequencer sender = new SendToSequencer(header);
			sender.send();
			
			// Grabbing replies from all the servers.
			queue = getMessages();
			
//			
//			System.out.print("--------------------------------------------------------------------");
//			System.out.println("After fill : " + queue);
//			System.out.print("--------------------------------------------------------------------");
			
			
			Map<String, List<String>> map = verify(queue);
			
//			System.out.print("--------------------------------------------------------------------");
//			System.out.println("After verify : " + queue);
//			System.out.print("--------------------------------------------------------------------");
//			

			
			if(map.get("fail").size() > 0 || map.get("success").size() > 0 ||
					map.get("incorrect").size() > 0) {
				
				Header faultHeader = new Header(Protocol.FE_TO_HOST_FAULT, map.get("fail"), 
						map.get("incorrect"), map.get("crash"));
				
				MulticastRM multicast = new MulticastRM(faultHeader);
				multicast.multicast();
			}
			
//			System.out.print("--------------------------------------------------------------------");
//			System.out.println("final : " + queue);
//			System.out.print("--------------------------------------------------------------------");
			
			return queue.peek();
			
		} catch(Exception e) {
			
			e.printStackTrace();
	
		}
		return null;

	}


	public synchronized String cancelEvent (String customerID, String eventID, String eventType) {
		
		Header header = new Header();
		header.setCapacity(0);
		header.setEventID(eventID);
		header.setEventType(eventType);
		header.setFromServer(customerID.substring(0, 3));
		header.setToServer(eventID.substring(0, 3));
		header.setUserID(customerID);
		header.setNewEventID(null);
		header.setNewEventType(null);
		header.setProtocol(Protocol.CANCEL_EVENT);
		
		Queue<String> queue = null;
		
		try {
			
			SendToSequencer sender = new SendToSequencer(header);
			sender.send();
			
			// Grabbing replies from all the servers.
			queue = getMessages();
			
			Map<String, List<String>> map = verify(queue);
			
//			String fault = map.get("success").size() > map.get("failed").size() ? "failed" : "success";
//			if(map.get(fault).size() > 0) {
//				
//				MulticastRM multicast = new MulticastRM(map.get(fault));
//				multicast.multicast();
//				
//			}
			
			if(map.get("fail").size() > 0 || map.get("success").size() > 0 ||
					map.get("incorrect").size() > 0) {
				
				Header faultHeader = new Header(Protocol.FE_TO_HOST_FAULT, map.get("fail"), 
						map.get("incorrect"), map.get("crash"));
				
				MulticastRM multicast = new MulticastRM(faultHeader);
				multicast.multicast();
			}
			
			return map.get("result").get(0);
			
		} catch(Exception e) {
			
			e.printStackTrace();
	
		}	
		return null;
	}


	public synchronized String swapEvent (String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) {
		
		Header header = new Header();
		header.setCapacity(0);
		header.setEventID(oldEventID);
		header.setEventType(oldEventType);
		header.setFromServer(null);
		header.setToServer(null);
		header.setUserID(customerID);
		header.setNewEventID(newEventID);
		header.setNewEventType(newEventType);
		header.setProtocol(Protocol.SWAP_EVENT);
		
		//TODO send and receive
		
		Queue<String> queue = null;
		
		try {
			
			SendToSequencer sender = new SendToSequencer(header);
			sender.send();
			
			// Grabbing replies from all the servers.
			queue = getMessages();
			
			Map<String, List<String>> map = verify(queue);
		
			
//			String fault = map.get("success").size() > map.get("failed").size() ? "failed" : "success";
//			if(map.get(fault).size() > 0) {
//				
//				MulticastRM multicast = new MulticastRM(map.get(fault));
//				multicast.multicast();
//				
//			}
			
			if(map.get("fail").size() > 0 || map.get("success").size() > 0 ||
					map.get("incorrect").size() > 0) {
				
				Header faultHeader = new Header(Protocol.FE_TO_HOST_FAULT, map.get("fail"), 
						map.get("incorrect"), map.get("crash"));
				
				MulticastRM multicast = new MulticastRM(faultHeader);
				multicast.multicast();
			}
			
			return map.get("result").get(0);
			
		} catch(Exception e) {
			
			e.printStackTrace();
	
		}	
		
		return null;
	}
	

	
	public static void main(String[] args) {
		
		try {
			// create and initialize the ORB //
			ORB orb = ORB.init(args, null);
			
			// get reference to rootpoa &amp; activate
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			FrontEnd feObj = new FrontEnd();
			

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(feObj);
			
			
			// and cast the reference to a CORBA reference
			FEApp.FEMethod href = FEMethodHelper.narrow(ref);
			// get the root naming context
			// NameService invokes the transient name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			
			// Use NamingContextExt, which is part of the
			// Interoperable Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			NameComponent path[] = ncRef.to_name("FE");
			ncRef.rebind(path, href);

			System.out.println("FE Server ready and waiting ...");
			
			// wait for invocations from clients
			for (;;) {
				orb.run();
			}
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
			
	        System.out.println("Exception in FE.main: " + e);
	        
		}


	}


	@Override
	public int getLocalTime(String name) {
		// TODO Auto-generated method stub
		return this.clock.get(name);
	}


	@Override
	public void incrementLocalTime(String name) {
		this.clock.put(name, this.clock.get(name) + 1);
		
	}


	@Override
	public Map<String, Integer> updateLocalClock(String name, Map<String, Integer> messageClock) {
		int newTime = 0;
		//compare local clock with message clock and update local clock accordingly
		for(Map.Entry<String, Integer> entry : messageClock.entrySet()) {
//			int localTime = this.clock.get(entry.getKey());
			newTime = Math.max(this.clock.get(entry.getKey()), messageClock.get(entry.getKey()));
			this.clock.put(name, newTime);
		}
		
		return this.clock;
	}
	
}


class ReceiveFromHost implements Runnable {
	
	private Queue<String> queue = null;
	private Thread thread = null;
	
	
	private int to;
	
	public ReceiveFromHost(int to, Queue<String> queue, Thread thread) {
		this.queue = queue;
		this.thread = thread;
		this.to = to;
		
	}
	
	private void receive() throws SocketException {
		
		DatagramSocket socket = new DatagramSocket(this.to);
		byte[] packet = new byte[20000];
		DatagramPacket datagram = new DatagramPacket(packet, packet.length);
		
		try {
			
			if(!FrontEnd.autoTimeout)
				socket.setSoTimeout(Integer.parseInt(IPConfig.getProperty("timeout_for_rm")));
			
			else
				socket.setSoTimeout(FrontEnd.timeoutLimit);
			
			
			socket.receive(datagram);
			
			queue.add(new String(packet) + ":" + datagram.getSocketAddress() + ":" + datagram.getPort());
			
			System.out.println("***********************************************************************************************");
			System.out.println("Received data : " + datagram.getSocketAddress() + ":" + datagram.getPort()  + "\n" + new String(packet));
			System.out.println("***********************************************************************************************");
			
			if(queue.size() == Integer.parseInt(IPConfig.getProperty("total_rm"))) {
				thread.interrupt();
			
			}
			
		} catch(SocketTimeoutException e) {
			
			queue.add("crashed");
			
			if(FrontEnd.autoTimeout)
				FrontEnd.timeoutLimit += 10;
			
			System.out.println("Request timeout....");
			
		
		} catch(Exception e) {
			
			//queue.add("failure" + ":" + datagram.getSocketAddress() + ":" + datagram.getPort());
			e.printStackTrace();
			
		} finally {
			
			socket.close();
		}
		System.out.println("about to exit received()");
	}
	
	
	@Override
	public void run() {
		
		try {
			
			receive();
			
		} catch (SocketException e) {
			
			e.printStackTrace();
		}
	}

}


class SendToSequencer {
	
	private DatagramSocket socket = null;
	private int port;
	private Header header = null;
	
	public SendToSequencer(Header header) {
		
		try {
			int selfPort = Integer.parseInt(IPConfig.getProperty("unicast_fe_port"));
			socket = new DatagramSocket(selfPort);
			this.header = header;
			this.port = Integer.parseInt(IPConfig.getProperty("sequencer_port"));
		} catch (Exception e) {
			
			e.printStackTrace();
		} 
	}

	
	@SuppressWarnings("unchecked")
	public String send() throws IOException {

		String sequencerAddr = IPConfig.getProperty("sequencer_addr");
		
		InetAddress addr = InetAddress.getByName(sequencerAddr);
		JSONObject jsonData = new JSONObject();
		
		jsonData.put("protocol_type", header.getProtocol());
		jsonData.put("userID", header.getUserID());
		jsonData.put("fromServer", header.getFromServer());
		jsonData.put("toServer", header.getToServer());
		jsonData.put("eventID", header.getEventID());
		jsonData.put("eventType", header.getEventType());
		jsonData.put("newEventID", header.getNewEventID());
		jsonData.put("newEventType", header.getNewEventType());
		jsonData.put("capacity", header.getCapacity());
		
		String json = jsonData.toJSONString();
		
		byte[] data = json.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
		
		socket.send(packet);
		
		byte[] statusCode = new byte[10000];
		
		//DatagramPacket ack = new DatagramPacket(statusCode, statusCode.length);
		//socket.receive(ack);
		
		
		socket.disconnect();
		socket.close();
		
		
		return new String(statusCode);

	}
	
}
