import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
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
			//	taskRequestXML = "<request><task><taskid>" +"1" + "</taskid><taskstr>sleep "+ "1000" +" </taskstr></task></request>";
				   
				  cInfo.taskQ.put(taskRequestXML);
			      System.out.println("RECEIVED: " + taskRequestXML);
			  } catch (Exception error) {
				  System.err.println("Task Receiver : Error in socket communication " + error.getMessage());
			  }
		   }
	   
	public void run(){
		while (true){
			receiveRequestXML();
		}
		
	}
	
}
