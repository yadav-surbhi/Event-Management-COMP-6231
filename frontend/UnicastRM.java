package frontend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.google.gson.Gson;

import ipconfig.IPConfig;
import vspackage.bean.Header;

public class UnicastRM {
	
	private String addr;
	private int port;
	private Header header;
	
	UnicastRM(String addr, int port, Header header) {
		this.addr = addr;
		this.port = port;
		this.header = header;
	}
	
	public void unicast() throws NumberFormatException, IOException {
		int selfPort = Integer.parseInt(IPConfig.getProperty("unicast_fe_port"));
		DatagramSocket socket = new DatagramSocket(selfPort);
		
		Gson gson = new Gson();
		
		String data = gson.toJson(header);
		
		byte[] msg = data.getBytes();
		
		DatagramPacket packet = new DatagramPacket(msg, msg.length, 
				InetAddress.getByName(this.addr), this.port);
		
		socket.send(packet);
		socket.close();
	}

}