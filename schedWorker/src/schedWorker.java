import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class schedWorker {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */

	static commonInfo cInfo; 
	
	static void displayHelp() { 
		
	}
	   public static  void bindDatagramSocket() {
		   try {
			   cInfo.serverSocket = new DatagramSocket(9810);
		   } catch (Exception error) {
				  System.err.println("Error in socket communication " + error.getMessage());
			
		   }
	   }
	   
	public static String makeJob(int tid, int t) { 
		String task = "<request><task><taskid>"+tid+"</taskid><taskstr>sleep "+ t +" </taskstr></task></request>";
		return task;
	}
	static String xmlInput = "<request><task><taskid>1</taskid><taskstr>sleep 1000</taskstr></task></request>";
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		//int localWorkers = 1;
		int i;
		int idx=0;
		int taskId = 0 ;
		String str;
		cInfo = new commonInfo();
		// Input Parsing 
		int argsLen = 0;
		argsLen = args.length ;
		
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
			 }
		}
		
		if(cInfo.remoteWorker == false && cInfo.localWorkers == 0 ){
			System.out.println("Quiting Since no worker is present ");
			return ;
			
		}
		String job = "<request><task><taskid>1</taskid><taskstr>sleep 1000</taskstr></task></request>";
		BlockingQueue <String> taskQ = new ArrayBlockingQueue<String>(1024);
		BlockingQueue <String> resultQ =   new ArrayBlockingQueue<String>(1024);

		
		bindDatagramSocket() ;
		cInfo.taskQ = taskQ; 
		cInfo.resultQ = resultQ; 
		worker w_node[] = new worker[cInfo.localWorkers];
		// Result Collector start
		resultCollector resCollector = new resultCollector();
		resCollector.cInfo = cInfo; 
		
		/*
		resCollector.resultQ = resultQ;
		resCollector.serverSocket = cInfo.serverSocket;
		resCollector.IPAddress = cInfo.IPAddress; 
		resCollector.port = cInfo.port;
		*/  
		resCollector.start();
		
		// Workers start 
		for(i=0;i<cInfo.localWorkers;i++){
			w_node[i] = new worker(i);
			w_node[i].cInfo = cInfo; 
			w_node[i].start();
		}
		// taskReceiver start
		taskReceiver server = new taskReceiver ();
		server.cInfo= cInfo;  
		/*
		server.taskQ = taskQ; 
		server.IPAddress = IPAddress;
		server.port = port;
		server.serverSocket = serverSocket;
		*/ 
		server.start(); 
		/*
		Thread.sleep(2000);
		// Put a job in the queue randomly
		taskQ.put(makeJob(taskId++,100));
		taskQ.add(makeJob(taskId++,200));
		Thread.sleep(200);
		taskQ.put(makeJob(taskId++,300));
		
		Thread.sleep(200);
		taskQ.put(makeJob(taskId++,400));
		Thread.sleep(200);
		taskQ.put(makeJob(taskId++,500));
		Thread.sleep(200);
		taskQ.put(makeJob(taskId++,600));
	 	*/ 
	 
		
	}

}
