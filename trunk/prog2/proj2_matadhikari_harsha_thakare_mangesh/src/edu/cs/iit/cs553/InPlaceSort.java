package src.edu.cs.iit.cs553;
import java.util.List;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

class LineReader{
	static char buf[];
	int rEnd;
	int len;
	boolean fileExh;
	int BLOCK_SIZE;
	BufferedReader reader ;
	long fileSize;
	long progress;
	long diskTime ;
	long start, stop;
	LineReader(BufferedReader rdr, int BLOCK_SIZE){
		this.BLOCK_SIZE = BLOCK_SIZE;
		buf = new char[(int) (BLOCK_SIZE+1000)] ;
		reader = rdr;
		rEnd = 0;
		len = 0 ; 
		fileExh = false;
		progress = 0 ;
		diskTime = 0 ; 
	}
	public  void setLen(int l){
		len = l ;
		rEnd = 0;
	}
	public  boolean isNewLine(char c){
		//if (c == '\n' || c == '\r'){
		if (c == '\n' ){
			return true;                
		}else{
			return false;                
		}
	}
	public  String readLine(){
		String str = new  String ();
		Character ch;
		try {
			if (rEnd >= len) { 
				if(fileExh == true) { 
					return null;
				}else{
					start = System.nanoTime();
					len = reader.read (buf,0,(int)BLOCK_SIZE);
					rEnd = 0 ;
					if(len < 0 ) { 
						fileExh = true;
						return null;
					}else{
						Boolean nwGot = false;
						char c ;
						progress = progress + (long) len;
						// Get remaing chars in a line
						while ((progress <= fileSize) && (nwGot == false) ) {
							//if(isNewLine(c = (char)reader.read()) == false){
							if(((c = (char)reader.read()) != '\n')){
								buf[len] = c ; 
							}else{
								nwGot = true;
								/*
								if (c == '\r') {
									if (isNewLine(c = (char)reader.read())== false ) {
										System.out.println("\n does not follow \r");
									}
								}
								*/
							}

							len++;
							progress = progress + 1 ;
						}
					}
					stop = System.nanoTime();
					diskTime = diskTime + (stop - start) ; 

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		char c ;
		for(;rEnd<len;rEnd++){
			//if(isNewLine(c=buf[rEnd])){
			if(((c = buf[rEnd]) != '\n')){
				// This is last Char
				rEnd++;
				/*
				if(isNewLine(c=buf[rEnd])) {
					rEnd++;
				}
				if(str.length() < 2 ) {
					// Come here 
					int kil;
					kil = 0 ; 
					kil++;
				}
				*/

				return str;
			}else{
				ch = new Character (c);
				str = str.concat(ch.toString());
			}

		}
		return str;
	}
}

class BufWriter{
	int BLOCK_SIZE;
	String strBuf;
	int slen;
	BufferedWriter bWriter;
	long diskTime ;
	long start, stop;
	BufWriter(BufferedWriter bWriter,int BLOCK_SIZE){
		this.BLOCK_SIZE = BLOCK_SIZE ; 
		this.bWriter = bWriter; 
		slen = 0 ; 
		strBuf = new String();
		diskTime = 0 ; 
	}
	public void close(){
		try {
			start = System.nanoTime();
			bWriter.write(strBuf);
			bWriter.flush();
			bWriter.close();
			stop = System.nanoTime();
			diskTime = diskTime + (stop - start);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void write(String str){
		int l ;
		l = str.length();
		slen = slen + l;

		strBuf = strBuf.concat(str);
		strBuf = strBuf.concat("\n");
		if(slen > BLOCK_SIZE) { 
			try {
				start = System.nanoTime();
				bWriter.write(strBuf);
				bWriter.flush();
				stop  = System.nanoTime();
				diskTime = diskTime + (stop - start);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

public class InPlaceSort{
	static int len;
	static int rEnd;
	static int BLOCK_SIZE;
	static long MILLION = 1000000;
	static long BILLION = 1000000000;
	static long readTime ; 
	static long writeTime;
	public static void displayHelp(){
		System.out.println("Usage: java InPlaceSort [-h] [-m linesAtTime] inpFile outFile ");
		System.out.println("\t -m linesAtTime - This option is to control the use of memory");
		System.out.println("\t          The program processes only linesAtTime number of entries at a time.");
		System.out.println("\t -b blockSize, The chunk size in MB. The program reads blockSize * Million bytes at a time from file for processing");
		System.out.println("\t    e.g. -b 1 mean 1 MB chunk size ");
		System.out.println("\t -h Displays Help\n");
		System.out.println("\t inputFile  First filename is treated as input file");
		System.out.println("\t outputFile Second filename is treated as output file");  
	}

	public static boolean isNewLine(char c){
		//if (c == '\n' || c == '\r'){
		if (c == '\n' ){
			return true;                
		}else{
			return false;                
		}
	}
	public static void mergeFiles(String inpF1Name,String inpF2Name,String outFName){
		InputStream inputStream1 ;
		BufferedReader rdr1 ;
		InputStream inputStream2;
		BufferedReader rdr2 ;
		LineReader reader1;
		LineReader reader2;
		StringTokenizer stk1;
		StringTokenizer stk2; 
		String str1;
		String str2;
		String k1;
		String k2;
		int ln;
		int v2;
		int tkn1;
		int tkn2;
		int cmp;
		//String inpF1Name = "imdFile".concat(((Integer)s1).toString());
		//String inpF2Name = "imdFile".concat(((Integer)s2).toString());
		//String outFName = "imdFile".concat(((Integer)o1).toString());
		System.out.printf("Merging %s and %s to %s\n",inpF1Name,inpF2Name,outFName);
		PrintWriter fstream;
		long fileSize;
		ln = 0 ;
		try {
			inputStream1 = new FileInputStream(inpF1Name);
			rdr1 = new BufferedReader(new InputStreamReader( inputStream1));
			//StreamTokenizer tokens1 = new StreamTokenizer(reader1);

			inputStream2 = new FileInputStream(inpF2Name);
			rdr2 = new BufferedReader(new InputStreamReader( inputStream2));
			//StreamTokenizer tokens2 = new StreamTokenizer(reader2);
			fstream = new PrintWriter(outFName);
			FileWriter writer = new FileWriter(outFName);
			BufferedWriter bwr = new BufferedWriter(writer);

			BufWriter bWriter = new BufWriter(bwr,BLOCK_SIZE); 
			reader1 = new LineReader(rdr1,BLOCK_SIZE);
			reader2 = new LineReader(rdr2,BLOCK_SIZE);
			fileSize = ((FileInputStream) inputStream1).getChannel().size();
			reader1.fileSize = fileSize ;
			fileSize = ((FileInputStream) inputStream2).getChannel().size();
			reader2.fileSize = fileSize ;

			str1 = reader1.readLine();
			str2 = reader2.readLine();

			//tkn1 = tokens1.nextToken();
			//tkn2= tokens2.nextToken();


			while(true){
				ln++;
				//  	System.out.println(ln + " " + str1);
				//  	System.out.println(ln + " " + str2);
				//System.out.println(ln);
				if (str1 == null) { 
					// Write rest of file 2
					while(str2 != null){
						bWriter.write(str2);
						str2 = reader2.readLine();
					}
					break;
				}
				if(str2 == null){
					while(str1!= null){
						bWriter.write(str1);
						str1 = reader1.readLine();
					}
					break;
				}
				cmp = str1.compareTo(str2) ; 
				if(cmp<0){ 
					bWriter.write(str1);
					str1 = reader1.readLine();
				}else if(cmp>0){
					bWriter.write(str2);
					str2 = reader2.readLine();
				}else{
					bWriter.write(str2);
					bWriter.write(str2);
					str1 = reader1.readLine();
					str2 = reader2.readLine();
				}

			}
			rdr1.close();
			rdr2.close();
			readTime = readTime + reader1.diskTime ;
			readTime = readTime + reader2.diskTime ;
			fstream.close ();
			fstream = new PrintWriter(inpF1Name);
			fstream.close ();
			fstream = new PrintWriter(inpF2Name);
			fstream.close ();
			bWriter.close();
			writeTime = writeTime + bWriter.diskTime ;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		//System.out.println("Merge Done ");
	}
	// end of mergeFiles

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int numThreads =2; 
		int numEntries = 1000000;
		String str =  new String ();
		InputStream inputStream ;
		BufferedReader reader ;
		int idx = 0;
		int argsLen = 0 ;
		boolean isFirst = true ;
		boolean isSecond= true ;
		long start,stop;
		long sort_start,sort_stop;
		long fileSize;
		long sortTime;
		int numFiles ;
		String inpFName = "C:\\setup\\s1m.txt";//"C:\\personal\\IIT\\cs553\\prog2\\log3";//"/home/mangesh/a.out" ;//= args[0]; // "/home/mangesh/mt/cont.xml";
		String outFName = "C:\\personal\\IIT\\cs553\\prog2\\s_100m_sorted.txt";//"C:\\personal\\IIT\\cs553\\prog2\\log3_sorted.txt";//= args[1];
		BLOCK_SIZE = 10000000;
		rEnd = 0 ; 
		readTime = 0 ; 
		writeTime = 0 ; 
		sortTime = 0 ; 
		argsLen = args.length ;
		while(idx <argsLen ) {
			System.out.println("Argument " + args[idx]);
			str = args[idx];
			if(str.charAt(0) == '-'){
				// Start of some option 
				if(str.charAt(1) == 'h'){
					//help
					displayHelp();
					return ; 
				}else if(str.charAt(1) == 'm'){
					// Number of lines this program loads at time uses 
					numEntries = Integer.parseInt(args[idx+1]) ;
					idx++;
				}else if(str.charAt(1) == 'b'){
					// Number of Threads this program uses 
					BLOCK_SIZE =  (int)(Float.parseFloat(args[idx+1]) * MILLION );
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
			//return;
		}	

		System.out.printf("Starting sorting with \ninput Files = %s \n",inpFName);
		System.out.printf("input Files = %s \n",outFName);
		System.out.printf("Number of entries to process at a time = %d \n",numEntries);
		System.out.printf("Block Size  = %f MB\n",(float)BLOCK_SIZE / MILLION);
		start = System.nanoTime();
		try { 
			inputStream = new FileInputStream(inpFName);
			reader = new BufferedReader(new InputStreamReader( inputStream));
			fileSize = ((FileInputStream) inputStream).getChannel().size();

		}catch(IOException e ){	
			e.printStackTrace();
			displayHelp();
			return ;
		}

		List <String> strList = new ArrayList<String>();

		int rl=0;
		String wfName;
		Integer fidx = 0;
		LineReader lr;
		lr = new LineReader(reader,BLOCK_SIZE); 
		lr.fileSize = fileSize ; 
		str = lr.readLine();
		try {
			while(str != null){
				strList.add(str);
				rl++;
				if(rl >= numEntries){
					// System.out.printf("started sorting \n");
					sort_start = System.nanoTime();
					Collections.sort(strList);
					sort_stop = System.nanoTime();
					sortTime = sortTime + (sort_stop - sort_start);
					wfName = "ipsFile".concat(fidx.toString());
					fidx++;
					FileWriter writer = new FileWriter(wfName);
					BufferedWriter bWr = new BufferedWriter(writer);
					BufWriter bWriter = new BufWriter(bWr,BLOCK_SIZE); 
					for(String s:strList){
						bWriter.write(s);
					}
					bWriter.close();
					writeTime = writeTime + bWriter.diskTime ;
					strList.clear();
					rl =0;
				}
				str = lr.readLine();
				//System.out.printf("%s\n",str);
			}

			reader.close();
			readTime = readTime + lr.diskTime ;

			System.out.printf("started sorting \n");
			sort_start = System.nanoTime();
			Collections.sort(strList);
			sort_stop = System.nanoTime();
			sortTime = sortTime + (sort_stop - sort_start);
			if (fidx == 0 ){
				wfName = outFName; 
			}else{
				wfName = "ipsFile".concat(fidx.toString());
			}
			fidx++;
			FileWriter writer = new FileWriter(wfName);
			BufferedWriter bWr = new BufferedWriter(writer);
			BufWriter bWriter = new BufWriter(bWr,BLOCK_SIZE); 
			for(String s:strList){
				bWriter.write(s);
			}
			bWriter.close();
			writeTime = writeTime + bWriter.diskTime ;
			strList.clear();
			rl =0;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String src[];
		String dst[]; 
		int i;
		int level =0;
		src = new String[fidx];
		dst = new String[fidx];
		for (i = 0;i<fidx;i++){
			src[i] = "ipsFile".concat(((Integer)i).toString());
		}
		numFiles = fidx;


		while(numFiles > 1) {
			for(i=0;i<numFiles-1;i=i+2){
				dst[i/2] = "ipsFile".concat(((Integer)(fidx*(level+1)+(i/2))).toString());
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
		stop = System.nanoTime();
		System.out.println("\nSorting Completed ");
		System.out.println("Total Time Taken in Seconds = "+ (stop-start) / BILLION);
		System.out.printf("Time spend in disk reading in Seconds = %f \n", (float)(readTime) / BILLION);
		System.out.printf("Time spend in disk writing in Seconds = %f\n", (float)(writeTime) / BILLION);
		System.out.printf("Time spend in sorting in Seconds = %f\n", (float)(sortTime) / BILLION);
	}

}

