import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
 
public class pa3test
{
	static String boundary;
	static OutputStream outputStream;
	static String LINE_FEED = "\r\n";
	static PrintWriter writer;
	static String charset = "UTF-8";
	static HttpURLConnection hConnection;
	  
	  
    public static void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }
    
    public static int addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();
 
        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        int length = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            length += bytesRead;
        }
        outputStream.flush();
        inputStream.close();
         
        writer.append(LINE_FEED);
        writer.flush(); 
        return length;
    }    
    
    public static List<String> finish(int footer) throws IOException {
        List<String> response = new ArrayList<String>();
 
        if (footer == 1)
        {
        	writer.append(LINE_FEED).flush();
        	writer.append("--" + boundary + "--").append(LINE_FEED);
        	writer.close();        	
        }
        else 
        {
        	writer.close();
        }
 
		// checks server's status code first
        int status = hConnection.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
            		hConnection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            hConnection.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
 
        return response;
    }
    
    public static void testInsert(String[] args) {

    	  long startTime, endTime, fsize;
		  boundary = "===" + System.currentTimeMillis() + "===";
		  
	      try
	      {
		      
		      
		      URL url = new URL( args[0] );
		      File uploadFile = new File(args[1]);
		      hConnection = (HttpURLConnection)
		                             url.openConnection();
		      HttpURLConnection.setFollowRedirects( true );
		 
		      hConnection.setDoOutput( true );
		      hConnection.setRequestMethod("POST");	
		      hConnection.setRequestProperty("Content-Type",
		              "multipart/form-data; boundary=" + boundary);
		      startTime  = System.currentTimeMillis();
		      outputStream = hConnection.getOutputStream();
		      writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
		              true);
    		  
		      addFormField("fun", "insert");
		      addFormField("file_size", "insert");
		      fsize = addFilePart("file_name", uploadFile);
		      
		      
		      List<String> response = finish(1);
		      endTime  = System.currentTimeMillis();
		      
		      //for (String line : response) {
		          //System.out.println(line);
		      //}
		      
		      System.out.println(args[1] + "," + fsize + "," + startTime + "," + endTime + "," + (endTime-startTime));
		      
		  } catch (IOException ex) {
		      System.out.println("ERROR: " + ex.getMessage());
		      ex.printStackTrace();
		  }    	
    }
    
    public static void testFind(String[] args) {
  	  	  long startTime, endTime;
		  boundary = "===" + System.currentTimeMillis() + "===";
		  
	      try
	      {
		      URL url = new URL( args[0] );
		      File uploadFile = new File(args[1]);
		      hConnection = (HttpURLConnection)
		                             url.openConnection();
		      HttpURLConnection.setFollowRedirects( true );
		 
		      hConnection.setDoOutput( true );
		      hConnection.setRequestMethod("POST");	
		      startTime  = System.currentTimeMillis();		      
		      outputStream = hConnection.getOutputStream();
		      writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
		              true);
		      writer.append("fun=find&file_name=" + args[1]);		      
		      
		      
		      int length = 0;
		      List<String> response = finish(0);
		      endTime  = System.currentTimeMillis();
		      for (String line : response) {
		    	  length += line.length();
		          //System.out.println(line);
		      }
		      
		      System.out.println(args[1] + "," + length + startTime + "," + endTime + "," + (endTime-startTime));
		      
		  } catch (IOException ex) {
		      System.out.println("ERROR: " + ex.getMessage());
		      ex.printStackTrace();
		  }    	
    }    
    
    public static void testRemove(String[] args) {
	  	  long startTime, endTime;
		  boundary = "===" + System.currentTimeMillis() + "===";
		  
	      try
	      {
		      
		      URL url = new URL( args[0] );
		      File uploadFile = new File(args[1]);
		      hConnection = (HttpURLConnection)
		                             url.openConnection();
		      HttpURLConnection.setFollowRedirects( true );
		 
		      hConnection.setDoOutput( true );
		      hConnection.setRequestMethod("POST");	
		      startTime  = System.currentTimeMillis();			      
		      outputStream = hConnection.getOutputStream();
		      writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
		              true);
		      writer.append("fun=remove&file_name=" + args[1]);	
		      
		      int length = 0;
		      List<String> response = finish(0);
		      endTime  = System.currentTimeMillis();		      
		      for (String line : response) {
		    	  length += line.length();
		          //System.out.println(line);
		      }
		      
		      System.out.println(args[1] + "," + startTime + "," + endTime + "," + (endTime-startTime));
		      
		  } catch (IOException ex) {
		      System.out.println("ERROR: " + ex.getMessage());
		      ex.printStackTrace();
		  }    	
  }        
    public static void main(String[] args)
    {
    	if (args[2].equals("insert")) {
    		testInsert(args);
    	} else if (args[2].equals("remove")) {
    		testRemove(args);
    	} else if (args[2].equals("find")) {
    		testFind(args);
    	}
 
    }
}