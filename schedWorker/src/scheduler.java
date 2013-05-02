


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.sqs.AmazonSQS.*; 
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
public class scheduler extends Thread {
	public commonInfo cInfo;
	public Double  spotInstancePrice; 
	public String myAMIID;  
	public AmazonEC2 ec2;  
	public int getQueueSize() {
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("All");
		GetQueueAttributesRequest qAttrbuteR = new GetQueueAttributesRequest(cInfo.taskQueueUrl) ;
		qAttrbuteR .setAttributeNames(attributeNames);
		Map<String, String> attributes = cInfo.sqs.getQueueAttributes(qAttrbuteR).getAttributes();
		int messages = Integer.parseInt(attributes.get("ApproximateNumberOfMessages"));
		System.out.println("Queue Size = " + messages);
		return messages;
				
	}
	public ArrayList<String> launchWorkers(int workersToLaunch){ 
		// Create the AmazonEC2Client object so we can call various APIs.

		// Initializes a Spot Instance Request
    	RequestSpotInstancesRequest requestRequest = new RequestSpotInstancesRequest();
    	// Request 1 x t1.small instance with a bid price of $0.007 
    	requestRequest.setSpotPrice(spotInstancePrice.toString());
    	requestRequest.setInstanceCount(workersToLaunch);
    	// Setup the specifications of the launch. This includes the instance type (e.g. t1.micro)
    	// and the latest Amazon Linux AMI id available. Note, you should always use the latest
    	// Amazon Linux AMI id or another of your choosing.
    	LaunchSpecification launchSpecification = new LaunchSpecification();
    	launchSpecification.setImageId(myAMIID);
    	launchSpecification.setInstanceType("m1.small");
    	
    	// Add the security group to the request.
    	ArrayList<String> securityGroups = new ArrayList<String>();
    	securityGroups.add("pa4SG");
    	launchSpecification.setSecurityGroups(securityGroups);
    	
    	// Add the launch specifications to the request.
    	requestRequest.setLaunchSpecification(launchSpecification);
    	//============================================================================================//
    	//=========================== Getting the Request ID from the Request ========================//
    	//============================================================================================//

    	// Call the RequestSpotInstance API.
    	List<SpotInstanceRequest> requestResponses= null; 
    	try {
    		RequestSpotInstancesResult requestResult = ec2.requestSpotInstances(requestRequest);
    		requestResponses = requestResult.getSpotInstanceRequests();
    	}catch(AmazonServiceException e ) { 
    		System.out.println("Request spot instance failed Service Exception " + e.getMessage());
    	}catch(AmazonClientException e ) { 
    		System.out.println("Request spot instance failed " + e.getMessage());
    	}
    	

    	// Setup an arraylist to collect all of the request ids we want to watch hit the running
    	// state.
    	ArrayList<String> spotInstanceRequestIds = new ArrayList<String>();

    	// Add all of the request ids to the hashset, so we can determine when they hit the
    	// active state.
    
    	for (SpotInstanceRequest requestResponse : requestResponses) {
    		System.out.println("Created Spot Request: "+requestResponse.getSpotInstanceRequestId());
    		spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
    	}
    	
    	return  spotInstanceRequestIds;

	}
	
	public void initilizeAmazonEC2() {
		
		ec2 = new AmazonEC2Client(new ClasspathPropertiesFileCredentialsProvider());
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		ec2.setRegion(usEast1);
		
	}
	public void run(){
		int numJobs;
		int totalWorkers = 0 ;
		int activeWorkers = 0 ;
		int launchedWorkers = 0 ;
		int amazonKilled= 0;
		int iterminated = 0 ;
		int workerToLaunch;
		spotInstancePrice = 0.007;
		myAMIID = "ami-3fec7956";
		ArrayList <String> ec2RequestId = null;  
		initilizeAmazonEC2();
		while(true){
			numJobs = getQueueSize();
			if (totalWorkers < cInfo.maxRemoteWorkers && numJobs > 0 ) {
				// Find out how many workers need to be launched
				workerToLaunch = (numJobs - launchedWorkers) ;
				workerToLaunch  = workerToLaunch  < 0 ? 0 : workerToLaunch ;
				workerToLaunch = totalWorkers + workerToLaunch > cInfo.maxRemoteWorkers ? cInfo.maxRemoteWorkers - totalWorkers :workerToLaunch;
				
			}
			System.out.println("Number of jobs to launch " + numJobs);
			if(numJobs > 0 ) {
				ArrayList <String> freshRequests;  
				freshRequests = launchWorkers(numJobs);
				ec2RequestId.addAll(freshRequests);
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		

				
	}
}
