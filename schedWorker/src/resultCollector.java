import java.net.*;

public class resultCollector extends Thread {
	public commonInfo cInfo; 
	
	static DatagramPacket sendPacket; 
	
	public void run(){
		String res=""; 
		while(true) { 
			try {
				res = cInfo.resultQ.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Task done at some worker , send this info to client 
			   
	       byte[] sendData = new byte[1024];
	       try {
		   System.out .println("Port = " + cInfo.port );
		   sendData = res.getBytes();
		   sendPacket = new DatagramPacket(sendData, sendData.length, 
				   					cInfo.IPAddress, cInfo.port);
		   cInfo.serverSocket.send(sendPacket);
	       } catch (Exception error) {
	 		  System.err.println("Result COllector : " + 
	 				"Error in socket communication " + error.getMessage());
	 	   }   

	       System.out.println(res);
		}
	}
}

