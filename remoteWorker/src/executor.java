import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;


public class executor extends Thread {
	public commonInfo cInfo;
	public  String taskResponseXML="";
	
	executor(){
		
	}
	
	public String  processCompleteBlock(Element taskBlock) {
		String responseXML = "";
		NodeList tasks  = taskBlock.getChildNodes();
		responseXML += "<taskblock>"; 
		for (int j = 0; j < tasks.getLength(); j++) {
			Element task  = (Element) tasks.item(j);
			boolean result;
			
			responseXML += "<task>";					
			NodeList taskId  = task.getElementsByTagName("taskid");
			
			Text txtTaskId = (Text) taskId.item(0).getFirstChild();
			responseXML += "<taskid>" + txtTaskId.getData() + "</taskid>";					
			//System.out.println(responseXML);
			NodeList taskStrNode  = task.getElementsByTagName("taskstr");
			
			Text txtTaskStr = (Text) taskStrNode.item(0).getFirstChild();

			
			responseXML += "<taskstr>" + txtTaskStr.getData() + "</taskstr>";
			//System.out.println(responseXML);			
			result = executeTask(txtTaskId, txtTaskStr);		
			if (result == true) {
				responseXML += "<taskstatus>" + "success" + "</taskstatus>";
			} else {
				responseXML += "<taskstatus>" + "failure" + "</taskstatus>";
			}
			responseXML += "</task>";	
		}
		responseXML += "</taskblock>";	
		return responseXML;
	}
	public boolean executeTask(Text txtTaskId, Text txtTaskStr) {
		// Currently only sleep task is accepted as per the project
		   long sleepTime =  getSleepTime(txtTaskStr.getData()); 
		   System.out.println("localworker " + 
				   					" sleepTime = " + sleepTime +
				   					" taskid = " + txtTaskId.getData());

			try {
				Thread.sleep(sleepTime);
				return true;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
	}
	public  int getSleepTime(String s ) {
		int idxSpc;
		String sc;

		idxSpc = s.indexOf(' ');
		sc = s.substring(idxSpc);
		sc = sc.trim(); 
		System.out.println("Sleep Time = " + Integer.valueOf(sc) ); 
		return Integer.valueOf(sc);  
	}
	

	public  void parseRequest(String xmlRequest)
	{
	   xmlRequest = xmlRequest.trim();
		try {
			DocumentBuilderFactory fact1 = DocumentBuilderFactory.newInstance();
			fact1.setValidating(false);
			fact1.setIgnoringElementContentWhitespace(true);
			DocumentBuilder build1 = fact1.newDocumentBuilder();

			ByteArrayInputStream astream = 
						new ByteArrayInputStream(xmlRequest.getBytes());
			Document requestDoc = build1.parse(new BufferedInputStream(astream));
			Element requestElement =  requestDoc.getDocumentElement();
			NodeList taskBlockNode = requestElement.getChildNodes();
			taskResponseXML = "<response>";
			for (int i = 0; i < taskBlockNode.getLength(); i++) {
				Element taskBlock  = (Element) taskBlockNode.item(i);
				taskResponseXML += processCompleteBlock(taskBlock);
			}
			taskResponseXML += "</response>";	
		}
		catch (Exception error) {
			System.err.println("Error parsing : " + error.getMessage());
		}		
	}   
	public void remoteSend(String message) {
		try { 
			cInfo.sqs.sendMessage(new SendMessageRequest(cInfo.resultQueueUrl, message));
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered " +
					"a serious internal problem while trying to communicate with SQS, such as not " +
					"being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

		public void run(){
		String res;
		while(cInfo.die == false ) { 
			cInfo.idle = true ; 
			List<Message> messages = null ;
			System.out.println("Try to get message from " + cInfo.taskQueueUrl ); 
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(cInfo.taskQueueUrl);
			receiveMessageRequest .setMaxNumberOfMessages(1);
			//Recursively try to get the job 
			while(cInfo.die == false) { 
				messages = cInfo.sqs.receiveMessage(receiveMessageRequest).getMessages();
				if (messages.size() > 0 ) { 
					break;
				}else{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			// Process all the received messages
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

				System.out.println("Worker = "  + " Received task : Len " + res.length() +" Task Name " + res); 

				
				cInfo.idle = false ;
				// Parse the message and Actual sleep task here 
				parseRequest(res);
				

				// Job Done Send result to scheduler
				//response = "<response><task><taskid>" + txtTaskId.getData() + "</taskid><taskstr>" + txtTaskStr.getData() + "</taskstr><taskresult>1</taskresult></task></response>";
				System.out.println(" "  +taskResponseXML);
				remoteSend(taskResponseXML);
				
	            // Delete the processed message message
	            System.out.println("Deleting a message.\n");
	            String messageRecieptHandle = messages.get(0).getReceiptHandle();
	            cInfo.sqs.deleteMessage(new DeleteMessageRequest(cInfo.taskQueueUrl, messageRecieptHandle));
			}
	}
	}
}
