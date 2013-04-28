import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.net.*;
public class p4client {
	
	static String xmlRequest=null;
	static String taskFile=null;
	static String serverIpAddress=null; 
	static String serverPort=null;
	static DatagramSocket clientSocket=null;
	
	public static void displayHelp() {
		// -f fileName -u url [-n numThreads] -c command
	}
	
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
		
	    try {	
		  byte[] receiveData = new byte[1024];
		  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		  clientSocket.receive(receivePacket);
		  String taskresponse = new String(receivePacket.getData());
		  taskresponse = taskresponse.substring(0, (taskresponse.lastIndexOf('>') + 1));
		  System.out.println("FROM SERVER:{" + taskresponse + "}");
		  printResponse(taskresponse);
		  clientSocket.close();
	    }
	    catch (Exception error){
		  System.err.println("Error in socket communication " + error.getMessage());
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
		  InetAddress IPAddress = InetAddress.getByName("localhost");
		  byte[] sendData = new byte[1024];
		  sendData = xmlRequest.getBytes();
		  DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
		  clientSocket.send(sendPacket);
	    }
	    catch (Exception error){
		  System.err.println("Error in socket communication " + error.getMessage());
	    }
	}
	
	public static void printResponse(String xmlResponse)
	{
		//System.out.println("Response={" + xmlResponse + "}");
		
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
				}
			}
		}
		catch (Exception error) {
			System.err.println("Error parsing : " + error.getMessage());
		}
				
	}
	
	public static void generateRequestXMLFile(int blockSize) 
	{
		int taskid = 0;
		String taskStr;
		int xmlBlocksize = 0;
		boolean endOfParsing = false;
		try {
			System.out.println("Task File : " + taskFile);			
			FileReader freader = new FileReader(taskFile);
			BufferedReader breader = new BufferedReader(freader);
			xmlRequest = "";
			xmlRequest += "<request>";
			taskStr = breader.readLine();
			while (endOfParsing == false) {

				xmlBlocksize = 0;
				if (taskStr == null) {
					break;
				}
				xmlRequest += "<taskBlock>";			
				while (taskStr != null) {
					xmlRequest +="<task>";
					taskid++;
					xmlRequest += "<taskid>" + taskid + "</taskid>";
					xmlRequest += "<taskstr>" + taskStr + "</taskstr>";
					xmlRequest +="</task>";
					xmlBlocksize++;
					taskStr = breader.readLine();
					if ((blockSize != 0) && (xmlBlocksize == blockSize)) {
						break;
					}
					
				}
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
	
	public static void parseArgs(String [] args) 
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
                     return ; 
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
			return;
		}
		
		if (serverAddress == null) {
			System.err.println("no scheduler address, returning");
			return;
		}
		
		serverIpAddress = 	serverAddress.substring(0, (serverAddress.indexOf(':')));
		serverPort = 	serverAddress.substring((serverAddress.indexOf(':') + 1));
		System.out.println("serverIpAddress = " + serverIpAddress);
		System.out.println("serverPort = " + serverPort);
	
	}
	
	public static void main(String[] args) {
		parseArgs(args);
		
		generateRequestXMLFile(2);
		
		sendRequestToScheduler();
		
		receiveResponseFromScheduler();


	}
}