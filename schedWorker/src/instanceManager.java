


import java.net.UnknownHostException;
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
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.InstanceAttribute;
import com.amazonaws.services.ec2.model.InstanceAttributeName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;

import com.amazonaws.services.sqs.AmazonSQS.*; 
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;

public class instanceManager extends Thread {
	public commonInfo cInfo;
	
	public AmazonEC2 ec2;
	public List<InstanceInfo> instanceInfo;
	ArrayList<String> activeInstanceIds;
	public int launchedCount; 
	public int activeCount;
	int lastActiveInstanceCount; 
	int totalWorkers ;
	ArrayList <String> ec2RequestId = new ArrayList<String>(); 
	instanceManager(){
		instanceInfo = new ArrayList<InstanceInfo>();
		activeInstanceIds= new ArrayList<String>();
		launchedCount = 0; 
		activeCount = 0;
		lastActiveInstanceCount = 0 ;
		totalWorkers = 0;
	}
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
		ArrayList<String> spotInstanceRequestIds = new ArrayList<String>();
		// Initializes a Spot Instance Request
    	RequestSpotInstancesRequest requestRequest = new RequestSpotInstancesRequest();
    	// Request 1 x t1.small instance with a bid price of $0.007 
    	requestRequest.setSpotPrice(cInfo.spotInstancePrice.toString());
    	requestRequest.setInstanceCount(workersToLaunch);
    	
    	// Setup the specifications of the launch. This includes the instance type (e.g. t1.micro)
    	// and the latest Amazon Linux AMI id available. Note, you should always use the latest
    	// Amazon Linux AMI id or another of your choosing.
    	LaunchSpecification launchSpecification = new LaunchSpecification();
    	launchSpecification.setImageId(cInfo.myAMIID);
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
    		return spotInstanceRequestIds;
    	}catch(AmazonClientException e ) { 
    		System.out.println("Request spot instance failed " + e.getMessage());
    		return spotInstanceRequestIds; 
    	}
    	ModifyInstanceAttributeRequest m = new ModifyInstanceAttributeRequest(); 
    	m.withInstanceInitiatedShutdownBehavior("terminate");		

    	// Setup an arraylist to collect all of the request ids we want to watch hit the running
    	// state.
    	

    	// Add all of the request ids to the hashset, so we can determine when they hit the
    	// active state.
    
    	for (SpotInstanceRequest requestResponse : requestResponses) {
    		System.out.println("Created Spot Request: "+requestResponse.getSpotInstanceRequestId());
    		spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
    	}
    	
    	return  spotInstanceRequestIds;

	}
	
	public void monitorAndCancelRequest(){
    	//============================================================================================//
    	//=========================== Determining the State of the Spot Request ======================//
    	//============================================================================================//

        // Create a variable that will track whether there are any requests still in the open state.
	    boolean anyOpen;

	    // Initialize variables.
	   
	    ArrayList<String> successfulRequests = new ArrayList<String>();

	   
	        // Create the describeRequest with tall of the request id to monitor (e.g. that we started).
	        DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
	        describeRequest.setSpotInstanceRequestIds(ec2RequestId);
	        System.out.print("Request Ids ");
	        for(String e :ec2RequestId) { 
	        	System.out.print( " " + e); 
	        	
	        }
	        System.out.println();
	        // Initialize the anyOpen variable to false ??? which assumes there are no requests open unless
	        // we find one that is still open.
	        anyOpen=false;

	    	try {
	    		List<SpotInstanceRequest> describeResponses = new ArrayList<SpotInstanceRequest>(); 
	    		// Retrieve all of the requests we want to monitor.
	    		if(ec2RequestId.size() > 0 ) { 
	    			DescribeSpotInstanceRequestsResult describeResult = ec2.describeSpotInstanceRequests(describeRequest);
	    			describeResponses = describeResult.getSpotInstanceRequests();
	    		}

	            // Look through each request and determine if they are all in the active state.
	            for (SpotInstanceRequest describeResponse : describeResponses) {
	            		// If the state is open, it hasn't changed since we attempted to request it.
	            		// There is the potential for it to transition almost immediately to closed or
	            		// cancelled so we compare against open instead of active.
	            	    //System.out.println("Status = " + describeResponse.getStatus());
	            		if (describeResponse.getState().equals("active")) {
	            			anyOpen = true;
	            			//break;
	            		

	            		// Add the instance id to the list we will eventually terminate.
	            		successfulRequests.add(describeResponse.getSpotInstanceRequestId());
	            		
	            		if( describeResponse.getInstanceId() != null) {
	            			System.out.println("Adding " + describeResponse.getInstanceId()) ;
	            			activeInstanceIds.add(describeResponse.getInstanceId());
	            		}
	            		}
	            }
	    	} catch (AmazonServiceException e) {
	            // If we have an exception, ensure we don't break out of the loop.
	    		// This prevents the scenario where there was blip on the wire.
	    		anyOpen = true;
	        }
	    	
	    	// Do the request cancellation for successful IDs

	    	//============================================================================================//
	    	//====================================== Canceling the Request ==============================//
	    	//============================================================================================//
	    	if(successfulRequests.size() > 0) { 
	        try {
	        	// Cancel requests.
	        	CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(successfulRequests);
	        	ec2.cancelSpotInstanceRequests(cancelRequest);
	        } catch (AmazonServiceException e) {
	    	 // Write out any exceptions that may have occurred.
	            System.out.println("Error cancelling instances");
	            System.out.println("Caught Exception: " + e.getMessage());
	            System.out.println("Reponse Status Code: " + e.getStatusCode());
	            System.out.println("Error Code: " + e.getErrorCode());
	            System.out.println("Request ID: " + e.getRequestId());
	        }
	    	
	        for(String request : successfulRequests) { 
	        	ec2RequestId.remove(request);
	        	launchedCount-- ; 
	        }
	        
	    	}
	    	System.out.println("Monitor and cancel ends here");
	}
	
	public void getInstanceHealthInfo(){ 
		DescribeInstanceAttributeRequest dr = new DescribeInstanceAttributeRequest ();
		
		DescribeInstanceStatusRequest instStatusRequest = new DescribeInstanceStatusRequest ();
		
		System.out.println("Getting Health");
		for(String s : activeInstanceIds) { 
			System.out.println("Getting Health for " + s );
			
		}
		List<InstanceStatus> stats = new ArrayList<InstanceStatus>();
		if(activeInstanceIds.size() > 0) { 
			instStatusRequest.setInstanceIds(activeInstanceIds);
			DescribeInstanceStatusResult res =  ec2.describeInstanceStatus();
			stats = res.getInstanceStatuses();
		}
		ArrayList<String> localActiveInstanceIds = new ArrayList<String> () ; 
		for (InstanceStatus s : stats) { 
			System.out.println("Instance Id  = " + s.getInstanceId() +" Status " + s.getInstanceStatus() + "state " + s.getInstanceState());
			// Valid values: 0 (pending) | 16 (running) | 32 (shutting-down) | 48 (terminated) | 64 (stopping) | 80 (stopped)

			if(s.getInstanceState().getCode() <= 16) { 
				localActiveInstanceIds.add(s.getInstanceId());
			}
		}
		/* Clean up the terminated instances */ 
		 
		ArrayList<String> localDeadInstanceIds = new ArrayList<String> () ;
		 for(String s : activeInstanceIds) { 
			 if(localActiveInstanceIds.contains(s)) { 
				 // It is running instance 
			 }else { 
				 // It is dead instance 
				 localDeadInstanceIds.add(s);
			 }
		 }
		 int deadInstanceCount = localDeadInstanceIds.size();
		 
		 activeInstanceIds.removeAll(localDeadInstanceIds);
		 
		 activeCount = activeInstanceIds.size() ; 
		 //launchedCount -= activeCount - lastActiveInstanceCount + deadInstanceCount ; 
		 launchedCount = launchedCount < 0 ? 0 : launchedCount; // Protection against malicious acts 
		 System.out.println("Launched " + launchedCount + " Active " + activeCount +
				 " LastActive " + lastActiveInstanceCount + " Dead Now " +deadInstanceCount );
		 System.out.println("Launch Request size " + ec2RequestId.size());
		 lastActiveInstanceCount =  activeCount;
		 totalWorkers = launchedCount + activeCount;
		/*
		for(String instance : activeInstanceIds) { 
			if(instance != null) { 
			System.out.println("Getting health for instance = " + instance);
			dr.setInstanceId(instance);
			
			dr.setAttribute("userData");
			//i was here
			DescribeInstanceAttributeResult instHealthResult = null;
			try { 
				 instHealthResult = ec2.describeInstanceAttribute(dr);
			}catch(AmazonServiceException e) { 
				 System.out.println("Getting Health error " + e.getMessage());
			}
			InstanceAttribute iAt = instHealthResult.getInstanceAttribute();
			
			System.out.println(" Instance Status "+ iAt.toString());
			System.out.println(" User Data "+iAt.getUserData() + iAt.getInstanceInitiatedShutdownBehavior());
			System.out.println(" " + iAt.getInstanceType());
			
			}
		 
		
		}
		*/
		
	}
	public void initilizeAmazonEC2() {
		
		Region usEast1 ;
		ec2 = new AmazonEC2Client(new ClasspathPropertiesFileCredentialsProvider());
			
		usEast1 = Region.getRegion(Regions.US_EAST_1);
		
		ec2.setRegion(usEast1);
		
		
	}
	public void run(){
		int numJobs;

		//int activeWorkers = 0 ;
		
		int amazonKilled= 0;
		int iterminated = 0 ;
		int workerToLaunch = 0;
		int tCount = 0 ; 
		
		
			
		initilizeAmazonEC2();
		/*
		// bewakuf 
		//i-7257f71c
		activeInstanceIds.add("i-7257f71c");
		activeInstanceIds.add("i-f660fa93");
		lastActiveInstanceCount = 2 ;
		totalWorkers = 2  ;   
		launchedCount = 0 ;
		*/  
		while(true && cInfo.schedMode == 0){
			numJobs = getQueueSize();
			workerToLaunch = 0 ; 
			if (totalWorkers < cInfo.maxRemoteWorkers && numJobs > 0 ) {
				// Find out how many workers need to be launched
				workerToLaunch = (numJobs - launchedCount) ;
				workerToLaunch  = workerToLaunch  < 0 ? 0 : workerToLaunch ;
				workerToLaunch = totalWorkers + workerToLaunch > cInfo.maxRemoteWorkers ? cInfo.maxRemoteWorkers - totalWorkers :workerToLaunch;
				
			}
			System.out.println("Number of jobs to launch " + workerToLaunch);
			if(workerToLaunch > 0 ) {
				ArrayList <String> freshRequests;
				
				freshRequests = launchWorkers(workerToLaunch);
				System.out.println("Fresh reqest size " + freshRequests.size());
				launchedCount += freshRequests.size();
				totalWorkers = launchedCount + activeCount;  
				ec2RequestId.addAll(freshRequests);
			}
			try {
				sleep(1000*5);
				tCount++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			if (tCount % 5 == 0 ){
				// Cancel the request when instance is started i.e. Open
				monitorAndCancelRequest();
			}
			getInstanceHealthInfo();
			
		} // end of while true 

	} // end of Run
}
