package cases;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Client {
	
	public static void main(String[] args) throws IOException {
		int PORT = 6661;
		
		DatagramSocket socket = new DatagramSocket(PORT);
		byte[] outgoing = "need access".getBytes();
//		InetSocketAddress sock = new InetSocketAddress("192.168.43.153", PORT);
//		socket.bind(sock);
		InetAddress address = InetAddress.getByName("192.168.43.99");
		DatagramPacket outgoingPacket = new DatagramPacket(outgoing, outgoing.length, address, 5591);
		socket.send(outgoingPacket);
		
		byte[] incoming = new byte[100];
		DatagramPacket incomingPacket = new DatagramPacket(incoming, incoming.length);
		
		socket.receive(incomingPacket);
		System.out.print(new String(incoming));
		
		socket.close();
	}
}
