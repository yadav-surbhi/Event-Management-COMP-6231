package sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import aspackage.clientServer.Util;
import ipconfig.IPConfig;
import vspackage.bean.Header;
import vspackage.tools.JSONParser;

public class Sequencer{
	private static SendRequest msr;
    private static int seqClock = 0;
    private static Map<String,String> msgqueue;
    static ArrayList<Integer> serverPortsMTL = new ArrayList<Integer>() ;
    static ArrayList<Integer> serverPortsTOR = new ArrayList<Integer>() ;
    static ArrayList<Integer> serverPortsOTW = new ArrayList<Integer>() ;
    static ArrayList<String> hostNames = new ArrayList<String>() ;
    static HashMap<String,ArrayList> serverports = new  HashMap<String,ArrayList>() ;
    
    private static Sequencer seq;
    
    private Sequencer() throws NumberFormatException, IOException {
//    	super(Sequencer.class.getSimpleName());
      msgqueue = new HashMap<String, String>();
      serverPortsMTL.add(Integer.parseInt(IPConfig.getProperty("mtl_port_as")));
      serverPortsTOR.add(Integer.parseInt(IPConfig.getProperty("tor_port_as")));
      serverPortsOTW.add(Integer.parseInt(IPConfig.getProperty("otw_port_as")));
      serverPortsTOR.add(Integer.parseInt(IPConfig.getProperty("tor_port_vs")));
      serverPortsMTL.add(Integer.parseInt(IPConfig.getProperty("mtl_port_vs")));
      serverPortsOTW.add(Integer.parseInt(IPConfig.getProperty("otw_port_vs")));
      serverports.put("MTL", serverPortsMTL);
      serverports.put("TOR", serverPortsTOR);
      serverports.put("OTW", serverPortsOTW);
      hostNames.add(IPConfig.getProperty("host1"));
       hostNames.add(IPConfig.getProperty("host3"));
       hostNames.add(IPConfig.getProperty("host2")); 
      hostNames.add(IPConfig.getProperty("host4"));
    	UDPListener(); 
    }

	public static void main(String[] args) throws NumberFormatException, IOException {
		System.out.println("Sequencer Ready And Waiting ...");
		new Sequencer();

	}

	private static void UDPListener() {
		DatagramSocket socket = null;
		try {
			int seqport = Integer.parseInt(IPConfig.getProperty("sequencer_port"));
			socket = new DatagramSocket(seqport);
			while (true) {
				byte[] buffer = new byte[Util.BUFFER_SIZE];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				String content = new String(request.getData());
				if(content.contains("resend")) {
					JSONParser parser = new JSONParser(content);
					Map<String, String> jsonObj = parser.deSerialize();
					Integer seq =Integer.parseInt(jsonObj.get("sequenceId"));
					String json = msgqueue.get(seq);
					byte[] data = json.getBytes();
					DatagramPacket packet = new DatagramPacket(data, data.length, request.getAddress(), request.getPort());
					socket.send(packet);
					new Retry(socket, packet);
					
				}else {
					
					msr = new SendRequest(socket, request, seqClock);
					msr.sendpacket();
					System.out.print("send reqeuest");
					seqClock++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}

	}
	static class Retry implements Runnable {
		DatagramSocket socket = null;
		DatagramPacket request = null;
		public Retry(DatagramSocket socket, DatagramPacket request) {
			this.socket = socket;
			this.request = request;
			
		}

		@Override
		public void run() {
			
			try {
				socket.send(request);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	static class SendRequest{
		DatagramSocket socket = null;
		DatagramPacket request = null; 
		int seqClock;

		public SendRequest(DatagramSocket socket, DatagramPacket request, int seqClock) {
			this.socket = socket;
			this.request = request;
			this.seqClock = seqClock + 1;
		}
		
       public void sendpacket(){
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
			
				String content = new String(request.getData());
				JSONParser parser = new JSONParser(content);
				System.out.print(content);
				Map<String, String> jsonObj = parser.deSerialize();
				Header header = new Header();
				header.setEventID(jsonObj.get("eventID"));
				header.setEventType(jsonObj.get("eventType"));
				header.setFromServer(jsonObj.get("fromServer"));
				header.setToServer(jsonObj.get("toServer"));
				header.setUserID(jsonObj.get("userID"));
				header.setNewEventID(jsonObj.get("newEventID"));
				header.setNewEventType(jsonObj.get("newEventType"));
				header.setProtocol(Integer.parseInt(jsonObj.get("protocol_type")));
				String capacity =jsonObj.get("capacity").trim();
				header.setCapacity(Integer.parseInt(capacity));
				System.out.println("capacity"+jsonObj.get("capacity"));
//				header.setCapacity(Integer.parseInt(jsonObj.get("capacity")));
				header.setSequenceId(seqClock);
				Gson gson = new Gson();
				String data = gson.toJson(header);
				System.out.println("data"+data);
				byte[] packet_to_send = data.getBytes();
				ArrayList<Integer> serverPorts = new ArrayList<Integer>();
				System.out.println(jsonObj.get("userID"));
				if(content.contains("userID")) {
					if(jsonObj.get("userID")!=null) {
						serverPorts = serverports.get(jsonObj.get("userID").substring(0,3));
					}else {
						serverPorts = serverports.get(jsonObj.get("fromServer"));
					}
				}
				else {
					serverPorts = serverports.get(jsonObj.get("fromServer").substring(0,3));
				}
				

				for (String host:hostNames) {
					try {
						System.out.println(host);
						InetAddress hostIP = InetAddress.getByName(host);
						for (Integer ports : serverPorts) {
							System.out.println(ports);
						DatagramPacket sendReq = new DatagramPacket(packet_to_send, packet_to_send.length,
								hostIP,ports);

							socket.send(sendReq); 
						}
						 msgqueue.put(String.valueOf(seqClock), data);
					} catch (IOException e) {
						
						e.printStackTrace();
					}finally {
						
					}

				}

			}
		});
		
		executor.shutdown();
       }
		

	}

}