package src.edu.cs.iit.cs553;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class ValuePair implements Comparable <ValuePair>{
	public String key; 
	public int value;
	ValuePair(){
		value = 0 ; 
	}
	ValuePair(String s1,int v1){
		this.key = s1;
		this.value = v1 ; 
	}
//	@Override
	public int compareTo(ValuePair arg0) {
		
		//
		return this.key.compareTo(arg0.key);
	}
}
class wcInfo {
public Map<String,Integer> map ;

public ArrayList  <Character > separators; 
public ArrayList  <Character > specials;
public int mapIdx;
	wcInfo(){
		map = new HashMap<String,Integer>();
		separators = new  ArrayList <Character> ();
		specials = new  ArrayList <Character> ();
		mapIdx = 0 ;
	}
}

class threadBuf{
	public char[] buf;
	threadBuf(){
		// 
	}
}
class findMap extends Thread   {
	// This class provides a thread that computes the key and value pair
long offset ;
long len;
BufferedReader reader;
Map<String,Integer> map ;
public int threadNum;

public char[] buf;
public ArrayList  <Character > separators; 
public ArrayList  <Character > specials;
	findMap(long offset){
		this.offset = offset ;
		separators = new  ArrayList <Character> ();
		specials = new  ArrayList <Character> ();
		 //map = new HashMap<String,Integer>();
		 threadNum = 0 ;
	}
	public boolean isSeparator(Character ch) {
		// Checks if the character is amongst user defined separators 
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
		int si;
		int ei;
		int strLen ;
		Integer mapCount;
		String str = new  String ();
		//System.out.printf("Running Thread no %d \n", threadNum);
			i=0;
			while (len > 0){
				c = buf[i++];
				ch = new Character (c);
				//System.out.printf("%s",ch.toString() );
				// Find if string is ends here 
				// 34 - Quote 
				// Check if the character is natural whitespace separator 
				// or used defined separator 
				if(Character.isWhitespace(ch) ||separators.contains(ch) ){
					//|| isSeparator(ch)
					//End of previous string
					//System.out.printf("%s \t",str );
					if (str.matches("") == false){
						//System.out.printf("%s \t",str );
						si = 0 ;
						//System.out.println("\nstr="+str);
						strLen = str.length() ;
						//System.out.println("\nstr="+str + "strlen = "+strLen);
						while(si<strLen-1){
							
							if (specials.contains(str.charAt(si))) {
								// Continue the loop
							} else {
								break; 
							}
							si++;
						}
						ei = strLen -1 ; 
						while(ei>=0){
							if (specials.contains(str.charAt(ei))) {
								// Continue the loop
							} else {
								break; 
							}
							ei--;
						}
                        //System.out.printf("\tsi = %d ei = %d \n",si,ei);
						
						if (si < ei+1){
							str = str.substring(si, ei+1);
							//System.out.println("\tafter str="+str);
							mapCount= map.get(str);
							if(mapCount !=null) {
								map.put(str, mapCount+1);
							}else{
								map.put(str, 1);
							}
						}
						
					}
					str = "";
				}else{
					str = str.concat(ch.toString());
				}
				len--;
			
			}
		//str.charAt(i)
			
			
			// For the last word 
			if (str.matches("") == false){
				//System.out.printf("%s \n",str );
				// Loop for removing leading and laging special chars
				si = 0 ;
				strLen = str.length() ;
				//System.out.println("\nstr="+str);
				while(si<strLen-1){
					if (specials.contains(str.charAt(si))) {
						// Continue the loop
					} else {
						break; 
					}
					si++;
				}
				ei = strLen -1 ; 
				while(ei>=0){
					if (specials.contains(str.charAt(ei))) {
						// Continue the loop
					} else {
						break; 
					}
					ei--;
				}
			
				
				if (si < ei+1){
					str = str.substring(si, ei+1);
					//System.out.println("\tafter str="+str);
					mapCount= map.get(str);
					if(mapCount !=null) {
						map.put(str, mapCount+1);
					}else{
						map.put(str, 1);
					}
				}
				
			}
		//System.out.printf("Done Running Thread no %d \n", threadNum);
	}
}

	public class WordCountJ {
		/**
		 * @param args
		 * 
		 */
		static List <ValuePair> vpList = new ArrayList<ValuePair>();
		static long  BILLION = 1000000000;
		static long  MILLION = 1000000;
		
		public static void displayHelp(){
			System.out.println("Usuage: java wordCount [-h]  [-nt numThreads] [-fm s/m fileMode] -w charValue -s charValue inputFile outputFile");
			System.out.println("\t -h Displays Help");
			System.out.println("\t -nt numThreads : Selectes the number of threads that program should use. Default value is 4 ");
			System.out.println("\t -fm Selects the way input is given. Default is single file");
			System.out.println("\t \t s means single file ");
			System.out.println("\t \t m means multiple files. Files would be taken in serial order ");
			System.out.println("\t -w charValue Specifies the ascii character that needs to treated \n\t    as separator  identified by integer charValue ");
			System.out.println("\n\t\t It is treates as white space. Ex. for double quote use -w 34  \n");
			System.out.println("\t -s charvalue specifies the special character that needs to removed \n\t\t occuring at begining or at end \n"); 
			System.out.println("\t inputFile  First filename is treated as input file");
			System.out.println("\t outputFile Second filename is treated as output file");
			
		}
		
		public static void outputMapToFile(Map<String,Integer> map, int idx){
			ValuePair vp;
		
			for(String s1 : map.keySet())	 {
				vp = new ValuePair(s1,map.get(s1));
				vpList.add(vp);
			}
			Collections.sort(vpList);
			// Write to a file
			String imdFileName = "imdFile".concat(((Integer)idx).toString());
			PrintWriter fstream;
			try {
				fstream = new PrintWriter(imdFileName);
		
			for(ValuePair y : vpList) { 
				fstream.printf("%s %d \n",y.key,y.value);
			}
			
			fstream.close ();
			} catch (FileNotFoundException e) {
			
				e.printStackTrace();
			}
			vpList.clear();
			System.out.printf("Writing done to file %s \n",imdFileName);
		}
		
		//public static void mergeFiles(int s1,int s2,int o1){
		public static void mergeFiles(String inpF1Name,String inpF2Name,String outFName){
			InputStream inputStream1 ;
			BufferedReader reader1 ;
			InputStream inputStream2;
			BufferedReader reader2 ;
			StringTokenizer stk1;
			StringTokenizer stk2; 
			String str1;
			String str2;
			String k1;
			String k2;
			int v1;
			int v2;
			int cmp;
			//String inpF1Name = "imdFile".concat(((Integer)s1).toString());
			//String inpF2Name = "imdFile".concat(((Integer)s2).toString());
			//String outFName = "imdFile".concat(((Integer)o1).toString());
			System.out.printf("Merging %s and %s to %s\n",inpF1Name,inpF2Name,outFName);
			PrintWriter fstream;
			try {
				inputStream1 = new FileInputStream(inpF1Name);
				reader1 = new BufferedReader(new InputStreamReader( inputStream1));
				//StreamTokenizer tokens1 = new StreamTokenizer(reader1);
				
				inputStream2 = new FileInputStream(inpF2Name);
				reader2 = new BufferedReader(new InputStreamReader( inputStream2));
				//StreamTokenizer tokens2 = new StreamTokenizer(reader2);
				fstream = new PrintWriter(outFName);
				
				str1 = reader1.readLine();
				str2 = reader2.readLine();
				
				//tkn1 = tokens1.nextToken();
				//tkn2= tokens2.nextToken();
				
				
				while(true){
					
					if (str1 == null) { 
						// Write rest of file 2
						while(str2 != null){
							stk2 = new StringTokenizer(str2," ");
							k2 = stk2.nextToken();
							v2 = Integer.parseInt(stk2.nextToken());
							fstream.printf("%s %d\n",k2,v2);
							str2 = reader2.readLine();
						}
						break;
					}
					if(str2 == null){
						while(str1!= null){
						   stk1 = new StringTokenizer(str1," ");
						   k1 = stk1.nextToken();
						   v1 = Integer.parseInt(stk1.nextToken());
						   fstream.printf("%s %d\n",k1,v1);
						   str1 = reader1.readLine();
						}
						break;
					}
					stk1 = new StringTokenizer(str1," ");
					k1 = stk1.nextToken();
					v1 = Integer.parseInt(stk1.nextToken());
					
					stk2 = new StringTokenizer(str2," ");
					k2 = stk2.nextToken();
					v2 = Integer.parseInt(stk2.nextToken());
					cmp = k1.compareTo(k2) ; 
					if(cmp<0){ 
						fstream.printf("%s %d\n",k1,v1);
						str1 = reader1.readLine();
					}else if(cmp>0){
						fstream.printf("%s %d\n",k2,v2);
						str2 = reader2.readLine();
					}else{
						fstream.printf("%s %d\n",k2,v2+v1);
						str1 = reader1.readLine();
						str2 = reader2.readLine();
					}
					/*
					if (tkn1 == StreamTokenizer.TT_EOF) {
						// Write rest of file 2
						while(tkn2 != StreamTokenizer.TT_EOF){
							fstream.printf("%s ",tokens2.sval);
							tkn2 = tokens2.nextToken();
							fstream.printf("%d\n",tokens2.nval);
							tkn2 = tokens2.nextToken();
						}
						break;
					}
					if (tkn2 == StreamTokenizer.TT_EOF) {
						// Write rest of file 1
						while(tkn1 != StreamTokenizer.TT_EOF){
							fstream.printf("%s ",tokens1.sval);
							tkn1 = tokens1.nextToken();
							fstream.printf("%d\n",tokens1.nval);
							tkn1 = tokens1.nextToken();
						}
						break;
					}
					if(tokens1.sval.compareTo(tokens2.sval)<0){
						fstream.printf("%s ",tokens1.sval);
						tkn1 = tokens1.nextToken();
						fstream.printf("%d\n",(int)tokens1.nval);
						tkn1 = tokens1.nextToken();
					}else if (tokens1.sval.compareTo(tokens2.sval)>0) {
						fstream.printf("%s ",tokens2.sval);
						tkn2 = tokens2.nextToken();
						fstream.printf("%d\n",(int)tokens2.nval);
						tkn2 = tokens2.nextToken();
					}else{
						// both are equal
						fstream.printf("%s ",tokens2.sval);
						tkn1 = tokens1.nextToken();
						tkn2 = tokens2.nextToken();
						fstream.printf("%d\n",(int)(tokens1.nval+tokens2.nval));
						tkn1 = tokens1.nextToken();
						tkn2 = tokens2.nextToken();
					}
					*/
				}
				reader1.close();
				reader2.close();
				fstream.close ();
				fstream = new PrintWriter(inpF1Name);
				fstream.close ();
				fstream = new PrintWriter(inpF2Name);
				fstream.close ();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e){
				e.printStackTrace();
			}
			
		}
		public static void main(String[] args) {
			// TODO Auto-generated method stub
			int numThreads =1; 
			InputStream inputStream ;
			BufferedReader reader ;
			String inpFName = "/home/mangesh/mt/cr1k";//"/home/mangesh/a.out" ;//= args[0]; // "/home/mangesh/mt/cont.xml";
			String outFName = "/home/mangesh/mt/b.txt";//= args[1];
			char  c ='a';
			int tNum = 0 ;
			long start,stop;
			long fileSize;
			long BLOCK_SIZE = 4096 ;
			//char []buf ;
			int fileMode = 1 ; 
			int idx = 0;
			int argsLen = 0 ;
			boolean isFirst = true ;
			boolean isSecond= true ;
			
			//int seps[]; 
			/*
			
			
			vp = new ValuePair();
			vp.key = "Rupesh";
			vp.value = 27;
			vpList.add(vp);

			
			vp = new ValuePair();
			vp.key = "Thakare";
			vp.value = 21;
			vpList.add(vp);
			
			vp = new ValuePair();
			vp.key = "Manegsh";
			vp.value = 23;
			vpList.add(vp);


			System.out.println("Before sorting");
			for(ValuePair y:vpList){
				System.out.printf("%s %d\n", y.key,y.value);
			}
			Collections.sort(vpList);
			System.out.println("After sorting");
			for(ValuePair y:vpList){
				System.out.printf("%s %d\n", y.key,y.value);
			}
			if(true) {
				return ;
			}
			*/
			ArrayList  <Character > separators = new  ArrayList <Character> ();
			ArrayList  <Character > specials = new  ArrayList <Character> ();
			String str =  new String ();
			
			//Map<String,Integer> map = new HashMap<String,Integer>();
			//---------------------------------------------
			// Get the input arguments here
			//---------------------------------------------
			argsLen = args.length ;
		//	args = new String[2];
		//	args[0] = "-s";
		//	args[1] = "45";
			specials.add((char)34); // Double quote 
			specials.add((char)63); // ? Question mark
			specials.add((char)60); // <
			//specials.add((char)61); // = 
			specials.add((char)62); // > 
			specials.add((char)46); // .
			specials.add((char)59); // ;
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
					}else if(str.charAt(1) == 'w'){
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
					}else if(str.charAt(1) == 's'){
						Integer  ws;
						try {
							ws = Integer.parseInt(args[idx+1]) ;
						}catch(NumberFormatException e ){
							e.printStackTrace();
							displayHelp();
							return ;
						}
						specials.add((char)ws.byteValue());
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
			    //displayHelp();
                //return;
			}
		    long MAPSIZE_THRESHOLD = (long) (1.0 * MILLION * 2 / numThreads);
			 System.out.println("Running with number of Threads  : " + numThreads );
			 System.out.println("Input File :"+ inpFName + "\t Outout File : "+outFName);
			 start = System.nanoTime();
			 // Initialisation 
			 findMap m_node[] = new findMap[8];
			 wcInfo mapNode[] = new wcInfo[8096];
			 threadBuf tBuf[] = new threadBuf[8];

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
			
			
			BLOCK_SIZE = (long) (fileSize / numThreads); 
			BLOCK_SIZE = (long) (BLOCK_SIZE > MILLION ? MILLION : BLOCK_SIZE); 
			//buf =  new char[(int) (BLOCK_SIZE+1000)] ; // [4000];
			//inputStream.available()	;
			int offset = 0;
			int len ;
			int absTnum = 0;
			int mapIdx = 0 ; 
			
			for(tNum = 0 ; tNum < numThreads;tNum++){
				
				mapNode[tNum] = new wcInfo();
				tBuf[tNum] = new threadBuf();
				//mapNode[tNum].buf =  new char[(int) (BLOCK_SIZE+1000)] ;
				tBuf[tNum].buf =  new char[(int) (BLOCK_SIZE+1000)] ;
				m_node[tNum] = new findMap(1);
				mapNode[tNum].mapIdx = mapIdx;
				
				for (Character sp : separators ){
					mapNode[tNum] .separators.add(sp);
				}
				for (Character sp : specials ){
					mapNode[tNum].specials.add(sp);
				}
				//m_node[tNum].threadNum = tNum; 
				mapIdx++;
			}
			System.out.printf("Using Block size = %d \n",BLOCK_SIZE);
			int vNum;
			long progress = 0;
			tNum = 0 ; 
			try { 
								
			while(true) {
				if(fileMode==1){ // Single filemode
					
					stop = System.nanoTime();
					//d = new Date(stop/1000000);
					System.out.print( (stop -start) / BILLION ); 
					System.out.printf(" Processed %d , Progress = %2.2f Thread = %d mapIdx = %d mapSize = %d \n", 
							progress,(float)((float) progress * 100)/fileSize,tNum, mapIdx, mapNode[mapNode[tNum].mapIdx].map.size());
					//System.out.printf(" Processed %d , Progress = %2.2f Thread = %d \n", 
					//				progress,(float)((float) progress * 100)/fileSize,tNum);
					 
				//	if((len = reader.read (mapNode[tNum].buf,offset,(int)BLOCK_SIZE)) <= 0) {
					if((len = reader.read (tBuf[tNum].buf,offset,(int)BLOCK_SIZE)) <= 0) {
						
						for(vNum = 0 ; vNum < numThreads && vNum < absTnum;vNum++){
							//System.out.printf("Waiting for last thread num = %d absTnum = %d \n",vNum,absTnum);
							m_node[vNum].join();
							outputMapToFile(mapNode[mapNode[vNum].mapIdx].map,mapNode[vNum].mapIdx);
							mapNode[mapNode[vNum].mapIdx].map.clear();
						}
						// Write all the last maps to File 
						
						break;
					}
					//System.out.printf("Reading for thread %d \n",tNum);
					if(len == BLOCK_SIZE) {
						//ch = new Character (c);
						int ec = 0;
						while(Character.isWhitespace(c = (char)reader.read()) == false ) {
							tBuf[tNum].buf[len] = c ; 
							len++;
							ec++;
							if (ec > 1000) {
								System.out.println("long string encountered");
								break;
							}
						}
					
					}else{
						//System.out.printf("Read less data = %d \n", len);
					}
					progress =  progress + (long)len ;
					m_node[tNum] = new findMap(1);
					//separators.addAll(m_node[tNum].separators);
					//m_node[tNum].separators = (ArrayList<Character>) separators.clone();
					//m_node[tNum].specials =  (ArrayList<Character>) specials.clone();
					m_node[tNum].specials = mapNode[tNum].specials ;
					m_node[tNum].separators  = mapNode[tNum].separators  ;
					//m_node[tNum].buf = buf.clone();
					//m_node[tNum].buf = mapNode[tNum].buf;
					m_node[tNum].buf = tBuf[tNum].buf;
					m_node[tNum].setOffsetLen(0, len);
				
					if(mapNode[mapNode[tNum].mapIdx].map.size()> MAPSIZE_THRESHOLD) {
					
						// for time being 
						
						// Drive this to array sort and save  
						outputMapToFile(mapNode[mapNode[tNum].mapIdx].map,mapNode[tNum].mapIdx);
						mapNode[mapNode[tNum].mapIdx].map.clear();
						
						
						mapNode[mapIdx] = new wcInfo();
					
						mapNode[tNum].mapIdx = mapIdx;
						//mapNode[tNum].mapIdx = tNum;
						mapIdx++;
						
					}
					
					m_node[tNum].map = mapNode[mapNode[tNum].mapIdx].map;
					//mapNode[tNum].mapIdx = tNum;
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

			// Implement merge sort for merging all the file outputs
			int numFiles = mapIdx;
			int i;
			int level = 0;
			String src[];
			String dst[]; 
			src = new String[mapIdx];
			dst = new String[mapIdx];
			for (i = 0;i<mapIdx;i++){
				src[i] = "imdFile".concat(((Integer)i).toString());
			}
			while(numFiles > 1) {
				for(i=0;i<numFiles-1;i=i+2){
					//mergeFiles(mapIdx*level+i,mapIdx*level+i+1,mapIdx*(level+1)+(i/2));
					dst[i/2] = "imdFile".concat(((Integer)(mapIdx*(level+1)+(i/2))).toString());
					if(numFiles == 2 ) {
						mergeFiles(src[i],src[i+1],outFName);
					}else{
						mergeFiles(src[i],src[i+1],dst[i/2]);
					}
				}
				dst[numFiles/2] = src[numFiles-1];
				numFiles = (numFiles + 1 )/2 ;
				src = dst.clone();
				level++;
			}
			/*
			for(tNum  = 1 ; tNum < mapIdx && tNum < absTnum;tNum++){
				for(String k : mapNode[tNum].map.keySet()){
					if(mapNode[0].map.get(k) !=null) {
						mapNode[0].map.put(k, mapNode[0].map.get(k)+mapNode[tNum].map.get(k));
					}else{
						mapNode[0].map.put(k, 1);
					}
				}
			}
			*/
			
			stop = System.nanoTime();
			/*
			try {
				PrintWriter fstream = new PrintWriter(outFName);
				//fstream.printf(format, args)
				//BufferedWriter out  = new BufferedWriter(fstream); 
				for(String k : mapNode[0].map.keySet()) { 
					fstream.printf("%s %d \n",k,mapNode[0].map.get(k));
				}
				
				fstream.close ();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			//System.out.printf("key = %s Val = %d \n",k,map.get(k));
			//System.out.printf("key = %s Val = %d \n","thakare",map.get("thakare"));
			
			System.out.printf("Time required = %2f seconds \n",(float) (stop-start)/BILLION);
		}

	}

