
public class watchDogTimer extends Thread {
	public commonInfo cInfo;
	public void run(){
		int idleCount = 0;  
		while(true) {
			System.out.println("Watch Dog Timer = " + idleCount + "Idle = " + cInfo.idle);
			if (cInfo.idle == true ) { 
				idleCount++;
				if (idleCount > cInfo.timeout) { 
					// It is time to kill yourself 
					// Find a way to suicide
					break;
				}
			}else{
				idleCount = 0 ;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
