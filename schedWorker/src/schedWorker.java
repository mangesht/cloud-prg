import java.util.ArrayList;


public class schedWorker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int num_worker = 10;
		int i;
		ArrayList <String> taskQ = new ArrayList<String>();  
		worker w_node[] = new worker[num_worker];
		for(i=0;i<num_worker;i++){
			w_node[i] = new worker();
			w_node[i].taskQ = taskQ;
			w_node[i].start();
		}
		
		// Put a job in the queue randomly 

	}

}
