import java.io.*;
import java.net.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
class p4scheduler
{
   static DatagramSocket serverSocket = null;
   static String taskRequestXML;
   static String taskResponseXML;   
   static DatagramPacket receivePacket;
   static DatagramPacket sendPacket;
   
   public static void bindDatagramSocket() {
	   try {
		      serverSocket = new DatagramSocket(9876);
	   } catch (Exception error) {
			  System.err.println("Error in socket communication " + error.getMessage());
	   }
   }
   
   
   public static void receiveRequestXML() {
	  byte[] receiveData = new byte[1024];
	  try {
	  receivePacket = new DatagramPacket(receiveData, receiveData.length);
	  serverSocket.receive(receivePacket);
	  taskRequestXML = new String( receivePacket.getData());
	  System.out.println("RECEIVED: " + taskRequestXML);
	  } catch (Exception error) {
		  System.err.println("Error in socket communication " + error.getMessage());
	  }
   }

   public static void parseRequest(String xmlResponse)
	{
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

   public static void processRequest() {
	   parseRequest(taskRequestXML);
   }

   public static void sendResponseXML() {
       byte[] sendData = new byte[1024];
       try {
	   InetAddress IPAddress = receivePacket.getAddress();
	   int port = receivePacket.getPort();

	   sendData = taskResponseXML.getBytes();
	   sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	   serverSocket.send(sendPacket);
       } catch (Exception error) {
 		  System.err.println("Error in socket communication " + error.getMessage());
 	  }
   }
   
   
   public static void main(String args[]) throws Exception
   {
	   	 	bindDatagramSocket();
	   	 	
            while(true)
            {
            	receiveRequestXML();	
            	
            	processRequest();
            	
            	sendResponseXML();
            }
   }
}