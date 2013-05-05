import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class resultCollector extends Thread {
	public commonInfo cInfo; 
	
	//String taskResponseXML; 	
	DatagramPacket receivePacket;
	DatagramPacket sendPacket;
	List<Message> messages = null ;
	resultCollector(){
		messages = new ArrayList<Message>(); 
	}
    public List<String> receiveCompletedRequestLocal() {
    	int sz  = 0 ;
    	int i;
    	List<String> strs = new ArrayList<String>();
		try {
			sz = cInfo.resultQ.size();
			if(sz > 0 ) { 
				for(i=0;i<sz;i++){ 
					strs.add(cInfo.resultQ.take()); 
				}
			}else {
				strs.add(cInfo.resultQ.take());
			}
			//return  cInfo.resultQ.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strs;  
		
    }
    
    public List<String> receiveCompletedRequestRemote() {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(cInfo.resultQueueUrl);
		receiveMessageRequest.setMaxNumberOfMessages(10);
		List<String> strs = new ArrayList<String>(); 
		messages.clear();
		while(true) { 
			
				System.out.println("Waiting for result from remote");
										
				messages = cInfo.sqs.receiveMessage(receiveMessageRequest).getMessages();
				System.out.println("Done Waiting for result from remote");
				
			try {	
			}catch (AmazonServiceException e ) { 
				System.out.println("Result Collector Rx Message" +  e.getMessage());
			}
			
			if (messages != null  ) {
				System.out.println("Result Queue Size " + messages.size());
				if (messages.size() > 0 ) { 
						break;
				}
			}
			
			{
				try {
					sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for(Message m : messages ) {
			System.out.println("Message body " + m.getBody());
			System.out.println("    ReceiptHandle: " + m.getReceiptHandle());
			strs.add(m.getBody()) ;// = str.concat(m.getBody()); 
		}
		return strs;
		
    }
	
    public List<String>  receiveCompletedRequest() {
		if(cInfo.remoteWorker == false ){
			return receiveCompletedRequestLocal();
    	} else {
    		return receiveCompletedRequestRemote();
	   }	
    }
    
   public String  parseCompletedRequestLocal(String xmlResponse)   {
	   String taskResponseXML = null ; 
	   xmlResponse = xmlResponse.trim();
	   
		try {
			DocumentBuilderFactory fact1 = DocumentBuilderFactory.newInstance();
			fact1.setValidating(false);
			fact1.setIgnoringElementContentWhitespace(true);
			DocumentBuilder build1 = fact1.newDocumentBuilder();

			ByteArrayInputStream astream = 
						new ByteArrayInputStream(xmlResponse.getBytes());
			Document requestDoc = build1.parse(new BufferedInputStream(astream));
			Element requestElement =  requestDoc.getDocumentElement();
			NodeList taskBlockNode = requestElement.getChildNodes();
			taskResponseXML = "<response>";
			//System.out.println("Response Rx = " + xmlResponse + "\n block Length = " + taskBlockNode.getLength());
			
			for (int i = 0; i < taskBlockNode.getLength(); i++) {
				Element taskBlock  = (Element) taskBlockNode.item(i);
				NodeList tasks  = taskBlock.getChildNodes();
				taskResponseXML += "<taskblock>"; 
				for (int j = 0; j < tasks.getLength(); j++) {
					Element task  = (Element) tasks.item(j);
					
					taskResponseXML += "<task>";					
					NodeList taskId  = task.getElementsByTagName("taskid");
					Text txtTaskId = (Text) taskId.item(0).getFirstChild();
					taskResponseXML += "<taskid>" + txtTaskId.getData() + "</taskid>";					
					
					NodeList taskStrNode  = task.getElementsByTagName("taskstr");
					
					Text txtTaskStr = (Text) taskStrNode.item(0).getFirstChild();
					
					taskResponseXML += "<taskstr>" + txtTaskStr.getData() + "</taskstr>";	
					
					taskResponseXML += "<taskstatus>" + "success" + "</taskstatus>";	
					taskResponseXML += "</task>";	
				}
				taskResponseXML += "</taskblock>";	
				
			}
			taskResponseXML += "</response>";	
			
		}
		catch (Exception error) {
			System.err.println("ResultCollector Error parsing : " + error.getMessage());
		}	
		return taskResponseXML; 
	}  
   
    public void parseCompletedRequestRemote(String xmlResponse)   {
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
	       xmlResponse += message.getBody();
	   }
    }

    public String  processCompletedRequest(String taskRequestXML) {
    	   return parseCompletedRequestLocal(taskRequestXML);
    }

    public void sendTCPResponseXML(String res) {
        try {
        	OutputStream outSockStream = null;
        	if (cInfo.acceptSocket == null) {
        		System.err.println("Accept Socket is NULL");
        	}
        	else {
        		outSockStream = cInfo.acceptSocket.getOutputStream();
	        	if (outSockStream == null) {
	        		System.err.println("Output stream null in the accept socket");
	        	} else  {
	        		outSockStream.write(res.getBytes());
	        	}
        	}
        } catch (Exception error) {
  		  System.err.println("Result Collector : " + 
  				"TCP Error in socket communication " + error.getMessage());
  	   }          
     }
    
    public void sendResponseXML(String res) {
       byte[] sendData = new byte[1024];
       try {
	   sendData = res.getBytes();
	   sendPacket = new DatagramPacket(sendData, sendData.length, 
			   					cInfo.IPAddress, cInfo.port);
	   cInfo.serverSocket.send(sendPacket);
       } catch (Exception error) {
 		  System.err.println("Result COllector : " + 
 				"UDP Error in socket communication " + error.getMessage() + "\n Ip:port = "+ cInfo.IPAddress +":" + cInfo.port);
 	   }          
    }
    
    public void cleanupCompletedRequest() {
    	if (cInfo.remoteWorker == true) {
    		for(Message m : messages) { 
    				String messageRecieptHandle = m.getReceiptHandle();
    				cInfo.sqs.deleteMessage(new DeleteMessageRequest(cInfo.resultQueueUrl , messageRecieptHandle));	
    		}
    		
    	}
    }
    
	private void millisleep(int n) {
		try
		   {
		   // Sleep at least n milliseconds.
		   // 1 millisecond = 1/1000 of a second.
		   Thread.sleep( n );
		   }
		catch ( InterruptedException e )
		   {
		   System.out.println( "awakened prematurely" );
	
		   // If you want to simulate the interrupt happening
		   // just after awakening, use the following line
		   // so that our NEXT sleep or wait
		   // will be interrupted immediately.
		   // Thread.currentThread().interrupt();
		   // Or have have same other thread awaken us:
		   // Thread us;
		   // ...
		   // us = Thread.currentThread();
		   // ...
		   // us.interrupt();
		   }
	}  
	
	public void run(){
		List<String> responses = new ArrayList<String>(); 
		String processedResponseXML; 
        while(true)
        {
        	responses = receiveCompletedRequest();	
        	for (String response : responses) {
        		System.out.println("RC : received request = " + response);
        		response = response.trim();
        		processedResponseXML = processCompletedRequest(response);
        		System.out.println("RC :processCompletedRequest completed " + processedResponseXML );
        		millisleep(10);
           	 	sendTCPResponseXML(processedResponseXML );
        	}
        	   	
        	
        	cleanupCompletedRequest();
        	
        	//millisleep(500);
        }
	}
}
