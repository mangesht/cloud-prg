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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class runTests extends Thread {
	
	static String boundary;
	static OutputStream outputStream;
	static String LINE_FEED = "\r\n";
	PrintWriter writer;
	static String charset = "UTF-8";
	HttpURLConnection hConnection;
	
	// User should set these parameters
	public URL url;
	public String cmd;
	public String fileName;
	
	public void setUrl(String inUrl) {
		try {
			url = new URL( inUrl );
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }
    
    public  int addFilePart(String fieldName, File uploadFile)
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
    
    public void testInsert() {

  	  long startTime, endTime, fsize;
		  boundary = "===" + System.currentTimeMillis() + "===";
		  
	      try
	      {
		      	      
		      //URL url = new URL( args[0]);
		      File uploadFile = new File(fileName);
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
		      
		      System.out.println(fileName + "," + fsize + "," + startTime + "," + endTime + "," + (endTime-startTime));
		      
		  } catch (IOException ex) {
		      System.out.println("ERROR: " + ex.getMessage());
		      ex.printStackTrace();
		  }    	
  }
    public  List<String> finish(int footer) throws IOException {
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
    
    public  void testRemove() {
	  	  long startTime, endTime;
		  boundary = "===" + System.currentTimeMillis() + "===";
		  
	      try
	      {
		      
		      //URL url = new URL( args[0] );
		      File uploadFile = new File(fileName);
		      hConnection = (HttpURLConnection)
		                             url.openConnection();
		      HttpURLConnection.setFollowRedirects( true );
		 
		      hConnection.setDoOutput( true );
		      hConnection.setRequestMethod("POST");	
		      startTime  = System.currentTimeMillis();			      
		      outputStream = hConnection.getOutputStream();
		      writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
		              true);
		      writer.append("fun=remove&file_name=" + fileName);	
		      
		      int length = 0;
		      List<String> response = finish(0);
		      endTime  = System.currentTimeMillis();		      
		      for (String line : response) {
		    	  length += line.length();
		          //System.out.println(line);
		      }
		      
		      System.out.println(fileName + "," + startTime + "," + endTime + "," + (endTime-startTime));
		      
		  } catch (IOException ex) {
		      System.out.println("ERROR: " + ex.getMessage());
		      ex.printStackTrace();
		  }    	
}        
    public void testFind() {
	  	  long startTime, endTime;
		  boundary = "===" + System.currentTimeMillis() + "===";
		  
	      try
	      {
		     // URL url = new URL( args[0] );
		      File uploadFile = new File(fileName);
		      hConnection = (HttpURLConnection)
		                             url.openConnection();
		      HttpURLConnection.setFollowRedirects( true );
		 
		      hConnection.setDoOutput( true );
		      hConnection.setRequestMethod("POST");	
		      startTime  = System.currentTimeMillis();		      
		      outputStream = hConnection.getOutputStream();
		      writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
		              true);
		      writer.append("fun=find&file_name=" + fileName);		      
		      
		      
		      int length = 0;
		      List<String> response = finish(0);
		      endTime  = System.currentTimeMillis();
		      for (String line : response) {
		    	  length += line.length();
		          //System.out.println(line);
		      }
		      
		      System.out.println(fileName + "," + length + startTime + "," + endTime + "," + (endTime-startTime));
		      
		  } catch (IOException ex) {
		      System.out.println("ERROR: " + ex.getMessage());
		      ex.printStackTrace();
		  }    	
  }    
  
	public void run(){ 
		if (cmd.equals("insert")) {
			testInsert();
		}else if (cmd.equals("remove")) {
			testRemove();
		}else if (cmd.equals("find")) {
			testFind();
		}
	}
	
}
