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
	
	   public static  void bindDatagramSocket() {
		   try {
			   cInfo.serverSocket = new DatagramSocket(9877);
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
		int num_worker = 5;
		int i;
		int taskId = 0 ;
		cInfo = new commonInfo();
		String job = "<request><task><taskid>1</taskid><taskstr>sleep 1000</taskstr></task></request>";
		BlockingQueue <String> taskQ = new ArrayBlockingQueue<String>(1024);
		BlockingQueue <String> resultQ =   new ArrayBlockingQueue<String>(1024);

		bindDatagramSocket() ;
		cInfo.taskQ = taskQ; 
		cInfo.resultQ = resultQ; 
		worker w_node[] = new worker[num_worker];
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
		for(i=0;i<num_worker;i++){
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
