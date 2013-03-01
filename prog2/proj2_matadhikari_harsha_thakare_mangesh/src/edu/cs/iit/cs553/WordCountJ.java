package src.edu.cs.iit.cs553;
import javax.naming.Context; 
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;


class wcInfo {
public Map<String,Integer> map ;
	
	wcInfo(){
		 map = new HashMap<String,Integer>();
	}
}

class findMap extends Thread   {
long offset ;
long len;
BufferedReader reader;
Map<String,Integer> map ;
public int threadNum;

public char[] buf;
public ArrayList  <Character > separators; 

	findMap(long offset){
		this.offset = offset ;
		separators = new  ArrayList <Character> ();
		 //map = new HashMap<String,Integer>();
		 threadNum = 0 ;
	}
	public boolean isSeparator(Character ch) {
		for (Character c : separators ){
			if (c.equals(ch)) {
				return true;
			}
			
		}
		return false;
	}
	public void setReader(BufferedReader r){
		reader = r ; 
	}
	public void setOffsetLen(long o,long len){
		this.offset = o;
		this.len = len ; 
	}
	public void run(){
		Character ch;
		char c ;
		int i;
		String str = new  String ();
		//System.out.printf("Running Thread no %d \n", threadNum);
			i=0;
			while (len > 0){
				c = buf[i++];
				ch = new Character (c);
				//System.out.printf("%s",ch.toString() );
				// Find if string is ends here 
				// 34 - Quote 
				if(Character.isWhitespace(ch) ||separators.contains(ch) ){
					//|| isSeparator(ch)
					//End of previous string
					//System.out.printf("%s \t",str );
					if (str.matches("") == false){
						//System.out.printf("%s \n",str );
						if(map.get(str) !=null) {
							map.put(str, map.get(str)+1);
						}else{
							map.put(str, 1);
						}
					}
					str = "";
				}else{
					str = str.concat(ch.toString());
				}
				len--;
			}
			// For the last word 
			if (str.matches("") == false){
				//System.out.printf("%s \n",str );
				if(map.get(str) !=null) {
					map.put(str, map.get(str)+1);
				}else{
					map.put(str, 1);
				}
			}
		//System.out.printf("Done Running Thread no %d \n", threadNum);
	}
}

	public class WordCountJ {
		/**
		 * @param args
		 */
		static long  BILLION = 1000000000;
		static long  MILLION = 1000000;
		public static void displayHelp(){
			System.out.println("Usuage: java wordCount [-h]  [-nt numThreads] [-fm s/m fileMode] -s charValue inputFile outputFile");
			System.out.println("\t -h Displays Help");
			System.out.println("\t -nt numThreads : Selectes the number of threads that program should use. Default value is 4 ");
			System.out.println("\t -fm Selects the way input is given. Default is single file");
			System.out.println("\t \t s means single file ");
			System.out.println("\t \t m means multiple files. Files would be taken in serial order ");
			System.out.println("\t -s charValue Specifies the ascii character that needs to treated \n\t    as separator  identified by integer charValue");
			System.out.println("\t inputFile  First filename is treated as input file");
			System.out.println("\t outputFile Second filename is treated as output file");
			
		}
		public static void main(String[] args) {
			// TODO Auto-generated method stub
			int numThreads = 8 ; 
			InputStream inputStream ;
			BufferedReader reader ;
			String inpFName = "" ;//= args[0];
			String outFName = "";//= args[1];
			char  c ='a';
			int tNum = 0 ;
			long start,stop;
			long fileSize;
			int BLOCK_SIZE = 4096 ;
			char []buf ;
			int fileMode = 1 ; 
			int idx = 0;
			int argsLen = 0 ;
			boolean isFirst = true ;
			boolean isSecond= true ;
			//int seps[]; 
			
			ArrayList  <Character > separators = new  ArrayList <Character> ();
			
			String str =  new String ();
			//Map<String,Integer> map = new HashMap<String,Integer>();
			//---------------------------------------------
			// Get the input arguments here
			//---------------------------------------------
			argsLen = args.length ;
		//	args = new String[2];
		//	args[0] = "-s";
		//	args[1] = "45";
			
			while(idx <argsLen ) {
				System.out.println("Argument " + args[idx]);
				str = args[idx];
				if(str.charAt(0) == '-'){
					// Start of some option 
					if(str.charAt(1) == 'h'){
						//help
						displayHelp();
						return ; 
					}else if(str.charAt(1) == 'n'){
						// Number of Threads this program uses 
						numThreads = Integer.parseInt(args[idx+1]) ;
						idx++;
					}else if(str.charAt(1) == 'f'){
						// FileType multiple or single 
						if(args[idx+1].charAt(0) == 'm'){
							fileMode = 0 ; 
						}else{
							fileMode = 1 ;
						}
						idx++;
					}else if(str.charAt(1) == 's'){
						Integer  ws;
						try {
							ws = Integer.parseInt(args[idx+1]) ;
						}catch(NumberFormatException e ){
							e.printStackTrace();
							displayHelp();
							return ;
						}
						separators.add((char)ws.byteValue());
						idx++;
					}
				}else if(isFirst){
					isFirst = false ; 
					inpFName = args[idx];					
				}else if(isSecond){
					isSecond = false;
					outFName = args[idx];					
				}else{
					displayHelp();
					return ; 
				}
				idx++;
			}
		        if(isFirst || isSecond){
			    displayHelp();
                            return;
			}	
			 System.out.println("Running with number of Threads  : " + numThreads );
			 System.out.println("Input File :"+ inpFName + "\t Outout File : "+outFName);
			 start = System.nanoTime();
			 findMap m_node[] = new findMap[8];
			 wcInfo mapNode[] = new wcInfo[8];

			 //= new findMap(1);
			// fileSize = java.io.File.
			try { 
				inputStream = new FileInputStream(inpFName);
				reader = new BufferedReader(new InputStreamReader( inputStream));
				fileSize = ((FileInputStream) inputStream).getChannel().size();
			}catch(IOException e ){	
				e.printStackTrace();
				displayHelp();
				return ;
			}
			
			
			BLOCK_SIZE = (int) (fileSize / numThreads); 
			BLOCK_SIZE = (int) (BLOCK_SIZE > MILLION ? MILLION : BLOCK_SIZE); 
			buf =  new char[BLOCK_SIZE+1000] ; // [4000];
			//inputStream.available()	;
			int offset = 0;
			int len ;
			int absTnum = 0;
			
			
			for(tNum = 0 ; tNum < numThreads;tNum++){
				mapNode[tNum] = new wcInfo();
				//m_node[tNum] = new findMap(1);
				//m_node[tNum].threadNum = tNum; 
			}
			int vNum;
			tNum = 0 ; 
			try { 
			while(true) {
				if(fileMode==1){ // Single filemode 
					if((len = reader.read(buf,offset,BLOCK_SIZE)) <= 0) {
						for(vNum = 0 ; vNum < numThreads;vNum++){
							//System.out.printf("Waiting for last thread");
							m_node[vNum].join();
						}

						break;
					}
					//System.out.printf("Reading for thread %d \n",tNum);
					if(len == BLOCK_SIZE) {
						//ch = new Character (c);
						while(Character.isWhitespace(c = (char)reader.read()) == false ) {
							buf[len] = c ; 
							len++;
						}
					
					}else{
						//System.out.printf("Read less data = %d \n", len);
					}
					m_node[tNum] = new findMap(1);
					//separators.addAll(m_node[tNum].separators);
					m_node[tNum].separators = (ArrayList<Character>) separators.clone();
					m_node[tNum].buf = buf.clone();
					m_node[tNum].setOffsetLen(0, len);
					m_node[tNum].map = mapNode[tNum].map;
					m_node[tNum].threadNum = tNum;
		
				}

				
				m_node[tNum].start();
				//m_node[tNum].run();
				
				//new Thread (m_node).start();
				if(tNum == numThreads -1 ){
					
				 	tNum = 0 ; 
				}else{
					
					tNum++;
				}
				if(absTnum >= numThreads){
				try {	
					//System.out.printf("Waiting for thread");			
					m_node[tNum].join();
					//System.out.printf("Done Waiting for thread");
					//	m_node.			
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
			
				}
				}
				absTnum ++; 
				
			}

			}catch(IOException e ){	
				e.printStackTrace();
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			for(tNum  = 1 ; tNum < numThreads;tNum++){
				for(String k : m_node[tNum].map.keySet()){
					if(m_node[0].map.get(k) !=null) {
						m_node[0].map.put(k, m_node[0].map.get(k)+m_node[tNum].map.get(k));
					}else{
						m_node[0].map.put(k, 1);
					}
				}
			}
			stop = System.nanoTime();
			try {
				PrintWriter fstream = new PrintWriter(outFName);
				//fstream.printf(format, args)
				//BufferedWriter out  = new BufferedWriter(fstream); 
				for(String k : m_node[0].map.keySet())
					fstream.printf("%s %d \n",k,m_node[0].map.get(k));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.printf("key = %s Val = %d \n",k,map.get(k));
			//System.out.printf("key = %s Val = %d \n","thakare",map.get("thakare"));
			
			System.out.printf("Time required = %2f seconds \n",(float) (stop-start)/BILLION);
		}

	}

