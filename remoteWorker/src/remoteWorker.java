
import java.util.List;
import java.util.Map.Entry;

import java.io.*;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class remoteWorker {

	/**
	 * @param args
	 */
	static commonInfo cInfo;
	static String taskResponseXML="";

	public static void remoteSend(String message) {
		try { 
			cInfo.sqs.sendMessage(new SendMessageRequest(cInfo.resultQueueUrl, message));
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered " +
					"a serious internal problem while trying to communicate with SQS, such as not " +
					"being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public static void SQSInit(){
		//  AWSCredentialsProvider awsCredentialsProvider = new ()
		cInfo.sqs = new AmazonSQSClient(new ClasspathPropertiesFileCredentialsProvider());

		cInfo.taskQueueUrl  = "https://sqs.us-east-1.amazonaws.com/571769354000/schedToWorker";
		cInfo.resultQueueUrl = "https://sqs.us-east-1.amazonaws.com/571769354000/workerToSched";
	}
	public static int getSleepTime(String s ) {
		int idxSpc;
		String sc;

		idxSpc = s.indexOf(' ');
		sc = s.substring(idxSpc);
		sc = sc.trim(); 
		System.out.println("Sleep Time = " + Integer.valueOf(sc) ); 
		return Integer.valueOf(sc);  
	}
	
	public static void parseRequest(String xmlRequest)
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
	public static String  processCompleteBlock(Element taskBlock) {
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
	
	public static boolean executeTask(Text txtTaskId, Text txtTaskStr) {
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
	static void  displayHelp(){ 
		System.out.println("");
		System.out.println("Usage p4client [-h] -i Timeout");
	}
    public static boolean parseArgs(String[] args) {
		String str;    	
		// Input Parsing 
		int argsLen = 0;
		int idx=0;		
		argsLen = args.length ;
		
		cInfo = new commonInfo();
	
		while(idx < argsLen ) {
			str = args[idx];
			if(str.charAt(0) == '-'){
				 if(str.charAt(1) == 'h'){
					 //help
					 displayHelp();

				 }else if(str.charAt(1) == 'i'){
					 // Timeout 
					 cInfo.timeout  = Integer.parseInt(args[idx+1]) ;                                                 
					 idx++;
				 }
				 idx++;
			 }
			 else {
				 idx++;
			 }
		}
		

		return true;
    }
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		String res;
		cInfo = new commonInfo();
		parseArgs(args);
		watchDogTimer wDogT;
		
		SQSInit();

		int i;
		Text txtTaskStr = null ;
		Text txtTaskId = null;
		String response = "";
		long sleepTime ;
		wDogT = new watchDogTimer(); 
		wDogT.cInfo = cInfo;
		System.out.println("Started remote worker with Timeout " + cInfo.timeout );
		// Start watchDog Thread
		if(cInfo.timeout != 0 ) { 
			wDogT.start();
		}

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
