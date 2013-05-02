import java.net.DatagramSocket;
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
		
	commonInfo () {
		remoteWorker = false;
		localWorkers = 1;
		serverPort = 9100;
		maxRemoteWorkers = 32 ; 
	}
}
