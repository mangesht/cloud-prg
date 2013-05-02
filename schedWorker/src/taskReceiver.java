import java.net.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class taskReceiver extends Thread {
	public commonInfo cInfo; 
	   
   static String taskRequestXML;
   static String responseXML;
   static DatagramPacket receivePacket;
   static DatagramPacket sendPacket;
   	
	  
	 public void remoteSend(String message) {
	   try { 
		   cInfo.sqs.sendMessage(new SendMessageRequest(cInfo.taskQueueUrl, message));
	   } catch (AmazonClientException ace) {
        System.out.println("Caught an AmazonClientException, which means the client encountered " +
                "a serious internal problem while trying to communicate with SQS, such as not " +
                "being able to access the network.");
        System.out.println("Error Message: " + ace.getMessage());
    }

	   }
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
			  
			  if (cInfo.remoteWorker == true ) { 
				  remoteSend(taskRequestXML);
			  }else { 
				  cInfo.taskQ.put(taskRequestXML);  
			  }
			  
		      
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
