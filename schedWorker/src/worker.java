import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class worker extends Thread {
	public commonInfo cInfo; 
	long sleepTime ;
	public int threadId;
	public int status;
	public int type;	
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
		//System.out.println("Sleep Time = " + Integer.valueOf(sc) ); 
		return Integer.valueOf(sc);  
		 
	}
	
	public boolean executeTask(Text txtTaskId, Text txtTaskStr) {
		// Currently only sleep task is accepted as per the project
		   sleepTime =  getSleepTime(txtTaskStr.getData()); 
		   //System.out.println("localworker " + threadId +
			//	   					" sleepTime = " + sleepTime +
			//	   					" taskid = " + txtTaskId.getData());

			try {
				sleep(sleepTime);
				return true;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
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
				taskResponseXML += processCompleteBlock(taskBlock);
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
   
   public void putResponseIntoQueue(String taskResponseXML) {
	   boolean addSuccess = false;
	   int count = 0;
	   while (addSuccess == false)
	   try {
	   cInfo.resultQ.add(taskResponseXML);
	   status = cInfo.available;
	   cInfo.localAvailWorkerCount++;
	   addSuccess = true;
	   
	   } catch (IllegalStateException e) {
		   try {
			   Thread.sleep(50);
			   count++;
			   if (count > 100) break;
		   } catch (Exception e1) {
			   
		   }
	   }
   }
   
   public String getRequestFromQueue() {
	String inpStr = "";	   
   	try {
		inpStr = cInfo.taskQ.take();
		status = cInfo.busy;
		cInfo.localAvailWorkerCount--;
	} catch (InterruptedException e1) {
		e1.printStackTrace();
    }
   	return inpStr;
   }
   
	public void run(){
		 String requestXML;
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
    	   requestXML = getRequestFromQueue();
		   /*System.out.print("\nWorker = " + threadId + 
				   			" Received task : Len " + requestXML.length() + 
				   			"\nTask : {" + requestXML + "}\n"  );*/
		   processRequest(requestXML);
	   
		   putResponseIntoQueue(taskResponseXML);
	     }
	}
}
