import java.util.ArrayList;
import java.util.Date;


public class worker extends Thread {
	long sleepTime ;
	public ArrayList <String> taskQ;
	worker(){
		sleepTime = 5000;
	}
	public void run(){
	       // Instantiate a Date object
	       Date date = new Date();
	        
	       // display time and date using toString()
	      
		try {
			 System.out.print(date.toString());
			System.out.println(" Wait Started for thread");
			sleep(sleepTime);
			date = new Date() ;
			System.out.print(date.toString());
			System.out.println(" Wait Done ");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
