
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class scheduler {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */

	static commonInfo cInfo; 
	
	static void displayHelp() { 
		System.out.println("Usage : " + 
				"java schedWorker -s <listening_port> " +
								 "-l <num_localworkers>" +
								 " [-r] " + 
								 "[-m schedMode]");
		System.out.println();
	}

	public static void SQSInit(){
		   //AWSCredentialsProvider awsCredentialsProvider = new ()
		   cInfo.sqs = new AmazonSQSClient(new 
				   ClasspathPropertiesFileCredentialsProvider());

		   cInfo.taskQueueUrl  = "https://sqs.us-east-1.amazonaws.com/571769354000/schedToWorker";
		   cInfo.resultQueueUrl = "https://sqs.us-east-1.amazonaws.com/571769354000/workerToSched";
	}

    public static  void bindDatagramSocket() {
	   try {
		   cInfo.serverSocket = new DatagramSocket(cInfo.serverPort);
				   				
			System.out.println("Bind to datagram socket at port " + cInfo.serverPort);		   
	   } catch (Exception error) {
			System.err.println("Error in socket communication " + error.getMessage());
			cInfo.serverSocket.close();
	   }
    }
    
    public static  void bindTCPSocket() {
	   try {
		   cInfo.serverTCPSocket = new tcpServerSocket(cInfo.serverPort);
		   cInfo.serverTCPSocket.start();		   				
			System.out.println("Bind to TCP socket at port " + cInfo.serverPort);		   
	   } catch (Exception error) {
			System.err.println("Error in socket communication " + error.getMessage());
			cInfo.serverSocket.close();
	   }
    }
    

    public static boolean parseArgs(String[] args) {
		String str;    	
		// Input Parsing 
		int argsLen = 0;
		int idx=0;		
		argsLen = args.length ;
		
		cInfo = new commonInfo();
	
		while(idx < argsLen ) {
			str = args[idx];
			if(str.charAt(0) == '-'){
				 if(str.charAt(1) == 'h'){
					 //help
					 displayHelp();
					 return false;
				 }else if(str.charAt(1) == 's'){
					 // Server Port where client send tasks 
					 cInfo.serverPort  = Integer.parseInt(args[idx+1]) ;                                                 
					 idx++;
				 }else if(str.charAt(1) == 'l'){
					 // Number of Local workers 
					 cInfo.localWorkers= Integer.parseInt(args[idx+1]) ;                                                 
					 idx++;
				 }else if(str.charAt(1) == 'r'){
					 cInfo.remoteWorker = true; 
				 }else if(str.charAt(1) == 'm'){
					 // Mode of operation for scheduler 
					 cInfo.schedMode = Integer.parseInt(args[idx+1]) ;                                                 
					 idx++;
				 }
				 idx++;
			 }
			 else {
				 idx++;
			 }
		}
		
		if(cInfo.serverPort == 0 ){
			System.err.println("Quiting Since no serverport not provided ");
			return false;
		}
		else {
			System.out.println("Server Port =  " + cInfo.serverPort );
		}
		if(cInfo.remoteWorker == true && cInfo.localWorkers != 0 ){
			// You only need to support either local OR remote workers at one time. 
			// If both are specified, then the remote workers should be used - Ioan 
		    // This had been taken care in the inner-flow, however let this laos
			// be here - it becomes more clear
			 	
			cInfo.localWorkers = 0 ; 
		}
		if(cInfo.remoteWorker == false && cInfo.localWorkers == 0 ){
			System.out.println("Quiting Since no worker is present ");
			return false;
		}
		else {
			System.out.println("remoteWorker =  " + cInfo.remoteWorker +
						       "localworkers = " + cInfo.localWorkers);
		}
		System.out.println("Scheduler mode " + cInfo.schedMode);
		return true;
    }
    
    public static void initialiseQueues() {
		BlockingQueue <String> taskQ = new ArrayBlockingQueue<String>(1024);
		BlockingQueue<String> resultQ =   new ArrayBlockingQueue<String>(1024);    
		cInfo.taskQ = taskQ; 
		cInfo.resultQ = resultQ;
		System.out.println("Queue initialised");
		return ;
    }
    
	public static void main(String[] args) throws InterruptedException {
		boolean bRet = false;

		bRet = parseArgs(args);
		
		if (bRet == false) {
					
			return;
		}
		
		if (cInfo.remoteWorker == true) {		
			SQSInit();
		} 
		initialiseQueues();
		
		bindTCPSocket() ;
 
		/* Start Result Collector Thread */
		resultCollector resCollector = new resultCollector();
		resCollector.cInfo = cInfo; 
		resCollector.start();
		System.out.println("Result Collector Started ");
		/* Start Worker Threads */
		/* Moved worker to cinfo, as i need to have
		 * per worker statistics also there and this would be 
		 * used in taskReceiver ..
		 */	
		cInfo.w_node = new worker[cInfo.localWorkers];
		for(int i=0;i<cInfo.localWorkers;i++){
			cInfo.w_node[i] = new worker(i);
			cInfo.w_node[i].cInfo = cInfo;
			cInfo.w_node[i].type = cInfo.local;
			cInfo.w_node[i].status = cInfo.available;
			cInfo.w_node[i].start();
		}
		cInfo.localAvailWorkerCount = cInfo.localWorkers;
		
		/* Start Request Receiver Thread */
		taskReceiver server = new taskReceiver ();
		server.cInfo= cInfo;  
		server.start();
		
		if (cInfo.remoteWorker == true) {
			/* Start instance manager */ 
			instanceManager sched = new instanceManager();  
			sched.cInfo = cInfo; 
			sched.start();
		}
	}
}
