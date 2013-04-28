import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class worker extends Thread {
	public commonInfo cInfo; 
	long sleepTime ;
	public int threadId;
	String taskResponseXML="";
	
	DocumentBuilderFactory fact1 = DocumentBuilderFactory.newInstance();
	DocumentBuilder build1 ; // = fact1.newDocumentBuilder()
	
	//Default constructor 
	worker() {
		sleepTime = 2000;
	}
	// Constructor with worker Id 
	worker(int tId) { 
		sleepTime = 2000;
		threadId = tId; 
	}

	public int getSleepTime(String s ) {
		int idxSpc;
		String sc;
		
		idxSpc = s.indexOf(' ');
		sc = s.substring(idxSpc);
		sc = sc.trim(); 
		System.out.println("Sleep Time = " + Integer.valueOf(sc) ); 
		return Integer.valueOf(sc);  
		 
	}
	
	public boolean executeTask(Text txtTaskStr) {
		// Currently only sleep task is accepted as per the project
		   sleepTime =  getSleepTime(txtTaskStr.getData()); 
		   System.out.println(threadId + " Actual sleep task here");
	        // Actual sleep task here 
			try {
				sleep(sleepTime);
				return true;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
	}
	
	public void parseRequest(String xmlRequest)
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
				NodeList tasks  = taskBlock.getChildNodes();
				taskResponseXML += "<taskblock>"; 
				for (int j = 0; j < tasks.getLength(); j++) {
					Element task  = (Element) tasks.item(j);
					boolean result;
					
					taskResponseXML += "<task>";					
					NodeList taskId  = task.getElementsByTagName("taskid");
					Text txtTaskId = (Text) taskId.item(0).getFirstChild();
					taskResponseXML += "<taskid>" + txtTaskId.getData() + "</taskid>";					
					
					NodeList taskStrNode  = task.getElementsByTagName("taskstr");
					
					Text txtTaskStr = (Text) taskStrNode.item(0).getFirstChild();
					
					taskResponseXML += "<taskstr>" + txtTaskStr.getData() + "</taskstr>";	
					result = executeTask(txtTaskStr);		
					if (result == true) {
					taskResponseXML += "<taskstatus>" + "success" + "</taskstatus>";
					} else {
					taskResponseXML += "<taskstatus>" + "failure" + "</taskstatus>";
					}
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

   public void processRequest(String taskRequestXML) {
	   parseRequest(taskRequestXML);
   }
   
	public void run(){
		 String inpStr = "";
         //Get the task from Queue
	     fact1.setValidating(false);
	 	 fact1.setIgnoringElementContentWhitespace(true);
	 	 System.out.println("Worker " + threadId + " started");
	 	 try {
			build1 = fact1.newDocumentBuilder();
		 } catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		 }
	 	 
	     while(true) { 
	    	try {
	    		// System.out.println(threadId + " Waiting for task");
	    		inpStr = cInfo.taskQ.take();
	    	} catch (InterruptedException e1) {
	    		e1.printStackTrace();
	        }
		   System.out.print("Worker = " + threadId + 
				   			" Received task : Len " + inpStr.length() + 
				   			" Task Name " + inpStr +"ABC" );
		   processRequest(inpStr);
	   
		   cInfo.resultQ.add(taskResponseXML);
	     }
	}
}
