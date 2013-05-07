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
	static long start;
	static long stop;
//	static byte[] b ; //= new byte[11024];
	static int bLen = 0 ; 
	static String  remStr; 
	client(){ 
//		 b = new byte[5000];
		bLen = 0 ; 
		remStr = ""; 
	}
	/*
	public static void receiveUDPResponseFromScheduler() {

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
		System.out.println("Q1 Waiting for response from server");
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
		  System.err.println("W1 Error in socket communication " + error.getMessage());
	    }
		}
		
	    clientSocket.close();

	}
	*/

	public static String readStringFromStream(InputStream in) 
			throws IOException {
		boolean bStart=false;
		boolean bEnd=false;
		StringBuffer out = new StringBuffer();
		String str = new String();
		int idx ; 
		byte[]  b = new byte[5000];
		//System.out.println("readStringFromStream");
		/* 
		while (bEnd == false) {
			int n;
			n = in.available();
			if (n == 0) {
				if (bStart == true) bEnd=true;
			}
			else  {
				n = in.read(b);
				//System.out.println("readStringFromStream n=" + n);
				if ((bStart == false) && (n > 0)) bStart=true;
				if (bEnd == true) {
					//System.out.println("readStringFromStream end str=" +out.toString());
					break;
				}
				out.append(new String(b, 0, n));
				//System.out.println("readStringFromStream n=" + n + "str=" +out.toString());
			}
		}
		System.out.println("Q4 starting ");
		*/
		while(bEnd == false) { 
			int n;
			int rem;
			n = in.available();
			//System.out.println("Q5 starting n =  " + n );
			if (n > 0) {
				n = n > 5000 ? 5000 : n ;  
				//System.out.println("Reading from bLen = " + bLen + " n = " + n ) ; 
				try {
					n = in.read(b, 0, n);
				} catch(IOException e) { 
					System.out.println("Reading Error : " + e.getMessage());
				}
				bLen =  n ; 
				str = new String(b,0,n).trim();
				System.out.println("P1 : "+str);
				if(str.contains("</response>")){ 
					 idx = str.lastIndexOf("</response>");
					 idx = idx + "</response>".length();
					 rem = bLen - idx;
					 str = str.substring(0,idx);
					 //str = str.trim();
  					//remStr = remStr.trim();
					 if (remStr != null) 
					 str = remStr + str ; 
					 for (int k = 0 ; k < rem ; k++) { 
						 b[k] = b[idx+k];
					 }
					 remStr = new String(b,0,rem).trim();
					 bLen = rem; 
					 System.out.println("P20 : str =" + str ) ;   
					 System.out.println("P2 : bLen " + bLen + " n = " + n + "Rem str = " + remStr ) ;   
					 break;
				}
			}else { 
				bEnd = true;
			}
		}
		//return out.toString();
		return str;
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
		System.out.println("Q2 Waiting for response from server");
		while(true) {
	    try {	
	      String collectiveResponse="";
	      
		  taskresponse = readStringFromStream(inputSockStream);
		  collectiveResponse = "<responses>";
		  collectiveResponse += taskresponse.trim();
		  collectiveResponse += "</responses>";
		  //System.out.print("RESPONSE len=" + taskresponse.length() + " DATA={" + taskresponse + "}");
		  taskRecievedCount += printResponse(collectiveResponse);

		  if (taskRecievedCount == taskSentCount) {
			  clientTCPSocket.close();
			 System.out.println("CLoasing client socket ");
			  break;
		  }
	    }
	    
	    catch (Exception error){
		  System.err.println("W1 Error in socket communication " + error.getMessage());
		  
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			try { 
			  inputSockStream = clientTCPSocket.getInputStream();
		    	  } catch (Exception e1){
			  System.err.println("Error in socket communication " + e1.getMessage());
	   	}
	    }
		}
  	    System.out.println("taskRecievedCount =" + taskRecievedCount + 
  	    				   " taskSentCount=" + taskSentCount );

	}
	
	public static void sendUDPRequestToScheduler() {
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
  	      //clientTCPSocket.setSendBufferSize(size);
		  inputSockStream = clientTCPSocket.getInputStream();
		  outputSockStream = clientTCPSocket.getOutputStream();
		  
		  outputSockStream.write(xmlRequest.getBytes());
		  System.out.println("Buffer Size = " + clientTCPSocket.getSendBufferSize());
		  System.out.println("SENTXMLFILE LENGTH=" + xmlRequest.length() + " DATA={" + xmlRequest + "}" );
	    }
	    catch (Exception error){
		  System.err.println("Error in socket communication " + error.getMessage());
	    }
	}	
	
	public static int printResponse(String xmlResponse)
	{
		//System.out.println("Response={" + xmlResponse + "}");
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
				//System.out.println("New Response List");				
				Element taskResp  = (Element) taskRespNode.item(resp);
				NodeList taskBlockNode = taskResp.getChildNodes();
				for (int i = 0; i < taskBlockNode.getLength(); i++) {
					Element taskBlock  = (Element) taskBlockNode.item(i);
					//System.out.println("New Response Block");
					//System.out.println("-----------------");
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
			//System.out.println("Completed Response List");				
			
		}
		catch (Exception error) {
			System.err.println("Error parsing : " + error.getMessage());
		}
		//System.out.println("Returning task count " + taskCount);				
		
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
						//System.out.println("batchjobstart:noSplit = true; ");
					}
					else if (taskStr.equals("==batchEnd==")) {
						//System.out.println("batchjobend: ");
						taskStr = breader.readLine();
						break;
					} else if (batchJob.equals("true") ||
						       (taskidCounter < maxTaskCount)) {
						taskidCounter++;
						//System.out.println("taskStr = " + taskStr);
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
		long  BILLION = 1000000000;
		start = System.nanoTime();
		generateRequestXMLFile();
		
		sendTCPRequestToScheduler();
		
		receiveTCPResponseFromScheduler();
		stop= System.nanoTime();
		
		System.out.println("Total Time Taken in seconds : " + (stop - start) / BILLION);

	}
}
