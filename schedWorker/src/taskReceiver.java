import java.net.*;

public class taskReceiver extends Thread {
	public commonInfo cInfo; 
	   
   static String taskRequestXML;
   static String responseXML;
   static DatagramPacket receivePacket;
   static DatagramPacket sendPacket;
   	
   
   public void receiveRequestXML() {
		  byte[] receiveData = new byte[1024];
		  try {
			  System.out.println("Server waiting for task ");
			  receivePacket = new DatagramPacket(receiveData, receiveData.length);
			  cInfo.serverSocket.receive(receivePacket);
			  taskRequestXML = new String( receivePacket.getData());
			  cInfo.IPAddress = receivePacket.getAddress();
			  cInfo.port = receivePacket.getPort();
			  taskRequestXML = taskRequestXML.trim();
			  System.out.println("RECEIVED: Length "+ taskRequestXML.length() + taskRequestXML); 
			  cInfo.taskQ.put(taskRequestXML);
		      
		  } catch (Exception error) {
			  System.err.println("Task Receiver : Error in socket communication " + error.getMessage());
			  return ;
		  }
	   }
	   
	public void run(){
		while (true){
			receiveRequestXML();
		}
		
	}
	
}
