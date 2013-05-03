import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.net.*;
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
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class resultCollector extends Thread {
	public commonInfo cInfo; 
	
	String taskResponseXML; 	
	DatagramPacket receivePacket;
	DatagramPacket sendPacket;
	List<Message> messages = null ;
	
    public void receiveCompletedRequestLocal(String xmlCompletedRequest) {
		try {
			xmlCompletedRequest = cInfo.resultQ.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void receiveCompletedRequestRemote(String xmlCompletedRequest) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(cInfo.resultQueueUrl);
		receiveMessageRequest .setMaxNumberOfMessages(1);
	
		while(true) { 
			try { 
				messages = cInfo.sqs.receiveMessage(receiveMessageRequest).getMessages();
			}catch (AmazonServiceException e ) { 
				System.out.println("Result Collector Rx Message" +  e.getMessage());
			}
			if (messages != null  ) {
				System.out.println("Result Queue Size " + messages.size());
				if (messages.size() > 0 ) 
					break;
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
    }
	
    public void receiveCompletedRequest(String xmlCompletedRequest) {
		if(cInfo.remoteWorker == false ){
			receiveCompletedRequestLocal(xmlCompletedRequest);
    	} else {
    		receiveCompletedRequestRemote(xmlCompletedRequest);
	   }	
    }
    
   public void parseCompletedRequestLocal(String xmlResponse)   {
	   xmlResponse = xmlResponse.substring(0, (xmlResponse.lastIndexOf('>') + 1));
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
			System.err.println("Error parsing : " + error.getMessage());
		}		
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

    public void processCompletedRequest(String taskRequestXML) {
	   parseCompletedRequestLocal(taskRequestXML);
	   if (cInfo.remoteWorker == true) {
		   parseCompletedRequestRemote(taskRequestXML);
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
 				"Error in socket communication " + error.getMessage());
 	   }          
    }
    
    public void cleanupCompletedRequest() {
        String messageRecieptHandle = messages.get(0).getReceiptHandle();
        cInfo.sqs.deleteMessage(new DeleteMessageRequest(cInfo.resultQueueUrl , messageRecieptHandle));
    }
    
	public void run(){
		String res=""; 
		
        while(true)
        {
        	receiveCompletedRequest(res);	
        	
        	processCompletedRequest(res);
        	
        	sendResponseXML(taskResponseXML);
        	
        	cleanupCompletedRequest();
        }
	}
}
