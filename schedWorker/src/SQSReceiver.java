import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;


public class SQSReceiver extends Thread {
	public commonInfo cInfo;
	public resultCollector rcHandle;
	public Semaphore available; //= new Semaphore(MAX_AVAILABLE, true);
	
	SQSReceiver() { 
		available = new Semaphore(1, true);
	}
	
	public void sqsReceive() { 
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(cInfo.resultQueueUrl);
		receiveMessageRequest.setMaxNumberOfMessages(10);
		receiveMessageRequest.setWaitTimeSeconds(20);
		List<String> strs = new ArrayList<String>();
		List<Message> messages = new ArrayList<Message> () ;
		String messageRecieptHandle ;
		 
		while(true) { 
				messages.clear();	
				System.out.println("Waiting for result from remote");
										
				messages = cInfo.sqs.receiveMessage(receiveMessageRequest).getMessages();
				System.out.println("Done Waiting for result from remote");
						
			if (messages != null  ) {
				System.out.println("Result Queue Size " + messages.size());
				
				if (messages.size() > 0 ) {
					System.out.println("SR Locking ");
					
					cInfo.getLock();
					try {
						this.rcHandle.available.acquire();
						this.available.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("SR IN ");
					//
					for(Message m : messages ) {
						//System.out.println("Message body " + m.getBody());
						//System.out.println("    ReceiptHandle: " + m.getReceiptHandle());
						//strs.add(m.getBody()) ;// = str.concat(m.getBody());
						//System.out.println("Adding to result Q") ;
						
						cInfo.resultQ.add(m.getBody());
						// Now delete the message 
						messageRecieptHandle = m.getReceiptHandle();
	    				cInfo.sqs.deleteMessage(new DeleteMessageRequest(cInfo.resultQueueUrl , messageRecieptHandle));
					}
					System.out.println("SR UnLocking ");
					cInfo.getUnlock();
					this.available.release();
					this.rcHandle.available.release();
					System.out.println("SR Out  ");
				}
				
			} else 
			
			{
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	public void run() {
		sqsReceive();
	}
}
