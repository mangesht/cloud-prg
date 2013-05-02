
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class schedWorker {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */

	static commonInfo cInfo; 
	
	static void displayHelp() { 
		System.out.println("Usage : " + 
				"java schedWorker -s <listening_port> " +
								 "-l <num_localworkers>" +
								 " [-r] ");
	}

	   public static void SQSInit(){
		 //  AWSCredentialsProvider awsCredentialsProvider = new ()
		   cInfo.sqs = new AmazonSQSClient(new ClasspathPropertiesFileCredentialsProvider());

		   cInfo.taskQueueUrl  = "https://sqs.us-east-1.amazonaws.com/571769354000/schedToWorker";
		   cInfo.resultQueueUrl = "https://sqs.us-east-1.amazonaws.com/571769354000/workerToSched";
	   }

    public static  void bindDatagramSocket() {
	   try {
		   cInfo.serverSocket = new DatagramSocket(cInfo.serverPort);
				   				
			System.out.println("Bind to datagram socket at port " + cInfo.serverPort);		   
	   } catch (Exception error) {
			  System.err.println("Error in socket communication " + error.getMessage());
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
				 }
				 idx++;
			 }
			 else {
				 idx++;
			 }
		}
		
		/*if(cInfo.serverPort == 0 ){
			System.err.println("Quiting Since no serverport not provided ");
			return false;
		}
		*/
		{
			System.out.println("Server Port =  " + cInfo.serverPort );
		}
		if(cInfo.remoteWorker == true && cInfo.localWorkers != 0 ){
			// You only need to support either local OR remote workers at one time. 
			// If both are specified, then the remote workers should be used - Ioan 
			
			cInfo.localWorkers = 0 ; 
		}
		if(cInfo.remoteWorker == false && cInfo.localWorkers == 0 ){
			System.out.println("Quiting Since no worker is present ");
			return false;
		}else {
			System.out.println("remoteWorker =  " + cInfo.remoteWorker +
						       "localworkers = " + cInfo.localWorkers);
		}
	
		return true;
    }
    
    public static void initialiseQueues() {
		BlockingQueue <String> taskQ = new ArrayBlockingQueue<String>(1024);
		BlockingQueue <String> resultQ =   new ArrayBlockingQueue<String>(1024);    
		cInfo.taskQ = taskQ; 
		cInfo.resultQ = resultQ;
		System.out.println("Queue initialised");
		return ;
    }
    
	public static  void main(String[] args) throws InterruptedException {
		boolean bRet = false;

		bRet = parseArgs(args);
		
		if (bRet == false) {
			System.out.println("Error in arguments");			
			return;
		}
		SQSInit();
		initialiseQueues();
		
		bindDatagramSocket() ;
 
		/* Start Result Collector Thread */
		resultCollector resCollector = new resultCollector();
		resCollector.cInfo = cInfo; 
		resCollector.start();
		
		/* Start Worker Threads */	
		worker w_node[] = new worker[cInfo.localWorkers];
		for(int i=0;i<cInfo.localWorkers;i++){
			w_node[i] = new worker(i);
			w_node[i].cInfo = cInfo; 
			w_node[i].start();
		}
		
		/* Start Request Receiver Thread */
		taskReceiver server = new taskReceiver ();
		server.cInfo= cInfo;  
		server.start();
		
	}
}
