import java.net.*;
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class resultCollector extends Thread {
	public commonInfo cInfo; 
	
	static DatagramPacket sendPacket; 
	
	public void run(){
		String res=""; 
		while(true) {
			if(cInfo.remoteWorker == false ){
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
			} else {
				List<Message> messages = null ;
				ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(cInfo.resultQueueUrl);
				receiveMessageRequest .setMaxNumberOfMessages(1);
				
				while(true) { 
					messages = cInfo.sqs.receiveMessage(receiveMessageRequest).getMessages();
					if (messages.size() > 0 ) { 
						break;
					}else{
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
	            for (Message message : messages) {
	                System.out.println("  Message");
	                System.out.println("    MessageId:     " + message.getMessageId());
	                System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
	                System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
	                System.out.println("    Body:          " + message.getBody());
	                for (Entry<String, String> entry : message.getAttributes().entrySet()) {
	                    System.out.println("  Attribute");
	                    System.out.println("    Name:  " + entry.getKey());
	                    System.out.println("    Value: " + entry.getValue());
	                }
	    			
	    			// Task done at some worker , send this info to client 
	               res = message.getBody();   
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
	            }
	            String messageRecieptHandle = messages.get(0).getReceiptHandle();
	            cInfo.sqs.deleteMessage(new DeleteMessageRequest(cInfo.resultQueueUrl , messageRecieptHandle));
	            
			}
 	       System.out.println(res);
		}
	}
}

