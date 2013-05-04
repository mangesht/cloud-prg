import java.net.*;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

import com.amazonaws.services.sqs.AmazonSQS;


public class commonInfo {
	public BlockingQueue <String> taskQ;
	public BlockingQueue<String> resultQ ;  
	public InetAddress IPAddress; 
	public Integer port ;
	public int serverPort;
	public boolean remoteWorker;
	public int localWorkers; 
	public DatagramSocket serverSocket;
	public tcpServerSocket serverTCPSocket;
	public Socket acceptSocket;
	public worker w_node[];
	public final int local = 1;
	public final int remote = 2;
	public final int available = 1;
	public final int busy = 0;
	public int localAvailWorkerCount;
	public int remoteAvailWorkerCount;
	public AmazonSQS sqs ;
	public String taskQueueUrl  ; 
	public String resultQueueUrl;
	public int maxRemoteWorkers;
	public int schedMode; 
	public int maxTaskCount=2;	
	commonInfo () {
		/* Lets keeo remoteWorker as default false,
		 * otherwise it will always get priority over
		 * the command line specification of -lw.
		 */
		remoteWorker = false;
		localWorkers = 32;
		serverPort = 9876;
		maxRemoteWorkers = 32 ; 
		schedMode = 1; //0 - Normal schedule for controlling remote instance
					  //1  - Disabled for manual instances to do the job
					  // 2 Control given to cloudWatch
	}
}
