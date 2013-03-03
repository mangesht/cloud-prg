package src.edu.cs.iit.cs553;

import java.io.*;
import java.util.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.util.*;



public class WordCountMR {

public static class SpecialCharacterRemover {
   public static String specials = new String();

   public static void initialise()
   {
        SpecialCharacterRemover.specials += new String("\"?<>.;=");
   }

   public static void addSpecialChars(String special_str) {

        SpecialCharacterRemover.specials += special_str;
   }   

   public static String removeSpecialCharactersFromToken(String str) {
       int si, ei;
       int strLen;
       si = 0 ;
       if ((str == null) || (str.length() == 0)) {
	 System.out.println("Null string ");
         return str; 
       }

       if ((SpecialCharacterRemover.specials == null) || (SpecialCharacterRemover.specials.length() == 0)) { 
            //System.out.println("Null specials; initialising to default"); 
            SpecialCharacterRemover.initialise();
       }

       if ((SpecialCharacterRemover.specials == null) || (SpecialCharacterRemover.specials.length() == 0)) {
           // System.out.println("Null specials; returning"); 
            return str;
       }

       strLen = str.length();
       /* System.out.println("str= " + str+ " special size = "+SpecialCharacterRemover.specials.length() +
                             " specials=" + SpecialCharacterRemover.specials);
       */
       System.out.print("str= " + str );
       while(si < strLen-1){

          if (SpecialCharacterRemover.specials.indexOf(str.charAt(si)) > -1) {
            //Continue the loop
          } else {
           break; 
          }
          si++;
       }
       ei = strLen -1 ; 
       while(ei>=0){
         if (SpecialCharacterRemover.specials.indexOf(str.charAt(ei)) > -1) {
            //Continue the loop
         }
         else {
            break; 
         }
         ei--;
      }

      if (si < ei+1){
         str = str.substring(si, ei+1);
      }
      System.out.println(" | truncatedstr= " + str);
      return str;
  }
}

public static class WordCountMapper extends MapReduceBase
                                    implements Mapper<LongWritable, Text, Text, IntWritable>
{
      private final static IntWritable one = new IntWritable(1);
      private Text word = new Text();
      String tokenToAdd;
      public static String delimiters = new String(" ");

      public static void addDelimiters(String delimiters)
      {
         WordCountMapper.delimiters += delimiters;
      }

      public void map(LongWritable key,
                      Text value,
                      OutputCollector<Text, IntWritable> output,
                      Reporter reporter) throws IOException
      {
          String line = value.toString();
          
       	  if ((WordCountMapper.delimiters == null) || (WordCountMapper.delimiters.length() == 0)) { 
            //System.out.println("Null delimiters; reinitialising"); 
            WordCountMapper.delimiters +=  " \";<>=?\\";
          }
          /*System.out.println(" delimiter size = "+WordCountMapper.delimiters.length() +
                             " delimiters =" + WordCountMapper.delimiters);*/
          StringTokenizer tokenizer = new StringTokenizer(line, WordCountMapper.delimiters );
          while (tokenizer.hasMoreTokens())
          {
               //START-SPECIAL-PRUNING
               tokenToAdd = tokenizer.nextToken();               
               tokenToAdd = SpecialCharacterRemover.removeSpecialCharactersFromToken(tokenToAdd);               
               word.set(tokenToAdd);
               //END-SPECIAL-PRUNING              
               //word.set(tokenizer.nextToken());
               output.collect(word, one);
          }
       }
}


public static class WordCountReducer extends MapReduceBase
                                     implements Reducer<Text, IntWritable, Text, IntWritable>
{
      public void reduce(Text key,
                         Iterator<IntWritable> values,
                         OutputCollector<Text, IntWritable> output,
                         Reporter reporter) throws IOException
      {
          int sum = 0;
          while (values.hasNext())
          {
               sum += values.next().get();
          }
          output.collect(key, new IntWritable(sum));
      }
}

public static void displayHelp() {
    System.out.println("Usuage: java WordCountMR [-h] -s charValue inputFile outputFile");
    System.out.println("\t -h Displays Help");
    System.out.println("\t -s charvalue specifies a leading/trailing character that needs to be removed from the token \n");
    System.out.println("\t -w charvalue specifies delimiters to separate the words \n");
    System.out.println("\t inputFile  First filename is treated as input file");
    System.out.println("\t outputFile Second filename is treated as output file");
}

public static void main(String[] args) throws Exception
{
    int argsLen = 0;
    int idx = 0;
    String str =  new String ();
    boolean isFirst = true ;
    boolean isSecond= true ;
    String inpFName = "";
    String outFName = "";

    argsLen = args.length;

    if (argsLen < 2)
    {
       displayHelp();
       return ; 
    }

    while(idx < argsLen ) {
       str = args[idx];

       if(str.charAt(0) == '-'){
            if(str.charAt(1) == 'h'){
                //help
                displayHelp();
                return ; 
            } else if(str.charAt(1) == 's') {
       	       System.out.println("Special leading/trailing chars ="  + args[idx + 1]);
               SpecialCharacterRemover.addSpecialChars(args[idx+1]);//SPECIAL-PRUNING
               idx++;
            } else if(str.charAt(1) == 'w') {
       	       System.out.println("Delimiters ="  + args[idx + 1]);
               WordCountMapper.addDelimiters(args[idx+1]);//SPECIAL-PRUNING
               idx++;
            } else {
                displayHelp();
                return ; 
            }
	
       } else if(isFirst){
          isFirst = false; 
          inpFName = args[idx];
       } else if(isSecond){
          isSecond = false;
          outFName = args[idx];
       } else{
         displayHelp();
         return; 
      }
      idx++;
    }

    if ((isFirst) || (isSecond))
    {
        displayHelp();
        return ; 
    }

    JobConf conf = new JobConf(WordCountMR.class);
    conf.setJobName("WordCountMR");

    SpecialCharacterRemover.initialise();
    //Setting configuration object with the Data Type of output Key and Value
    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(IntWritable.class);

    //Providing the mapper and reducer class names
    conf.setMapperClass(WordCountMapper.class);
    conf.setReducerClass(WordCountReducer.class);
    conf.setCombinerClass(WordCountReducer.class);

    //OutputFormat
    conf.setInputFormat(TextInputFormat.class);
    conf.setOutputFormat(TextOutputFormat.class);

    //the hdfs input and output directory to be fetched from the command line
    FileInputFormat.addInputPath(conf, new Path(inpFName));
    FileOutputFormat.setOutputPath(conf, new Path(outFName));

    JobClient.runJob(conf);
    return;
}
     
}
