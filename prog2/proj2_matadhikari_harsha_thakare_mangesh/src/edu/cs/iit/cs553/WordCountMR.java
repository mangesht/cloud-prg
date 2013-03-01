package src.edu.cs.iit.cs553;

import java.io.*;
import java.util.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.util.*;

public class WordCountMR {

public static class WordCountMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable>
{
      private final static IntWritable one = new IntWritable(1);
      private Text word = new Text();
     
      public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException
      {
          String line = value.toString();
          StringTokenizer tokenizer = new StringTokenizer(line);
         
          while (tokenizer.hasMoreTokens())
          {
               word.set(tokenizer.nextToken());
               output.collect(word, one);
          }
       }
}


public static class WordCountReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable>
{
      public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException
      {
          int sum = 0;
          while (values.hasNext())
          {
               sum += values.next().get();
          }
          output.collect(key, new IntWritable(sum));
      }
}

public static void main(String[] args) throws Exception
{
      JobConf conf = new JobConf(WordCountMR.class);
      conf.setJobName("WordCountMR");

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
      FileInputFormat.addInputPath(conf, new Path(args[0]));
      FileOutputFormat.setOutputPath(conf, new Path(args[1]));

      JobClient.runJob(conf);
      return;
}
     
}
