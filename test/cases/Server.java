package cases;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
	
	public static void main(String[] args) throws IOException {
		int PORT = 5591;
		
		DatagramSocket socket = new DatagramSocket(PORT);
		byte[] incoming = new byte[100];
		DatagramPacket incomingPacket = new DatagramPacket(incoming, incoming.length);
		socket.receive(incomingPacket);
		
		System.out.println("Received : " + incoming.toString());
		
		byte[] outgoing = "Access granted....".getBytes();
		
		DatagramPacket outgoingPacket = new DatagramPacket(outgoing, outgoing.length, incomingPacket.getAddress(), incomingPacket.getPort());
		socket.send(outgoingPacket);
		
		socket.close();
	}
}
