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
	public AmazonSQS sqs ;
	public String taskQueueUrl  ; 
	public String resultQueueUrl;
	public int maxRemoteWorkers;
	commonInfo () {
		remoteWorker = true;
		localWorkers = 0;
		serverPort = 9100;
		maxRemoteWorkers = 32 ; 
	}
}
