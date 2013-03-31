package cs553.pa3jsp;

import java.io.IOException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.*;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;


import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
/*import com.google.appengine.api.files.FileReadChannel;*/
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;
import java.io.PrintWriter;
import java.nio.channels.Channels;

@SuppressWarnings("serial")
public class Pa3CloudStore extends HttpServlet {
	public static final String BUCKETNAME = "cloud-prg-prg3";	
	public static  Entity statistics_insert;
	public static  Entity statistics_find;
	public static  Entity statistics_remove;
	public static  int init_statistics = 0;
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException  {

		if (init_statistics == 0) {
			initStatistics();
			init_statistics = 1;
		}
			
		fileOpsUsingCloudStore(req,resp);
	}

	public void fileOpsUsingCloudStore(HttpServletRequest req,
												  HttpServletResponse resp)
			throws ServletException, IOException  {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        /* Temporarily, ignore the case of user being NULL.
           Our test automation program does not implement the          
           oauth logic yet.
         */
        if (user == null)
        {
        	/*resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));*/
        }

        
    	boolean isMultipart = ServletFileUpload.isMultipartContent(req);
    	try {
            if (isMultipart == true) {
            	performInsert(user, req,resp);
            } else {
                String ops=req.getParameter("fun");
                if (ops.equals("listing")) {
                	performListing(user, req,resp);
                } else if (ops.equals("check")) {
                	performCheck(user, req,resp);
                } else if (ops.equals("find")) {
                	performFind(user, req,resp);
                } else if (ops.equals("remove")) {
                	performRemove(user, req,resp);
                } else if (ops.equals("statistics")) {
                	performStatistics(user, req,resp);
                }
            }
    	} catch (IOException e) {
    		throw new ServletException("Cannot parse multipart request: " + e.getMessage());
    	}
	}
	
	public void initStatistics() {
		/* We store the statistics in datastore */
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		/* One key for operation */
		Key k_insert = new KeyFactory.Builder("stats_op", "insert").getKey();
		Key k_find = new KeyFactory.Builder("stats_op", "find").getKey();
		Key k_remove = new KeyFactory.Builder("stats_op", "remove").getKey();
		
		/* That key contains entities that contains counter */
		try {
			statistics_insert = datastore.get(k_insert);
		} catch (EntityNotFoundException e) {
			statistics_insert = new Entity(k_insert);
			statistics_insert.setProperty("counter", 0);
		}

		try {
			statistics_find = datastore.get(k_find);
		} catch (EntityNotFoundException e) {
			statistics_find = new Entity(k_find);
			statistics_find.setProperty("counter", 0);
		}
		
		try {
			statistics_remove = datastore.get(k_remove);
		} catch (EntityNotFoundException e) {
			statistics_remove = new Entity(k_remove);
			statistics_remove.setProperty("counter", 0);
		}		

	}	
	
	public Entity statisticsStart(String operation,
								String filename) {
        /* We store the statistics in datastore */
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity  s =  null;
        Entity  counter;
        int nextCount = 0;
        /* One key for operation */
      	/* That key contains entities that contains counter */
		
        if (operation.equals("insert")) {
        	nextCount = (Integer) statistics_insert.getProperty("counter");
        	nextCount++;
       		statistics_insert.setProperty("counter", nextCount);
        } else if (operation.equals("find")){
        	nextCount = (Integer) statistics_find.getProperty("counter");
        	nextCount++;
       		statistics_find.setProperty("counter", nextCount);
        } else if (operation.equals("remove")){
        	nextCount = (Integer) statistics_remove.getProperty("counter");
        	nextCount++;
       		statistics_remove.setProperty("counter", nextCount);
        }

      	Key k = new KeyFactory.Builder("stats_op", operation)
			.addChild("stats_index", nextCount)
			.getKey();
   		s = new Entity(k);
   		s.setProperty("stats_index", nextCount);
   		s.setProperty("startTime", System.currentTimeMillis());
        s.setProperty("filename", filename);
   		return s;
	}
	
	public void statisticsEnd(Entity s, int datasize) {
		s.setProperty("endTime", System.currentTimeMillis());
		s.setProperty("filesize", datasize);
		return;
    }
	
	public void outputHeader(User user,
			HttpServletRequest req,
			HttpServletResponse resp) 
	        		 throws ServletException, IOException {
   	    String login_user;
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
        resp.setContentType("text/html");
        resp.getWriter().println("<div id=\"welcome\">");
     	resp.getWriter().println("<h1>PA3 File Storage -- Using CloudStore</h2>");
        resp.getWriter().println("</div>");
        
        resp.getWriter().println("<div id=\"Banner\">");
        resp.getWriter().println("<p> Hello " + login_user + "</p>");
			
        resp.getWriter().println("</div>");
        
        resp.getWriter().println("<div id=\"logout\">");
        resp.getWriter().println("<a href=\"<%= userService.createLogoutURL(request.getRequestURI()) %>\">sign out</a>.</p>");
        resp.getWriter().println("</div>");
        
        resp.getWriter().println("<div id=\"ops\">");		
	}
	
	public void outputFooter(User user,
			HttpServletRequest req,
	         HttpServletResponse resp) 
	        		 throws ServletException, IOException {
        resp.getWriter().println("</div>");
   	    resp.getWriter().println("<div id=\"nav\">");
        resp.getWriter().println("<a href=\"/pa3_cloud.jsp\">Go Back</a>.</p>");
        resp.getWriter().println("</div>");			
	}
	
	public void performStatistics(User user,
            HttpServletRequest req, 
            HttpServletResponse resp)
          		  throws IOException,
                    ServletException         {
   	    String login_user;
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        /* Insert */
       	Key k = new KeyFactory.Builder("stats_op", "insert").getKey();  	    
  	    Query q = new Query("stats_index", k);
   	    PreparedQuery pq = datastore.prepare(q); 
   	    outputHeader(user, req,resp);
        resp.getWriter().println("<i> Statistics for Insert </i><br><br>");  
        
        resp.getWriter().println("<table border=1><tr>" +
        		"<td>Index</td>" +
        		"<td>Filename</td>" +
        		"<td>Filesize</td>" +
        		"<td>StartTime</td>" +
        		"<td>EndTime</td>" +
        		"<td>TotalTime</td>" +
        		"</tr> ");
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	long totalTime;
   	    	totalTime = ((Integer)result.getProperty("endTime") - 
   	    			(Integer)result.getProperty("startTime"));
   	    	
    	    resp.getWriter().println("<tr>" +
    	    				    "<td>" + result.getProperty("stats_index") + 
    							"</td><td>" + result.getProperty("filename") + "</td>" + 
    							"</td><td>" + result.getProperty("filesize") + "</td>" + 
    							"</td><td>" + result.getProperty("startTime") + "</td>" + 
    							"</td><td>" + result.getProperty("endTime") + "</td>" +
    							"</td><td>" + totalTime + "</td>" +
    	    					"</tr>");
   	    }
	    resp.getWriter().println("</table>");
	    outputFooter(user,req, resp);
	}
	
	public void performListing(User user,
            HttpServletRequest req, 
            HttpServletResponse resp)
          		  throws IOException,
                    ServletException         {
   	    String login_user;
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       	Key k3 = new KeyFactory.Builder("user", login_user).getKey();  	    
  	    Query q = new Query("fileinfo", k3);
   	    PreparedQuery pq = datastore.prepare(q); 
   	    outputHeader(user, req,resp);
        resp.getWriter().println("<i> So you want see the listing of your files "
						 + " ! Right? Here we go ... </i><br><br>");  
        
        resp.getWriter().println("<table border=1><tr><td>Filename</td><td>File Size in Bytes</td><td>File Store</td></tr> "); 
   	    for (Entity result : pq.asIterable()) {  
   	    	String filename = (String) result.getProperty("file-name");
   	    	String filesize = String.valueOf(
   	    	result.getProperty("file-contentlen"));
   	    	String filestore = (String) result.getProperty("file-store");
   	    	
    	    resp.getWriter().println("<tr><td>" + filename +
    							"</td><td>" + filesize + "</td>" + 
    							"</td><td>" + filestore + "</td>" + 
    	    					"</tr>");
   	    }
	    resp.getWriter().println("</table>");
	    outputFooter(user,req, resp);
	}

	public void performFind(User user,
            HttpServletRequest req, 
            HttpServletResponse resp)
          		  throws IOException,
                    ServletException         {
		int found = 0;
        int len;
        int complete_len = 0;        
        String buffer;		
  	    String filename=req.getParameter("file_name");
   	    String login_user;
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
		Entity stats = statisticsStart("find", filename);
		
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       	Key k2 = new KeyFactory.Builder("user", login_user)
		   .addChild("fileinfo", filename)
		   .getKey();  	    
  	    Query q = new Query(k2);
   	    PreparedQuery pq = datastore.prepare(q); 
   	    for (Entity result : pq.asIterable()) {  
   	    	found = 1;
   	    	String filestore = (String) result.getProperty("file-store");
   	    	    	   	    	
   	    	if (filestore.equals("BlobStore"))	{
   	    		/* Blobstore retrieval */
   	    	} else if (filestore.equals("DataStore")) {
   	    		/* Datastore retieval */
   	    	} else if (filestore.equals("CloudStore")) {
   	    		/* Cloudstore retieval */
   	   	    	String filesize = String.valueOf(
   	   	   	    	result.getProperty("file-contentlen"));   	    		
   	            FileService fileService = FileServiceFactory.getFileService();   	    		
   	    		String cloud_filename = "/gs/" + BUCKETNAME + "/" + filename;
                AppEngineFile readableFile = new AppEngineFile(cloud_filename);
                FileReadChannel readChannel =
                    fileService.openReadChannel(readableFile, false);
                // Again, different standard Java ways of reading from the channel.
                
   		   	    String mimeType = "application/octet-stream";
   	        	resp.setContentType(mimeType);
   	            resp.setContentLength((int) Integer.valueOf(filesize));
   	            String headerKey = "Content-Disposition";
   	            String headerValue = String.format("attachment; filename=\"%s\"", filename);
   	            resp.setHeader(headerKey, headerValue);   	        	
   	            OutputStream outStream = resp.getOutputStream();

                BufferedReader reader =
                        new BufferedReader(Channels.newReader(readChannel, "UTF8"));
                String str;
                while ((str = reader.readLine()) != null) {
                	str += "\n";
               		outStream.write(str.getBytes(), 0, str.length());
               		complete_len += str.length();
                }        	

                readChannel.close();     	        
   	    	}
   	    }
   	    
   	    if (found == 0) {
   	   	    outputHeader(user, req,resp);   	    	
    	    resp.getWriter().println("<p>Filename = " + filename +
					                              " Not Found ...</p>");
    	    outputFooter(user,req, resp);     	    
   	    }	
		statisticsEnd(stats, complete_len);
    
	}
	
	public void performCheck(User user,
            HttpServletRequest req, 
            HttpServletResponse resp)
          		  throws IOException,
                    ServletException         {
		int found = 0;
   	    String login_user;
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
  	    String filename=req.getParameter("file_name");	
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
   	    outputHeader(user, req,resp);
       	Key k2 = new KeyFactory.Builder("user", login_user)
		   .addChild("fileinfo", filename)
		   .getKey();  	    
       	
  	    Query q = new Query(k2);
   	    PreparedQuery pq = datastore.prepare(q); 
   	    
        resp.getWriter().println("<i> So you want to search the file "
						 + filename + " ! Right? Here we go ... </i><br><br>");  
   	    for (Entity result : pq.asIterable()) {  
   	    	found = 1;
   	    	String filename1 = (String) result.getProperty("file-name");
   	    	String filesize = String.valueOf(
   	    	result.getProperty("file-contentlen"));
   	    	String filestore = (String) result.getProperty("file-store");
   	    	
    	    resp.getWriter().println("<p>Filename = " + filename1 +
    							"</p><p> File Size in Bytes = " + filesize + "</p>" + 
    							"</p><p> File Store = " + filestore + "</p>");
   	    }
   	    
   	    if (found == 0) {
    	    resp.getWriter().println("<p>Filename = " + filename +
					                              " Not Found ...</p>");
   	    }
	    outputFooter(user,req, resp);   	   	    
	}	
	
	public void performRemove(User user,
            HttpServletRequest req, 
            HttpServletResponse resp)
          		  throws IOException,
                    ServletException         {
   	    String login_user;
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			

		String filename=req.getParameter("file_name");	
		
		Entity stats = statisticsStart("remove", filename);		
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       	Key k2 = new KeyFactory.Builder("user", login_user)
		   .addChild("fileinfo", filename)
		   .getKey();  	    
       	
  	    Query q = new Query(k2);
   	    PreparedQuery pq = datastore.prepare(q); 
   	    outputHeader(user, req,resp);   	    
        resp.getWriter().println("<i> So you want to remove the file "
						 + filename + " ! Right? Here we go ... </i><br><br>");  
   	    for (Entity result : pq.asIterable()) {  
   	    	String filestore = (String) result.getProperty("file-store");
   	    	
   	    	if (filestore.equals("BlobStore"))	{
   	    		resp.getWriter().println("<i> File is stored in Blobstore, Please relogin after selecting Blobstore </i><br>");
   	    	} else if (filestore.equals("DataStore")) {
   	    		resp.getWriter().println("<i> File is stored in Datastore, Please relogin after selecting Blobstore </i><br>");
   	    	} else if (filestore.equals("CloudStore")) {
   	    		BlobKey blobKey;
   	    		BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
   	    		String cloud_filename = "/gs/" + BUCKETNAME + "/" + filename;
   	    		resp.getWriter().println("<i> File is stored in Cloudstore, Deleting the file there. </i><br>");
   	    		blobKey = blobstore.createGsBlobKey(cloud_filename);
   	    		blobstore.delete(blobKey);
   	    	}
   	    }
   	    
        datastore.delete(k2);
		statisticsEnd(stats, 0);
	    outputFooter(user,req, resp);          
	}	
	
	public void performInsert(User user,
			                  HttpServletRequest req, 
			                  HttpServletResponse resp)
			                		  throws IOException,
			                          ServletException         {
   	    String login_user;
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
    	// Create a factory for disk-based file items
   	    outputHeader(user, req,resp);   		
        resp.getWriter().println("<i> So you want do the operation " + "insert" + " ! Right?</i><br>");             	
    	try {
    		ServletFileUpload upload = new ServletFileUpload();
    		FileItemIterator iter = upload.getItemIterator(req);
    		
    		while (iter.hasNext()) {
    			FileItemStream item = iter.next();
    			if (item.isFormField()) continue;
        		storeInStore(login_user, item, req,resp);
    		}
    	} catch (FileUploadException e) {
    		throw new ServletException("Cannot parse multipart request: " + e.getMessage());
    	} catch (EntityNotFoundException e) {
    		throw new ServletException("Cannot parse multipart request: " + e.getMessage());
    	}
	    outputFooter(user,req, resp);     	
    } 
	
	public void storeInStore(String userName,
								 FileItemStream item, 
								 HttpServletRequest req, 
								 HttpServletResponse resp) throws IOException,
								 EntityNotFoundException {

        resp.getWriter().println("<i> field_name = " + item.getFieldName() + "</i><br>");  
        resp.getWriter().println("<i> content-type = " + item.getContentType() + "</i><br>");  
        resp.getWriter().println("<i> is form field = " + item.isFormField() + "</i><br>");  
        resp.getWriter().println("<i> filename = " + item.getName() + "</i><br>");
        resp.getWriter().println("<i> content-length = " + req.getContentLength() + "</i><br>");
        
        resp.getWriter().println("<i> Should invoke  storeInCloudStore </i><br>");
	
        storeInCloudStore(userName, item, req, resp);

	}
	
	public void storeInCloudStore(String userName,
			 FileItemStream item, 
			 HttpServletRequest req, 
			 HttpServletResponse resp) throws IOException,
			 								  EntityNotFoundException {	
		InputStream stream = item.openStream();
        int len;
        int chunk_count = 0;
        byte[] buffer = new byte[1024*924];
        int complete_len = 0;
        
		Entity stats = statisticsStart("insert", item.getFieldName());        
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       	Key k3 = new KeyFactory.Builder("user", userName).addChild("fileinfo", item.getName()).getKey();
       	Entity  d;
                
       	try {
         	  d = datastore.get(k3);
        } catch (EntityNotFoundException ex) {
     		  d = new Entity(k3);
        	  resp.getWriter().println("<br><i>new fileinfo into datastore</i><br>" );       		  
        }

       	

       	FileService fileService = FileServiceFactory.getFileService();
       	

       	GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
           .setBucket(BUCKETNAME)
           .setKey(item.getName())
           .setMimeType("text/plain")
           .setAcl("public-read"); 

       	AppEngineFile writableFile =
                fileService.createNewGSFile(optionsBuilder.build());
       
     
       	boolean lock = false;

        FileWriteChannel writeChannel =
                fileService.openWriteChannel(writableFile, lock);

        PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
        

        
        while ((len = stream.read(buffer, complete_len, (buffer.length-complete_len))) != -1) {
       	    complete_len += len;
        	resp.getWriter().println("<br><i>File Chunk " + chunk_count +
        			" with lenth " + len + " complete_len = " + complete_len +
        				"buffer.length = " + buffer.length + "</i><br>" );
        	chunk_count++;
           	if (complete_len == buffer.length) {
            	resp.getWriter().println("<br><i>Writing into cloud " 
            				+ " complete_len = " + complete_len +
            				"buffer.length = " + buffer.length + "</i><br>" );

           		out.println(buffer.toString());
           		complete_len = 0;
           	}        	
        }

       	if (complete_len < buffer.length) {
       		String str = new String(buffer, 0, complete_len);
        	resp.getWriter().println("<br><i>Writing into cloud " 
    				+ " complete_len = " + complete_len +
    				"buffer.length = " + buffer.length + "</i><br>" );
       		out.println(str);
       	}
        out.close();

        String path = writableFile.getFullPath();
        
       	d.setProperty("file-contentlen", complete_len);
       	d.setProperty("file-name", item.getName());
       	d.setProperty("file-path", new Blob(path.getBytes()));
       	d.setProperty("file-store", "CloudStore");       	
       	datastore.put(d);       	
        // Now finalize
        lock = true;

        writeChannel =
                fileService.openWriteChannel(writableFile, lock);
       	writeChannel.closeFinally();
		statisticsEnd(stats, complete_len);       	
        resp.getWriter().println("<br><i>File " +  item.getName() + " Added. Length =" + complete_len + "</i><br>" );
        
	}
	
}
