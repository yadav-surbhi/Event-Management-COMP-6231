package frontend;

import java.io.IOException;

import ipconfig.IPConfig;
import vspackage.bean.Header;

public class MulticastRM {
	
	private Header header;
	
	MulticastRM(Header header) {
		
		this.header = header;
	}
	
	public void multicast() throws NumberFormatException, IOException {
		
		int totalRM = Integer.parseInt(IPConfig.getProperty("total_rm")); 
		
		String rm_one_addr = IPConfig.getProperty("host1");
		String rm_two_addr = IPConfig.getProperty("host2");
		String rm_three_addr = IPConfig.getProperty("host3");
		String rm_four_addr = IPConfig.getProperty("host4");
		
		int port_rm = Integer.parseInt(IPConfig.getProperty("port_rm"));
		
		
		UnicastRM unicastOne = new UnicastRM(rm_one_addr, port_rm, header);
		UnicastRM unicastTwo = new UnicastRM(rm_two_addr, port_rm, header);
		UnicastRM unicastThree = new UnicastRM(rm_three_addr, port_rm, header);
		UnicastRM unicastFour = new UnicastRM(rm_four_addr, port_rm, header);
		
		unicastOne.unicast();
		unicastTwo.unicast();
		unicastThree.unicast();
		unicastFour.unicast();
		
		
	}
}

