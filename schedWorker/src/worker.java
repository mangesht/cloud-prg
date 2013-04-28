
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

public class worker extends Thread {
	public commonInfo cInfo; 
	long sleepTime ;
	public int threadId;
	
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
	public void run(){
	       // Instantiate a Date object
	      Date date = new Date();
	      boolean found = false ;  
	      int i;
	      Text txtTaskStr = null ;
	      Text txtTaskId = null;
	      String response = "";
	      //Get the task from Queue
	      String inpStr = "<?xml version=\"1.0\"?> <request><task>sleep 200</task> </request>";
	      inpStr = "";
	      
	     fact1.setValidating(true);
	 	 fact1.setIgnoringElementContentWhitespace(true);
	 	 System.out.println("Worker " + threadId + " started");
	 	 try {
			build1 = fact1.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    while(true) { 
	    	// Once Started the thread should always be active;
	    	// Waiting for job to get queued 
	   try {
		  // System.out.println(threadId + " Waiting for task");
		   inpStr = cInfo.taskQ.take();
	   	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	   }
	   /*
	    while(found == false ) { 
	      if(cInfo.taskQ.size() > 0 ) {
	    	   //= taskQ.get(0);
	    	  try { 
	    		 
	    		  found = true ; 
	    	  }catch (IndexOutOfBoundsException | InterruptedException e ){
	    		  //e.printStackTrace();
	    	  }
	      }else {
	    	  try {
	    	     sleep(100);
	  		 	} catch (InterruptedException e) {
	  		 		// TODO Auto-generated catch block
	  		 		e.printStackTrace();
	  		 	}
	      }
	      
	      
	   }
	   */
	   found = false ; 
	   System.out.print("Worker = " + threadId + " Received task : Len " + inpStr.length() +" Task Name " + inpStr +"ABC" ); 
	   try {
		   	 ByteArrayInputStream astream = 
						 new ByteArrayInputStream(inpStr.getBytes());
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
	   System.out.println(threadId + " Actual sleep task here");
        // Actual sleep task here 
		try {
		    //System.out.print(date.toString());
			//System.out.println(" Wait Started for thread");
			sleep(sleepTime);
			//date = new Date() ;
			//System.out.print(date.toString());
			//System.out.println(" Wait Done ");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Job Done Send result to scheduler 
		response = "<response><task><taskid>" + txtTaskId.getData() + "</taskid><taskstr>" + txtTaskStr.getData() + "</taskstr><taskresult>1</taskresult></task></response>";
		System.out.println(threadId +" "  +response);
		cInfo.resultQ.add(response);
	}
	}
}
