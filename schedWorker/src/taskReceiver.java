import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class taskReceiver extends Thread {
	public commonInfo cInfo; 
	   
   //static String taskRequestXML;
	static String tcpReceivedRequests; 
   static String responseXML;
   static DatagramPacket receivePacket;
   static DatagramPacket sendPacket;
   	
	 public void remoteSend(List<String> messages) {
		 boolean batch = false; 
		 String id;
		 Integer idCount = 0 ; 
	   try {
		   if ( batch == false) {
			   for(String message: messages ) {  
				   cInfo.sqs.sendMessage(new SendMessageRequest(cInfo.taskQueueUrl, message));
			   }
		   } else { 
			   
		   
		   List<SendMessageBatchRequestEntry> batchEntries = new ArrayList<SendMessageBatchRequestEntry>() ;
		   for(String message: messages ) {
			   batchEntries.add(new SendMessageBatchRequestEntry(idCount .toString(),message));
			   idCount++; 
		  
			   cInfo.sqs.sendMessageBatch( new SendMessageBatchRequest(cInfo.taskQueueUrl,batchEntries));
		   }
		   //SendMessageBatchRequest s = new SendMessageBatchRequest(cInfo.taskQueueUrl);
		   //cInfo.sqs.sendMessageBatch(); 
		   // Batch way 
		   /* 
		   public SendMessageBatchRequestEntry(String id,
		   SendMessageBatchRequestEntry(String id, String messageBody)
           SendMessageBatchRequest(String queueUrl, List<SendMessageBatchRequestEntry> entries)
           Constructs a new SendMessageBatchRequest object        String messageBody)
		   */
		   }
	   } catch (AmazonClientException ace) {
        System.out.println("Caught an AmazonClientException, which means the client encountered " +
                "a serious internal problem while trying to communicate with SQS, such as not " +
                "being able to access the network.");
        System.out.println("Error Message: " + ace.getMessage());
       }

	}
		
   public boolean putrequestIntoQueue(List<String> requestXML) {
	   
	     
		  try {	   
			  if (cInfo.remoteWorker == true) {
				  /* put into remote task queue SQS ? */
				  //System.out.println("Adding to remote worker queue :\n{"	+ requestXML + "}");
				  remoteSend(requestXML);							 
				  
			  } else {
				  /*System.out.println("Adding to local worker queue :\n{" 
						  								+ requestXML + "}");*/
				  for(String message : requestXML) { 
					  cInfo.taskQ.put(message);
				  }
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

 
		requestTaskBlock += requestXML;

		return requestTaskBlock;
	}	

	   	
	public void processRequest(String xmlRequest)
	{
			List<String> jobRequests = new ArrayList<String>();
			String taskRequestXML; 
		try {
 		    xmlRequest = xmlRequest.trim();
		    //System.out.println(xmlRequest);			
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
				jobRequests.clear(); 
				Element taskBlock  = (Element) taskBlockNode.item(i);
				taskNodes = taskBlock.getAttribute("taskNodes");
				batchJob = taskBlock.getAttribute("batchJob");
				System.out.println("Block " + i + " batchJob=" +  batchJob + 
						                     " taskNodes = " + taskNodes);
				if (batchJob.equals("true")) {
					taskRequestXML = "<response>";
					taskRequestXML += processCompleteBlock(taskBlock);
					taskRequestXML += "</response>";
					jobRequests.add(taskRequestXML);
				} else {
					
					int jStart = 0;
					int jLast =  jStart + Integer.valueOf(taskNodes);
					for (int j = jStart; j < jLast; ) {
						taskRequestXML = "<response>";
						taskRequestXML += "<taskblock>";
						for (int k = 0; k < cInfo.maxTaskCount; k++)
						{
							taskRequestXML += processSingleTask(taskBlock, j);
							j++;
							if (j >= jLast) break;
						}
						taskRequestXML += "</taskblock>";
						taskRequestXML += "</response>";
						System.out.println("JOb request " + taskRequestXML);
						jobRequests.add(taskRequestXML);
					}
					System.out.println("JOb request Size" + jobRequests.size());
					putrequestIntoQueue(jobRequests);
					
					//putrequestIntoQueue(taskRequestXML);
					
				}
			}
				
		}
		catch (Exception error) {
			System.err.println("Error parsing : " + error.getMessage());
		}		
	} 
	
	/*
   public boolean receiveUDPRequestXML() {
		  byte[] receiveData = new byte[11024];
		  try {
			  //System.out.println("Server waiting for task ");
			  receivePacket = new DatagramPacket(receiveData, 
					  							receiveData.length);
			  cInfo.serverSocket.receive(receivePacket);
			  taskRequestXML = new String( receivePacket.getData());
			  cInfo.IPAddress = receivePacket.getAddress();
			  cInfo.port = receivePacket.getPort();
			  taskRequestXML = taskRequestXML.trim();
			  System.out.println("RECEIVED: Length "+ taskRequestXML.length() +
					  "Rx IPaddress:Port " + cInfo.IPAddress + ":" + cInfo.port + "\n"+
					  taskRequestXML);
			  
			  return true;
		      
		  } catch (Exception error) {
			  System.err.println("Task Receiver : " +
					  "Error in socket communication " + error.getMessage());
			  cInfo.serverSocket.close();
			  return false;
		  }
	   }
   */
   public boolean receiveTCPRequestXML() {
		  Socket acceptSocket;
		  
		  try {
			  acceptSocket = cInfo.serverTCPSocket.retrieveAcceptSocket();
			  if (acceptSocket == null) {
				  System.out.println("Accept Socket Null"); 
				  return false; } 
			  System.out.println("AcceptSocket " + acceptSocket.getPort());
			 
			  cInfo.acceptSocket = acceptSocket;
			  System.out.println("Buffer Size " +  acceptSocket.getReceiveBufferSize());
			  tcpReceivedRequests = "";
			  tcpReceivedRequests = cInfo.serverTCPSocket.readString(cInfo.acceptSocket);
			  tcpReceivedRequests = tcpReceivedRequests.trim();
			  System.out.println("Received from client " +tcpReceivedRequests  ) ;
			  return true;
		      
		  } catch (IOException error) {
			  System.err.println("Task Receiver : " +
					  "Error in socket communication " + error.getMessage());
			  cInfo.serverSocket.close();
			  return false;
		  }
	   }

	private void millisleep(int n) {
		try
		{

		   Thread.sleep( n );
		}
		catch ( InterruptedException e )
		{
		   System.out.println( "awakened prematurely" );
		}
	} 
	
	public void run(){
		boolean bRet;
		System.out.println("taskReceiver running .. ");
		String residual = new String(); 
		
		while (true){
			System.out.print(".");
			bRet = receiveTCPRequestXML();
			if (bRet == true) {
				if(tcpReceivedRequests.contains("<request>")) { 
					System.out.println("First request");
					if(tcpReceivedRequests.contains("</request>")){
					//if (false) { 
						System.out.println("Last request");
						// DOnothing 
					}else{
						int idx;
						idx = tcpReceivedRequests.lastIndexOf("</taskBlock>") +  "</taskBlock>".length();
						residual = tcpReceivedRequests.substring(idx);
						tcpReceivedRequests = tcpReceivedRequests.substring(0, idx);
						tcpReceivedRequests += "</request>";
						System.out.println("Residue-" + residual );
						System.out.println("tcpReceivedRequests-" + tcpReceivedRequests );
						// Need to suffix with end request tag 
						
					}
				}else if (tcpReceivedRequests.contains("</request>")) {
					tcpReceivedRequests = "<request>" + tcpReceivedRequests ; 
				}else{
					int idx;
					idx = tcpReceivedRequests.lastIndexOf("</taskBlock>") +  "</taskBlock>".length();
					residual = tcpReceivedRequests.substring(idx);
					tcpReceivedRequests = tcpReceivedRequests.substring(0, idx);
					tcpReceivedRequests += "</request>";
					tcpReceivedRequests = "<request>" + tcpReceivedRequests ; 
					System.out.println("Residue-" + residual );
					System.out.println("tcpReceivedRequests-" + tcpReceivedRequests );
				}
				processRequest(tcpReceivedRequests);
			}
			millisleep(500);
		}
		
	}
		
}
