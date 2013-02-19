
import javax.naming.Context; 
import java.util.*;
import java.io.*;
public class hellow {

	/**
	 * @param args
	 */
	static long  BILLION = 1000000000;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int a ;
		Integer b ;
		
		StreamTokenizer tokens;
		String inpFName = "/home/mangesh/a.out";
		char  c ='a';
		Character ch;
		long start,stop;
		char buf[] = null;
		Map<String,Integer> map = new HashMap<String,Integer>();
		 start = System.nanoTime(); 
		try { 
		InputStream inputStream = new FileInputStream(inpFName);
		
	//	InputStreamReader isr = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(new InputStreamReader( inputStream));
		
	   
		tokens = new StreamTokenizer(reader);
		//tokens.resetSyntax();
		tokens.wordChars(48, 57);
		String str =  new String ();
		//str = str.concat("Mangesh");
		//System.out.printf("%s\n",str);
		//reader.r
		try {
			//reader.read(buf,1,4096);
		while ((byte)(c = (char)reader.read())!= -1){
			ch = new Character (c);
			//System.out.printf("%s",ch.toString() );
			// Find if string is ends here 
			// 34 - Quote 
			if(Character.isWhitespace(ch) || (byte) c == 34 ) {
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
		}
		}catch(IOException e ){
			// This means end of file 
		}
	//	tokens.parseNumbers();
/*		try {
			while(tokens.nextToken() != StreamTokenizer.TT_EOF) {
				if(tokens .ttype == StreamTokenizer.TT_WORD ) { 
					System.out.printf("%s \t",tokens.sval );	
				}else{
					System.out.printf("%f \t",tokens.nval );
				}
					
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		}catch(IOException e ){	
			e.printStackTrace();
		}
		
		/*map.put("name", 1);
		map.put("raju", 57);
		map.put("mangesh", 25);
		System.out.printf("Hello world = %d \n",map.get("mangesh"));
		*/
		
		for(String k : map.keySet())
			System.out.printf("%s %d \n",k,map.get(k));
		//System.out.printf("key = %s Val = %d \n",k,map.get(k));
		//System.out.printf("key = %s Val = %d \n","thakare",map.get("thakare"));
		stop = System.nanoTime();
		System.out.printf("Time required = %2f seconds ",(float) (stop-start)/BILLION);
	}

}
