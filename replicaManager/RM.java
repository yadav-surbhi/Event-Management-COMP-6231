/**
 * 
 */
package replicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.gson.Gson;

import ipconfig.IPConfig;
import vspackage.RemoteMethodApp.RemoteMethodPackage.SecurityException;
import vspackage.bean.Header;
import vspackage.bean.Protocol;
import vspackage.config.Config;
import vspackage.tools.JSONParser;
import vspackage.tools.Logger;

/**
 * Replica Manager
 * @author vanduong
 *
 */
public class RM {
	private Logger logger = null;
	private String hostIP = null;
	private List<String> otherIPs = null;
	private List<String> workingIPs = null;
	/**
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * 
	 */
	public RM() throws NumberFormatException, IOException {
		logger = new Logger("RM");
		this.hostIP = InetAddress.getLocalHost().toString().split("/")[1];
		System.out.println(hostIP);
		otherIPs = new ArrayList<String>();
//		this.addOtherIPs(this.hostIP);
//		this.workingIPs.addAll(this.otherIPs);
//		logger = new Logger(hostIP);
//		logger.log(2, hostIP + " started.");
		new Thread(new ReceiveMessage(hostIP)).start();
	}

	private void addOtherIPs(String hostIP) throws IOException {
		String rm1 = IPConfig.getProperty("rm_one");
		String rm2 = IPConfig.getProperty("rm_two");
		String rm3 = IPConfig.getProperty("rm_three");
		String rm4 = IPConfig.getProperty("rm_four");
		if(hostIP.equalsIgnoreCase(rm1)) {
			this.otherIPs.add(IPConfig.getProperty("rm_two"));
			this.otherIPs.add(IPConfig.getProperty("rm_three"));
			this.otherIPs.add(IPConfig.getProperty("rm_four"));
		}else if(hostIP.equalsIgnoreCase(rm2)) {
			this.otherIPs.add(IPConfig.getProperty("rm_one"));
			this.otherIPs.add(IPConfig.getProperty("rm_three"));
			this.otherIPs.add(IPConfig.getProperty("rm_four"));
		}else if(hostIP.equalsIgnoreCase(rm3)) {
			this.otherIPs.add(IPConfig.getProperty("rm_one"));
			this.otherIPs.add(IPConfig.getProperty("rm_two"));
			this.otherIPs.add(IPConfig.getProperty("rm_four"));
		}else if(hostIP.equalsIgnoreCase(rm4)) {
			this.otherIPs.add(IPConfig.getProperty("rm_one"));
			this.otherIPs.add(IPConfig.getProperty("rm_three"));
			this.otherIPs.add(IPConfig.getProperty("rm_one"));
		}
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		new RM();

	}
	
class ReceiveMessage implements Runnable {
		
		DatagramSocket socket = null;
		String hostIP = null;
		int port;
		
		public ReceiveMessage(String ip) throws NumberFormatException, IOException {
			port = Integer.parseInt(IPConfig.getProperty("port_rm"));
			this.hostIP = ip;
			//TODO add port number
			
			this.socket = new DatagramSocket(port);
			
			logger.log(2, "ReceiveMessage(" + ip + 
					") : returned : " + "None : Init the socket and port " + port);
		}
		
		public Header unicastTwoWays(String addr, int port, Header header) throws NumberFormatException, IOException{
			Gson gson = new Gson();
			System.out.println("inside unicastTwoWays to "+ addr +":" + port);
			String data = gson.toJson(header);
			
			byte[] msg = data.getBytes();
			
			DatagramPacket packet = new DatagramPacket(msg, msg.length, 
					InetAddress.getByName(addr), port);
			
			socket.send(packet);
			
			byte [] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			try {
				socket.setSoTimeout(30000);
				socket.receive(reply);
			}catch(SocketTimeoutException e) {
				return null;
			}
			
			String content = new String(buffer).trim();
			return  gson.fromJson(content, Header.class);

		}
		
		public void unicastOneWay(String addr, int port, Header header) throws IOException {
			System.out.println("inside unicastOneWay to " + addr + " " + port);
			Gson gson = new Gson();
			
			String data = gson.toJson(header);
			
			byte[] msg = data.getBytes();
			
			DatagramPacket packet = new DatagramPacket(msg, msg.length, 
					InetAddress.getByName(addr), port);
			socket.send(packet);
		}
		
		
		public void run() {
			
			try {
				logger.log(2, "Run(" + 
						") : returned : " + "None : Thread started");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			while(true) {
				Thread.currentThread().setName(Integer.toString(port));
				System.out.println("Thread for receive : " + Thread.currentThread().getName());
				byte[] message = new byte[10000];
				DatagramPacket packet = new DatagramPacket(message, message.length);
				
				try {
					//ObjectMapper mapper = new ObjectMapper();
					socket.receive(packet);
					String content = new String(message).trim();
					System.out.println("content: " + content);
					Gson gson = new Gson();
					Header data = gson.fromJson(content, Header.class);
					
					List<String> errorList = data.getError();//the string is of IP:port
					List<String> incorrectList = data.getIncorrect();
					List<String> crashList = data.getCrash();
					
					
					
					
					/*
					 * The handling message logic here. 
					 */
					
					Object result = null;
					if(crashList.size() > 0) {
						this.syncData(crashList);
					}
					
					if(incorrectList.size() > 0) {
						this.syncData(incorrectList);
					}
					
					if(errorList.size() > 0) {
						this.syncData(errorList);
					}
//					socket.disconnect();
//					socket.close();

					logger.log(2, "Run(" + 
							") : returned : " + "None : send data from port " + port);
					
					
				} catch (SocketTimeoutException e) {
					continue;
				}
				catch (IOException e) {
					
					try {
						logger.log(0, "Run(" + 
								") : returned : " + "None : " + e.getMessage());
					} catch (IOException e1) {
						
						e1.printStackTrace();
					}
					
					e.printStackTrace();
				}
			}
		}

		private void syncData(List<String> crashList) throws NumberFormatException, IOException {
			for(String s : crashList) {
				System.out.println("inside syncData: " + crashList);
				String ip = s.split(":")[0];
				String port = s.split(":")[1];
				System.out.println("ip: " + ip);
				System.out.println("port: " + port);
				if(ip.equalsIgnoreCase(hostIP)) {//if this host ip is mentioned in the crash list, send a synch request to the other host
		
					//send a sync message to a working host and the corresponding server
					List<Map<String, HashMap<String, Integer>>> eventMapList = new ArrayList<Map<String, HashMap<String, Integer>>>();
					List<Map<String,HashMap<String, List<String>>>> eventCusList = new ArrayList<Map<String,HashMap<String, List<String>>>>();
					Header head = new Header(Protocol.SYNC_REQUEST, null, null);
					//send unicast to other hosts
					for(int i = 1; i <= 4; i++) {
						//skip itself
						if(this.hostIP.equalsIgnoreCase(IPConfig.getProperty("host"+i))) {
							continue;
						}
						Header responseHead = unicastTwoWays(IPConfig.getProperty("host"+i), 										Integer.parseInt(port), head);
						if(responseHead != null) {
							System.out.println("received response from "+ IPConfig.getProperty("host"+i) +":" + responseHead.getEventCus());
							System.out.println("received response from "+ IPConfig.getProperty("host"+i) +":"+ responseHead.getEventMap());
							eventMapList.add(responseHead.getEventMap());
							eventCusList.add(responseHead.getEventCus());
						}
						
					}
					Map<String, HashMap<String, Integer>> eventMap = getCorrectEventMap(eventMapList);
					Map<String,HashMap<String, List<String>>> eventCus = getCorrectEventCus(eventCusList);
					if(eventCus != null || eventMap != null) {
						//send sync message to the right server based on port given by the FE
						System.out.println("Sending sync data from RM to server");
						unicastOneWay(this.hostIP, Integer.parseInt(port), new Header(Protocol.SYNC, eventMap, eventCus));
					}
//					else {
//						System.out.println("DATA IS NULL");
//					}
				}
			}
			
		}

		private Map<String, HashMap<String, List<String>>> getCorrectEventCus(
				List<Map<String, HashMap<String, List<String>>>> eventCusList) {
			for(int i = 0; i < eventCusList.size() -1; i++) {
				for(int k = i + 1; k < eventCusList.size(); k++ ) {
					if(eventCusList.get(i).equals(eventCusList.get(k))){
						return eventCusList.get(i);
					}
				}
			}
			return null;
		}

		private Map<String, HashMap<String, Integer>> getCorrectEventMap(
				List<Map<String, HashMap<String, Integer>>> eventMapList) {
			if(eventMapList.size() == 1) {
				return eventMapList.get(0);
			}
			for(int i = 0; i < eventMapList.size() -1; i++) {
				for(int k = i + 1; k < eventMapList.size(); k++ ) {
					if(eventMapList.get(i).equals(eventMapList.get(k))){
						return eventMapList.get(i);
					}
				}
			}
			return null;
		}

	}

}