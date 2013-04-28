
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
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class remoteWorker {

	/**
	 * @param args
	 */
	static commonInfo cInfo;

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
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		String res;
		cInfo = new commonInfo();
		watchDogTimer wDogT; 
		SQSInit();

		int i;
		Text txtTaskStr = null ;
		Text txtTaskId = null;
		String response = "";
		long sleepTime ;
		wDogT = new watchDogTimer(); 
		// Start watchDog Thread
		if(cInfo.timeout != 0 ) { 
			wDogT.start();
		}
		DocumentBuilderFactory fact1 = DocumentBuilderFactory.newInstance();
		DocumentBuilder build1 = null ; // = fact1.newDocumentBuilder()

		fact1.setValidating(true);
		fact1.setIgnoringElementContentWhitespace(true);
		System.out.println("Worker " +   " started");
		try {
			build1 = fact1.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(cInfo.die == false ) { 
			cInfo.idle = true ; 
			List<Message> messages = null ;
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
				try {
					ByteArrayInputStream astream = 
							new ByteArrayInputStream(res.getBytes());
					Document requestDoc = build1.parse(new BufferedInputStream(astream));
					Element requestElement =  requestDoc.getDocumentElement();

					NodeList taskNode = requestElement.getChildNodes();
					for ( i = 0; i < taskNode.getLength(); i++) {
						Element task  = (Element) taskNode.item(i);
						NodeList taskId  = task.getElementsByTagName("taskid");
						txtTaskId = (Text) taskId.item(0).getFirstChild();
						NodeList taskStr  = task.getElementsByTagName("taskstr");
						txtTaskStr = (Text) taskStr.item(0).getFirstChild();
						System.out.println("task id = " + txtTaskId.getData() );
						System.out.println("task str = " + txtTaskStr.getData() );
					}

				}
				catch (Exception error) {
					System.err.println("Error parsing : " + error.getMessage());
				}

				sleepTime =  getSleepTime(txtTaskStr.getData()); 
				System.out.println(" Actual sleep task here");
				cInfo.idle = false ;
				// Actual sleep task here 

				Thread.sleep(sleepTime);

				// Job Done Send result to scheduler
				response = "<response><task><taskid>" + txtTaskId.getData() + "</taskid><taskstr>" + txtTaskStr.getData() + "</taskstr><taskresult>1</taskresult></task></response>";
				System.out.println(" "  +response);
				remoteSend(response);
			}
		}
	}
}
