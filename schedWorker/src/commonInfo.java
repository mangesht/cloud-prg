import java.net.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.amazonaws.services.sqs.AmazonSQS;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


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
	public int maxTaskCount=1;
	public String myAMIID;
	public Double spotInstancePrice;
	public Lock lock ; //= new ReentrantLock();
    private  int MAX_AVAILABLE = 1;
	private  static Semaphore availableSema ; //= new Semaphore(MAX_AVAILABLE, true);

	
	commonInfo () {
		/* Lets keep remoteWorker as default false,
		 * otherwise it will always get priority over
		 * the command line specification of -lw.
		 */
		lock = new ReentrantLock();

		availableSema = new Semaphore(MAX_AVAILABLE, true);
		System.out.println("CI locking ");
		getLock();
		getUnlock();
		System.out.println("CI unlocking ");
		
		
		myAMIID = "ami-3f107c56";
		spotInstancePrice = 0.007;
		remoteWorker = true;
		localWorkers = 1;
		serverPort = 9100;
		maxRemoteWorkers = 32 ;
		
		schedMode = 1; //0 - Normal schedule for controlling remote instance
					  //1  - Disabled for manual instances to do the job
					  // 2 Control given to cloudWatch
	}
	public static void  getLock() {
		//lock.lock();
	     try {
			availableSema.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void  getUnlock(){
		
		//lock.unlock();
		availableSema.release();
	}
}
