import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.net.*;
public class client {
	
	static String xmlRequest=null;
	static String taskFile=null;
	static String serverIpAddress=null; 
	static String serverPort=null;
	static DatagramSocket clientSocket=null;
	static Socket clientTCPSocket=null;
	static InputStream inputSockStream;
	static OutputStream outputSockStream;
	static int taskRecievedCount=0;
	static int taskSentCount=0;
	static int maxTaskCount=25;

	public static void receiveResponseFromScheduler() {

		if (xmlRequest == null) {
			System.err.println("no task request, returning");
			return;
		}
		
		if (serverIpAddress == null) {
			System.err.println("serverIpAddress is null");
			return;			
		}

		if (serverPort == null) {
			System.err.println("serverIpAddress is null");
			return;			
		}
		
		if (clientSocket == null) {
			System.err.println("request not yet sent to scheduler");
			return;					
		}
		System.out.println("Waiting for response from server");
		while(true) {
	    try {	
	      String collectiveResponse;
		  byte[] receiveData = new byte[1024];
		  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		  clientSocket.receive(receivePacket);
		  String taskresponse = new String(receivePacket.getData());

		  collectiveResponse = "<responses>";
		  collectiveResponse += taskresponse.trim();
		  collectiveResponse += "</responses>";
		  //System.out.println("FROM SERVER:{" + taskresponse + "}");
		  taskRecievedCount += printResponse(collectiveResponse);
		  if (taskRecievedCount == taskSentCount) {
			  break;
		  }
	    }
	    catch (Exception error){
		  System.err.println("Error in socket communication " + error.getMessage());
	    }
		}
		
	    clientSocket.close();

	}

	public static String readStringFromStream(InputStream inputSockStream) 
			throws IOException {
				  String taskresponse = "";
			      int availLen;
			      int readLen;
			      int pos = 0;
			      byte b[] = new byte[1024];
			      int c = 1024;
				  boolean continueReading = true;
				  boolean readStarted = false;

				  c = 0;
				  while (c < 1024) {
					  b[c] = 0;
					  c++;
				  }
				  
				  while (continueReading) {
				      boolean endOfBuffer;
				      
				      endOfBuffer = false;
				      
					  availLen = inputSockStream.available();
					  if (availLen == 0) {
						  if (readStarted == true) {
							  continueReading = false;
							  continue;
						  }
					  }
					  else {
						  //System.out.println("available length=" + availLen);
					  }
					  
					  if (pos + availLen > 1024) {
						  readLen = 1024 - pos;
						  endOfBuffer = true;
					 } else {
						  readLen = availLen;
					  }
					  //System.out.println("readLen=" + readLen + " pos = " + pos);
					  inputSockStream.read(b, pos, readLen);


					  if (endOfBuffer == true) {
						  //System.out.println("fullbuffer taskresponse length=" 
						//		  	+ taskresponse.length());
						  c = 0;
						  while (c < 1024) {
							  if (b[c] == 0) break;
							  readStarted = true;
							  taskresponse += (char) b[c];
							  //System.out.println("(1)taskresponse=" + taskresponse);
							  b[c] = 0;
							  c++;
						  }
						  //System.out.println("(2)taskresponse=" + taskresponse);
						  pos = 0;
					  } else {
						  //System.out.println("partial buffer ");
						  //System.out.println("(2)readLen=" + readLen + " pos = " + pos);
						  if ((pos == 0) && (readLen == 0)) {
							  if (readStarted == true) {
								  continueReading = false;
								  continue;
							  }
						  }
						  c = 0;
						  while (c < 1024) {
							  if (b[c] == 0) break;
							  readStarted = true;
							  taskresponse += (char) b[c];
							  c++;
						  }
						  //System.out.println("(3)taskresponse=" + taskresponse);
						  pos += readLen;
					  }
					  
				  }
				  //System.out.println("taskresponse=" + taskresponse);
				  return taskresponse;
	}
	
	public static void receiveTCPResponseFromScheduler() {
		String taskresponse = "";
		if (xmlRequest == null) {
			System.err.println("no task request, returning");
			return;
		}
		
		if (serverIpAddress == null) {
			System.err.println("serverIpAddress is null");
			return;			
		}

		if (serverPort == null) {
			System.err.println("serverIpAddress is null");
			return;			
		}
		
		if ((clientTCPSocket == null) && (clientSocket == null)) {
			System.err.println("request not yet sent to scheduler");
			return;					
		}
		System.out.println("Waiting for response from server");
		while(true) {
	    try {	
	      String collectiveResponse="";
	      
		  taskresponse = readStringFromStream(inputSockStream);
		  collectiveResponse = "<responses>";
		  collectiveResponse += taskresponse.trim();
		  collectiveResponse += "</responses>";
		  //System.out.print("RESPONSE len=" + taskresponse.length() + " DATA={" + taskresponse + "}");
		  taskRecievedCount += printResponse(collectiveResponse);
		  System.out.println("taskRecievedCount =" + taskRecievedCount + " taskSentCount=" + taskSentCount );

		  if (taskRecievedCount == taskSentCount) {
			  clientTCPSocket.close();
			  break;
		  }
	    }
	    catch (Exception error){
		  System.err.println("Error in socket communication " + error.getMessage());
	    }
		}
	}
	
	public static void sendRequestToScheduler() {
		if (xmlRequest == null) {
			System.err.println("no task request, returning");
			return;
		}
		
		if (serverIpAddress == null) {
			System.err.println("serverIpAddress is null");
			return;			
		}

		if (serverPort == null) {
			System.err.println("serverIpAddress is null");
			return;			
		}
  	    try {	
		  clientSocket = new DatagramSocket();
		  InetAddress IPAddress = InetAddress.getByName(serverIpAddress);
		  byte[] sendData = new byte[1024];
		  sendData = xmlRequest.getBytes();
		  DatagramPacket sendPacket = new DatagramPacket(sendData,
				  		sendData.length, IPAddress, Integer.valueOf(serverPort));
		  clientSocket.send(sendPacket);
		  //System.out.println("SENTXMLFILE LENGTH=" + xmlRequest.length() + " DATA={" + xmlRequest + "}" );
	    }
	    catch (Exception error){
		  System.err.println("Error in socket communication " + error.getMessage());
	    }
	}
	
	public static void sendTCPRequestToScheduler() {
		if (xmlRequest == null) {
			System.err.println("no task request, returning");
			return;
		}
		
		if (serverIpAddress == null) {
			System.err.println("serverIpAddress is null");
			return;			
		}

		if (serverPort == null) {
			System.err.println("serverIpAddress is null");
			return;			
		}
  	    try {	
  	      clientTCPSocket = new Socket(serverIpAddress, 
				  				Integer.valueOf(serverPort));
		  
		  inputSockStream = clientTCPSocket.getInputStream();
		  outputSockStream = clientTCPSocket.getOutputStream();
		  
		  outputSockStream.write(xmlRequest.getBytes());
		  //System.out.println("SENTXMLFILE LENGTH=" + xmlRequest.length() + " DATA={" + xmlRequest + "}" );
	    }
	    catch (Exception error){
		  System.err.println("Error in socket communication " + error.getMessage());
	    }
	}	
	
	public static int printResponse(String xmlResponse)
	{
		System.out.println("Response={" + xmlResponse + "}");
		int taskCount = 0;
		try {
			DocumentBuilderFactory fact1 = DocumentBuilderFactory.newInstance();
			fact1.setValidating(false);
			fact1.setIgnoringElementContentWhitespace(true);
			DocumentBuilder build1 = fact1.newDocumentBuilder();

			ByteArrayInputStream astream = 
						new ByteArrayInputStream(xmlResponse.getBytes());
			Document requestDoc = build1.parse(new BufferedInputStream(astream));
			Element requestElement =  requestDoc.getDocumentElement();
			NodeList taskRespNode = requestElement.getChildNodes();
			for (int resp =0;resp < taskRespNode.getLength(); resp++) {
				System.out.println("New Response List");				
				Element taskResp  = (Element) taskRespNode.item(resp);
				NodeList taskBlockNode = taskResp.getChildNodes();
				for (int i = 0; i < taskBlockNode.getLength(); i++) {
					Element taskBlock  = (Element) taskBlockNode.item(i);
					System.out.println("New Response Block");
					System.out.println("-----------------");
					NodeList tasks  = taskBlock.getChildNodes();
					for (int j = 0; j < tasks.getLength(); j++) {
						Element task  = (Element) tasks.item(j);
						NodeList taskId  = task.getElementsByTagName("taskid");
						Text txtTaskId = (Text) taskId.item(0).getFirstChild();
						NodeList taskStrNode  = task.getElementsByTagName("taskstr");
						Text txtTaskStr = (Text) taskStrNode.item(0).getFirstChild();
						NodeList taskStatusNode  = task.getElementsByTagName("taskstatus");
						Text txtTaskStatus = (Text) taskStatusNode.item(0).getFirstChild();	
						System.out.println("task id = " + txtTaskId.getData() + " " +
										   "task str = " + txtTaskStr.getData() + " " +
										   "task status = " + txtTaskStatus.getData());
						taskCount++;
					}
				}
			}
			System.out.println("Completed Response List");				
			
		}
		catch (Exception error) {
			System.err.println("Error parsing : " + error.getMessage());
		}
		System.out.println("Returning task count " + taskCount);				
		
		return 	taskCount;	
	}
	
	public static void generateRequestXMLFile() 
	{
		int taskid = 0;
		int taskCount;
		int taskidCounter;
		String taskStr;
		String batchJob;
		
		String xmltaskRequest="";
		boolean endOfParsing = false;
		try {
			System.out.println("Task File : " + taskFile);			
			FileReader freader = new FileReader(taskFile);
			BufferedReader breader = new BufferedReader(freader);
			taskStr = breader.readLine();
			xmlRequest = "<request>";
			taskidCounter = 0;
			while (endOfParsing == false) {

				if (taskStr == null) {
					break;
				}

				batchJob = "false";
				taskCount = 0;
				xmltaskRequest = "";
				taskidCounter = 0;
				while (taskStr != null) {
					if (taskStr.equals("==batchStart==")) {
						batchJob="true" ;
						System.out.println("batchjobstart:noSplit = true; ");
					}
					else if (taskStr.equals("==batchEnd==")) {
						System.out.println("batchjobend: ");
						taskStr = breader.readLine();
						break;
					} else if (batchJob.equals("true") ||
						       (taskidCounter < maxTaskCount)) {
						taskidCounter++;
						System.out.println("taskStr = " + taskStr);
						xmltaskRequest +="<task>";
						taskid++;
						taskCount++;
						taskSentCount++;
						xmltaskRequest += "<taskid>" + taskid + "</taskid>";
						xmltaskRequest += "<taskstr>" + taskStr + "</taskstr>";
						xmltaskRequest +="</task>";
					} 
					
					if (!batchJob.equals("true") &&
					    (taskidCounter == maxTaskCount)) {
						taskidCounter = 0;
						taskStr = breader.readLine();
						break;
					}
					else {
						taskStr = breader.readLine();
					}
						
				}
				xmlRequest += "<taskBlock batchJob=\"" + batchJob + "\" " +
						  			    " taskNodes=\"" + taskCount + "\">";
				xmlRequest += xmltaskRequest;
				xmlRequest += "</taskBlock>";			
				
				
				if (taskStr == null) {
					endOfParsing = true;
				}
			}
			xmlRequest += "</request>";
			
			freader.close();

		}
		catch (Exception error) {
			System.err.println("File not found : " + error.getMessage());
		}
		
		if (xmlRequest == null) {
			System.err.println("no task request, returning");
			return;
		}
	}
	public static void displayHelp() {
		// -f fileName -u url [-n numThreads] -c command
		System.out.println("");
		System.out.println("Usage client [-h] -s serverIp:port -w workloadFile");
		
	}
	
	public static boolean parseArgs(String [] args) 
	{
		String str;
		int idx=0;
		int argsLen = 0;
		argsLen = args.length ;		
		String serverAddress = null;

		while(idx < argsLen ) {
             //System.out.println("Argument " + args[idx]);
             str = args[idx];
             if(str.charAt(0) == '-'){
            	 if(str.charAt(1) == 'h'){
                     //help
                     displayHelp();
                     return false; 
                 }else if(str.charAt(1) == 's'){
                     // Number of Threads this program uses
                	 serverAddress = args[idx+1] ;
                     idx++;
                 }else if(str.charAt(1) == 'w'){
                	 // Command to run 
                	 taskFile = args[idx+1];
                	 idx++;
                 }
             }
             idx++;
		 }

		if (taskFile == null) {
			System.err.println("no task request, returning");
			return false;
		}
		
		if (serverAddress == null) {
			System.err.println("no scheduler address, returning");
			return false;
		}
		
		serverIpAddress = 	serverAddress.substring(0, (serverAddress.indexOf(':')));
		serverPort = 	serverAddress.substring((serverAddress.indexOf(':') + 1));
		//System.out.println("serverIpAddress = " + serverIpAddress);
		//System.out.println("serverPort = " + serverPort);
		return true;
	
	}
	
	public static void main(String[] args) {
		if(parseArgs(args) == false) {
			return;
		}
		
		generateRequestXMLFile();
		
		sendTCPRequestToScheduler();
		
		receiveTCPResponseFromScheduler();


	}
}