package wordCount;

import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class findMap extends Thread   {
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

