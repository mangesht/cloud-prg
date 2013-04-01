
public class pa3Test {

	/**
	 * @param args
	 */
	public static void displayHelp() {
		// -f fileName -u url [-n numThreads] -c command
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 int argsLen = 0 ;
		 int idx = 0;
		 int numThreads = 4 ; 
		 int fIdx = 0 ;
		 String url = ""; 
		 String files[];
		 String runCmd  = "";
		 //runTests tests[];
		 String str =  new String ();
		 argsLen = args.length ;
		 files = new String [5];
		 
		 while(idx <argsLen ) {
             //System.out.println("Argument " + args[idx]);
             str = args[idx];
             if(str.charAt(0) == '-'){
            	 if(str.charAt(1) == 'h'){
                     //help
                     displayHelp();
                     return ; 
                 }else if(str.charAt(1) == 'n'){
                     // Number of Threads this program uses
                     numThreads = Integer.parseInt(args[idx+1]) ;
                     idx++;
                 }else if(str.charAt(1) == 'c'){
                	 // Command to run 
                	 runCmd = args[idx+1];
                	 idx++;
                 }else if(str.charAt(1) == 'f'){
                	 // FileName to process
                	 if(fIdx > 3 ) { 
                		 System.out .println("Only upto 4 thread supports"); 
                		 displayHelp();
                		 return; 
                	 }
                	 files[fIdx] = args[idx+1]; 
                	 fIdx++;
                	 idx++;
                 }else if(str.charAt(1) == 'u'){
                	 // url 
                	 url =args[idx+1];
                	 idx++;
                 }
             }
             idx++;
		 }
		 /*
		 files[0] = "I:\\frmZapOne\\rand_dataset\\d1k\\1iAiDAxeGh";
		 files[1] = "I:\\frmZapOne\\rand_dataset\\d1k\\2FHOWzii13";
		 files[2] = "I:\\frmZapOne\\rand_dataset\\d1k\\3ricHtAmll";
		 files[3] = "I:\\frmZapOne\\rand_dataset\\d1k\\4hyVqw9mAM";
		 
		 numThreads = 4 ; 
		 url = "http://pa3-cs553-jsp.appspot.com/fops_cloudstore";
		 runCmd = "insert";
		  */
		 runTests tests[] = new runTests[numThreads+1];
		 //numThreads = 2 ; 
		 // set tests parameters 
		 for (idx=0;idx<numThreads ;idx++) {
			 
			 tests[idx] = new runTests() ; 
			 System.out.println("fileName = " + files[idx] );
			 tests[idx].setUrl(url);
			 tests[idx].fileName = files[idx];
			 tests[idx].cmd = runCmd ;
		 }
		 for (idx=0;idx<numThreads ;idx++) {
			 tests[idx].start(); 
		 }
		 for (idx=0;idx<numThreads ;idx++) {
			 try {
				tests[idx].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		 }
		
	}

}
