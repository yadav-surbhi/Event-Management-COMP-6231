/**
 * 
 */
package aspackage.clientServer;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import frontend.FrontEnd;



/**
 * @author apoorvasharma
 *
 */
public class MultiThreading extends Thread {
	private static FrontEnd fe = new FrontEnd();
	
	
	private static BlockingQueue<String> msgs = new LinkedBlockingQueue<String>();
	
    
	public static void main(String args[]) {
	
		
		try {
			ExecutorService executoraddEvent = Executors.newCachedThreadPool();
			executoraddEvent.execute(new Runnable() {
				@Override
				public void run() {
	
					msgs.add("ADD Event TORM100100: "+fe.addEvent("TORM100100", "Conference", 10));
					msgs.add("ADD Event TORM100101: "+fe.addEvent("TORM100101", "Conference", 10));
					msgs.add("ADD Event TORM100102: "+fe.addEvent("TORM100102", "Conference", 10));
					msgs.add("ADD Event TORM100102: "+fe.addEvent("MTLM100102", "Conference", 10));
					msgs.add("ADD Event TORM100102: "+fe.addEvent("OTWM100102", "Conference", 10));
				}});
			executoraddEvent.shutdown();
			executoraddEvent.awaitTermination(1, TimeUnit.HOURS);
			ExecutorService executor = Executors.newCachedThreadPool(); 
			executor.execute(new Runnable() {
				@Override
				public void run() {
					msgs.add("Book for TORC1001 Event TORM100100: "+fe.bookEvent("TORC1001", "TORM100100", "Conference").trim());
				}});
			executor.execute(new Runnable() {

				@Override
				public void run() {
					msgs.add("Book for TORC1002 Event TORM100100: "+fe.bookEvent("TORC1002", "TORM100100", "Conference").trim());
				}});
			
			executor.execute(new Runnable() {

				@Override
				public void run() {
					
					msgs.add("Book for TORC1001 Event TORM100100: "+fe.bookEvent("TORC1001", "MTLM100102", "Conference").trim());
					msgs.add("Book for TORC1002 Event TORM100100: "+fe.bookEvent("TORC1002", "MTLM100102", "Conference").trim());
				}});
			
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);
			System.out.println("Events booked");
			System.out.println("Events Swapping Begins");
			ExecutorService executor1 = Executors.newCachedThreadPool();
			Random rand = new Random();
			executor1.execute(new Runnable() {

				@Override
				public void run() {
					int millis = rand.nextInt((100 - 0) + 1) + 0;
					try {
						Thread.sleep(millis);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					msgs.add("Swap for TORC1001 1 "+fe.swapEvent("TORC1001", "MTLM100102", "Conference", "TORM100102", "Conference"));
					
				}

			});
			
			executor1.execute(new Runnable() {

				@Override
				public void run() {
					int millis = rand.nextInt((100 - 0) + 1) + 0;
					try {
						Thread.sleep(millis);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//msgs.add("Swap for TORC1001 2 "+fe.swapEvent("TORC1001", "TORM100100", "Seminar", "MTLM100102", "Trade Show"));
					
				}

			});
			
//			executor1.execute(new Runnable() {
//
//				@Override
//				public void run() {
//					int millis = rand.nextInt((100 - 0) + 1) + 0;
//					try {
//						Thread.sleep(millis);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					msgs.add("Swap for TORC1002 1 "+fe.swapEvent("TORC1002", "MTLM100100", "Trade Show", "TORM100100", "Trade Show"));
//					
//				}
//
//			});
//			
//			executor1.execute(new Runnable() {
//
//				@Override
//				public void run() {
//					int millis = rand.nextInt((100 - 0) + 1) + 0;
//					try {
//						Thread.sleep(millis);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					msgs.add("Swap for TORC1001 3 "+fe.swapEvent("TORC1001", "MTLM100100", "Trade Show", "OTWM100100", "Trade Show"));
//					
//				}
//
//			});
//			
//			executor1.execute(new Runnable() {
//
//				@Override
//				public void run() {
//					int millis = rand.nextInt((100 - 0) + 1) + 0;
//					try {
//						Thread.sleep(millis);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					msgs.add("Swap for TORC1002 2 "+fe.swapEvent("OTWM100102", "TORM100100", "Trade Show", "MTLM100100", "Trade Show"));
//					
//				}
//
//			});
		 
			executor1.shutdown();
			executor1.awaitTermination(1, TimeUnit.HOURS);
	
			
			while(!msgs.isEmpty()) {
				System.out.println(msgs.poll());
			}

		} catch (Exception e) {
			System.out.println(e);

		}

	}

}
