import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.net.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
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
		
   public boolean isLocalWorkerAvailable() {
	   
	   if (cInfo.localAvailWorkerCount > 0) {
		   return true;
	   }
	   return false;
   }
   
   public boolean putrequestIntoQueue(String requestXML) {
	   
	     
		  try {	   
			  if (cInfo.remoteWorker == true) {
				  /* put into remote task queue SQS ? */
				  /*System.out.println("Adding to remote worker queue :\n{" 
							+ requestXML + "}");*/
				  remoteSend(requestXML);							 
				  
			  } else if (isLocalWorkerAvailable()) {
				  /*System.out.println("Adding to local worker queue :\n{" 
						  								+ requestXML + "}");*/ 
				  cInfo.taskQ.put(requestXML);
			  } else {
				  System.err.println("All workers are busy, retry next time");
				  Thread.sleep(500);
				  return false;
			  }
		  } catch (Exception error) {
			  System.err.println("Task Receiver : " +
					  "Error in adding request to worker queue " +
					  error.getMessage());
			  return false;
		  }
		  return true;
   }
   
	public String  processCompleteBlock(Element taskBlock) {
		String requestXML = "";
		NodeList tasks  = taskBlock.getChildNodes();
		requestXML += "<taskblock>"; 
		for (int j = 0; j < tasks.getLength(); j++) {
			Element task  = (Element) tasks.item(j);
			requestXML += "<task>";					
			NodeList taskId  = task.getElementsByTagName("taskid");
			Text txtTaskId = (Text) taskId.item(0).getFirstChild();
			requestXML += "<taskid>" + txtTaskId.getData() + "</taskid>";					
			NodeList taskStrNode  = task.getElementsByTagName("taskstr");
			Text txtTaskStr = (Text) taskStrNode.item(0).getFirstChild();
			requestXML += "<taskstr>" + txtTaskStr.getData() + "</taskstr>";	
			requestXML += "</task>";	
		}
		requestXML += "</taskblock>";	

		return requestXML;
	}
	
	public String  processSingleTask(Element taskBlock, int j) {
		String requestXML = "";
		String requestTaskBlock = "";
		NodeList tasks  = taskBlock.getChildNodes();


		Element task  = (Element) tasks.item(j);			
		requestXML = "<task>";					
		NodeList taskId  = task.getElementsByTagName("taskid");
		Text txtTaskId = (Text) taskId.item(0).getFirstChild();
		requestXML += "<taskid>" + txtTaskId.getData() + "</taskid>";
		
		NodeList taskStrNode  = task.getElementsByTagName("taskstr");
		Text txtTaskStr = (Text) taskStrNode.item(0).getFirstChild();
		requestXML += "<taskstr>" + txtTaskStr.getData() + "</taskstr>";
		
		requestXML += "</task>";	

		requestTaskBlock += "<taskblock>"; 
		requestTaskBlock += requestXML;
		requestTaskBlock += "</taskblock>";
		return requestTaskBlock;
	}	

	   	
	public void processRequest(String xmlRequest)
	{
	   xmlRequest = xmlRequest.trim();
	   System.out.println(xmlRequest);
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
			System.out.println("taskBlockNode.getLength()=" 
							+ taskBlockNode.getLength());
			
			for (int i = 0; i < taskBlockNode.getLength(); i++) {
				String taskNodes;
				String batchJob;
				Element taskBlock  = (Element) taskBlockNode.item(i);
				taskNodes = taskBlock.getAttribute("taskNodes");
				batchJob = taskBlock.getAttribute("batchJob");
				System.out.println("Block " + i + " batchJob=" +  batchJob + 
						                     " taskNodes = " + taskNodes);
				if (batchJob.equals("true")) {
					taskRequestXML = "<response>";
					taskRequestXML += processCompleteBlock(taskBlock);
					taskRequestXML += "</response>";
					putrequestIntoQueue(taskRequestXML);
				} else {
					
					int jStart = 0;
					int jLast =  jStart + Integer.valueOf(taskNodes);
					for (int j = jStart; j < jLast; j++) {
						taskRequestXML = "<response>";
						taskRequestXML += processSingleTask(taskBlock, j);
						taskRequestXML += "</response>";
						putrequestIntoQueue(taskRequestXML);
					}
					
				}
			}
				
		}
		catch (Exception error) {
			System.err.println("Error parsing : " + error.getMessage());
		}		
	} 
	
   public boolean receiveRequestXML() {
		  byte[] receiveData = new byte[1024];
		  try {
			  System.out.println("Server waiting for task ");
			  receivePacket = new DatagramPacket(receiveData, 
					  							receiveData.length);
			  cInfo.serverSocket.receive(receivePacket);
			  taskRequestXML = new String( receivePacket.getData());
			  cInfo.IPAddress = receivePacket.getAddress();
			  cInfo.port = receivePacket.getPort();
			  taskRequestXML = taskRequestXML.trim();
			  System.out.println("RECEIVED: Length "+ 
					  		taskRequestXML.length() + taskRequestXML);
			  
			  return true;
		      
		  } catch (Exception error) {
			  System.err.println("Task Receiver : " +
					  "Error in socket communication " + error.getMessage());
			  return false;
		  }
	   }
	   
	public void run(){
		boolean bRet;
		int retryCount;
		
		while (true){
			retryCount = 10;
			do {
			bRet = receiveRequestXML();
			if (bRet == true) break;
			retryCount--;
			} while (retryCount > 0);
			processRequest(taskRequestXML);
		}
		
	}
		
}
