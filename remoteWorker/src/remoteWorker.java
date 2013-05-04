
import java.util.List;
import java.util.Map.Entry;

import java.io.*;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class remoteWorker {

	/**
	 * @param args
	 */
	static commonInfo cInfo;
	static String taskResponseXML="";


	public static void SQSInit(){
		//  AWSCredentialsProvider awsCredentialsProvider = new ()
		cInfo.sqs = new AmazonSQSClient(new ClasspathPropertiesFileCredentialsProvider());

		cInfo.taskQueueUrl  = "https://sqs.us-east-1.amazonaws.com/571769354000/schedToWorker";
		cInfo.resultQueueUrl = "https://sqs.us-east-1.amazonaws.com/571769354000/workerToSched";
	}

	static void  displayHelp(){ 
		System.out.println("");
		System.out.println("Usage p4client [-h] -i Timeout");
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

				 }else if(str.charAt(1) == 'i'){
					 // Timeout 
					 cInfo.timeout  = Integer.parseInt(args[idx+1]) ;                                                 
					 idx++;
				 }
				 idx++;
			 }
			 else {
				 idx++;
			 }
		}
		

		return true;
    }
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
		cInfo = new commonInfo();
		parseArgs(args);
		
		watchDogTimer wDogT;
		SQSInit();

		wDogT = new watchDogTimer(); 
		wDogT.cInfo = cInfo;
		executor exe = new executor();
		exe.cInfo = cInfo;
		exe.start();
		System.out.println("Started remote worker with Timeout " + cInfo.timeout );
		// Start watchDog Thread
	 
		wDogT.start();
		wDogT.join(); 
		System.out.println("Remote worker going down...");
		exe.stop();
		return ; 

		}
	}
