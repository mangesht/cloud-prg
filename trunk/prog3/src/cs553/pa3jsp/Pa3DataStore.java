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

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
//import com.google.appengine.api.datastore.KeyFactory.Builder;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;



@SuppressWarnings("serial")
public class Pa3DataStore extends HttpServlet {
	public static  Entity statistics_insert;
	public static  Entity statistics_find;
	public static  Entity statistics_remove;
	public static  Key k_insert;
	public static  Key k_find;
	public static  Key k_remove;
	
	public static  int init_statistics = 0;
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException  {
		fileOpsUsingDataStore(req,resp);
	}

	public void fileOpsUsingDataStore(HttpServletRequest req,
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
                } else if (ops.equals("clear_statistics")) {
                	performClearStatistics(user, req,resp);
                }
            }
    	} catch (IOException e) {
    		throw new ServletException("Cannot parse multipart request: " + e.getMessage());
    	}
	}
	
	public void initStatistics(String ops) {
		/* We store the statistics in datastore */
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		/* One key for operation */
		
		/* That key contains entities that contains counter */
		if (ops.equals("insert")) {
			k_insert = KeyFactory.createKey("ds_stats_op", "insert");
			try {
				statistics_insert = datastore.get(k_insert);
			} catch (EntityNotFoundException e) {
				statistics_insert = new Entity(k_insert);
				statistics_insert.setProperty("counter", 0);
				datastore.put(statistics_insert);
			}
	    }
	    
		if (ops.equals("find")) {
			k_find =  KeyFactory.createKey("ds_stats_op", "find");
			try {
				statistics_find = datastore.get(k_find);
				/* should not output anything during find */
			} catch (EntityNotFoundException e) {
				statistics_find = new Entity(k_find);
				statistics_find.setProperty("counter", 0);
				datastore.put(statistics_find);				
			}
	    }

		if (ops.equals("remove")) {
			k_remove = KeyFactory.createKey("ds_stats_op", "remove");
			try {
				statistics_remove = datastore.get(k_remove);
			} catch (EntityNotFoundException e) {
				statistics_remove = new Entity(k_remove);
				statistics_remove.setProperty("counter", 0);
				datastore.put(statistics_remove);					
			}		
	    }

	}	
	
	public Entity statisticsStart(String operation,
								String filename) {
        /* We store the statistics in datastore */
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity  s =  null;
        Entity  counter;
        Key k = null;
        int nextCount = 0;
        /* One key for operation */
      	/* That key contains entities that contains counter */
        
		initStatistics(operation);
		
        if (operation.equals("insert")) {
        	nextCount = Integer.valueOf(String.valueOf(statistics_insert.getProperty("counter")));
        	nextCount++;
       		statistics_insert.setProperty("counter", nextCount);
	      	k = KeyFactory.createKey(k_insert, "ds_stats_index_insert", nextCount);
        } else if (operation.equals("find")){
        	nextCount = Integer.valueOf(String.valueOf(statistics_find.getProperty("counter")));
        	nextCount++;
       		statistics_find.setProperty("counter", nextCount);
	      	k = KeyFactory.createKey(k_find, "ds_stats_index_find", nextCount);
        } else if (operation.equals("remove")){
        	nextCount = Integer.valueOf(String.valueOf(statistics_remove.getProperty("counter")));
        	nextCount++;
       		statistics_remove.setProperty("counter", nextCount);
	      	k = KeyFactory.createKey(k_remove, "ds_stats_index_remove", nextCount);
        }
        
        if (null != k) {
	   		s = new Entity(k);
	   		s.setProperty("stats_index", nextCount);
	   		s.setProperty("startTime", System.currentTimeMillis());
	        s.setProperty("filename", filename);
	        s.setProperty("operation", operation);
        }
   		return s;
	}
	
	public void statisticsEnd(Entity s, int datasize) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		s.setProperty("endTime", System.currentTimeMillis());
		s.setProperty("filesize", datasize);
      	datastore.put(s);
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
     	resp.getWriter().println("<h1>PA3 File Storage -- Using Datastore</h2>");
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
        resp.getWriter().println("<a href=\"/pa3_datastore.jsp\">Go Back</a>.</p>");
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
  	    Query q = new Query("ds_stats_index_insert", k_insert);
   	    PreparedQuery pq = datastore.prepare(q); 
   	    outputHeader(user, req,resp);
        resp.getWriter().println("<br><br><i> Statistics for Insert </i><br>");  
        
        resp.getWriter().println("<table border=1><tr>" +
        		"<td>Index</td>" +
        		"<td>Filename</td>" +
        		"<td>Filesize</td>" +
        		"<td>Operation</td>" +
        		"<td>StartTime</td>" +
        		"<td>EndTime</td>" +
        		"<td>TotalTime</td>" +
        		"</tr> ");
        
   	    for (Entity result : pq.asIterable()) {
   	    	Long endTime, startTime, totalTime;
   	    	endTime = (Long.valueOf(String.valueOf(result.getProperty("endTime")))); 
   	    	startTime = (Long.valueOf(String.valueOf(result.getProperty("startTime"))));
   	    	
    	    resp.getWriter().println("<tr>" +
    	    				    "<td>" + result.getProperty("stats_index") + 
    							"</td><td>" + result.getProperty("filename") + "</td>" + 
    							"</td><td>" + result.getProperty("filesize") + "</td>" + 
    							"</td><td>" + result.getProperty("operation") + "</td>" + 
    							"</td><td>" + result.getProperty("startTime") + "</td>" + 
    							"</td><td>" + result.getProperty("endTime") + "</td>" +
    							"</td><td>" + (endTime - startTime) + "</td>" +
    	    					"</tr>");
   	    }
	    resp.getWriter().println("</table>");

	    
       /* Find */
  	    q = new Query("ds_stats_index_find", k_find);
   	    pq = datastore.prepare(q); 
        resp.getWriter().println("<br><br><i> Statistics for Find </i><br>");  
        
        resp.getWriter().println("<table border=1><tr>" +
        		"<td>Index</td>" +
        		"<td>Filename</td>" +
        		"<td>Filesize</td>" +
        		"<td>Operation</td>" +	        		
        		"<td>StartTime</td>" +
        		"<td>EndTime</td>" +
        		"<td>TotalTime</td>" +
        		"</tr> ");
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	Long endTime, startTime, totalTime;
   	    	endTime = (Long.valueOf(String.valueOf(result.getProperty("endTime")))); 
   	    	startTime = (Long.valueOf(String.valueOf(result.getProperty("startTime"))));
   	    	
    	    resp.getWriter().println("<tr>" +
    	    				    "<td>" + result.getProperty("stats_index") + 
    							"</td><td>" + result.getProperty("filename") + "</td>" + 
    							"</td><td>" + result.getProperty("filesize") + "</td>" + 
    							"</td><td>" + result.getProperty("operation") + "</td>" + 	    							
    							"</td><td>" + result.getProperty("startTime") + "</td>" + 
    							"</td><td>" + result.getProperty("endTime") + "</td>" +
    							"</td><td>" + (endTime - startTime) + "</td>" +
    	    					"</tr>");
   	    }
	    resp.getWriter().println("</table>");
		    
       /* Remove */
  	    q = new Query("ds_stats_index_remove", k_remove);
   	    pq = datastore.prepare(q); 
        resp.getWriter().println("<br><br><i> Statistics for Remove </i><br>");  
        
        resp.getWriter().println("<table border=1><tr>" +
        		"<td>Index</td>" +
        		"<td>Filename</td>" +
        		"<td>Filesize</td>" +
        		"<td>Operation</td>" +		        		
        		"<td>StartTime</td>" +
        		"<td>EndTime</td>" +
        		"<td>TotalTime</td>" +
        		"</tr> ");
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	Long endTime, startTime, totalTime;
   	    	endTime = (Long.valueOf(String.valueOf(result.getProperty("endTime")))); 
   	    	startTime = (Long.valueOf(String.valueOf(result.getProperty("startTime"))));
   	    	
    	    resp.getWriter().println("<tr>" +
    	    				    "<td>" + result.getProperty("stats_index") + 
    							"</td><td>" + result.getProperty("filename") + "</td>" + 
    							"</td><td>" + result.getProperty("filesize") + "</td>" + 
    							"</td><td>" + result.getProperty("operation") + "</td>" + 		    							
    							"</td><td>" + result.getProperty("startTime") + "</td>" + 
    							"</td><td>" + result.getProperty("endTime") + "</td>" +
    							"</td><td>" + (endTime - startTime) + "</td>" +
    	    					"</tr>");
   	    }
	    resp.getWriter().println("</table>");		    
		    
	    outputFooter(user,req, resp);	    
	}
	
	public void performClearStatistics(User user,
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
   	    outputHeader(user, req,resp);        
        /* Insert */
        resp.getWriter().println("<i> Deleting Statistics for Insert </i><br><br>");          
  	    Query q = new Query("ds_stats_index_insert", k_insert);
   	    PreparedQuery pq = datastore.prepare(q); 
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	datastore.delete(result.getKey());
   	    }
	    
	    
       /* Find */
  	    q = new Query("ds_stats_index_find", k_find);
   	    pq = datastore.prepare(q); 
        resp.getWriter().println("<i> Deleting Statistics for Find </i><br><br>");  
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	datastore.delete(result.getKey());
   	    	
   	    }
		    
       /* Remove */
  	    q = new Query("ds_stats_index_remove", k_remove);
   	    pq = datastore.prepare(q); 
        resp.getWriter().println("<i> Deleting Statistics for Remove </i><br><br>");  
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	datastore.delete(result.getKey());

   	    }
		    
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
  	    String filename=req.getParameter("file_name");	
   	    String login_user;
   	    String filesize = "0";
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
   	    	filesize = String.valueOf(
   	    	    	result.getProperty("file-contentlen"));
    		/* Datastore retieval */
        	Key k3 = new KeyFactory.Builder("user", user.getNickname())
		   .addChild("file", filename)
		   .getKey();     
   	   	    Query qFile = new Query(k3);
   	   	    PreparedQuery pqFIle = datastore.prepare(qFile);
   	   	    Entity resultFile;   	    		
   	 	    String filestore = (String) result.getProperty("file-store");
	    	
	    	if (filestore.equals("BlobStore"))	{
	    		resp.getWriter().println("<i> File is stored in Blobstore, please select Blob store in the index page and continue </i><br>");
	    	} else if (filestore.equals("CloudStore"))	{
	    		resp.getWriter().println("<i> File is stored in Cloud, please select Cloud store in the index page and continue </i><br>");
	    	} else if (filestore.equals("DataStore")) {
	   	   	    resultFile = pqFIle.asSingleEntity();
   	   	    
	        	Blob blob = (Blob) resultFile.getProperty("file-content");
		   	    String mimeType = "application/octet-stream";
		   	   
	        	resp.setContentType(mimeType);
	            resp.setContentLength((int) Integer.valueOf(filesize));
	            String headerKey = "Content-Disposition";
	            String headerValue = String.format("attachment; filename=\"%s\"", filename);
	            resp.setHeader(headerKey, headerValue);   	        	
	            OutputStream outStream = resp.getOutputStream();
	            byte[] buffer = blob.getBytes();
		        outStream.write(buffer, 0, Integer.valueOf(filesize));
	    	}
   	    }
   	    
   	    if (found == 0) {
   	   	    outputHeader(user, req,resp);   	    	
    	    resp.getWriter().println("<p>Filename = " + filename +
					                              " Not Found ...</p>");
    	    outputFooter(user,req, resp);     	    
   	    }	
   	    int intFileSize = Integer.valueOf(filesize);
		statisticsEnd(stats, intFileSize);
    
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
       	Key k1 = new KeyFactory.Builder("user", user.getNickname())
       						   .addChild("file", filename)
       						   .getKey();  	    
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
   	    		resp.getWriter().println("<i> File is stored in Blobstore, please select Blob store in the index page and continue </i><br>");
   	    	} else if (filestore.equals("CloudStore"))	{
   	    		resp.getWriter().println("<i> File is stored in Cloud, please select Cloud store in the index page and continue </i><br>");
   	    	} else if (filestore.equals("DataStore")) {
   	    		found = 1;
   	    		resp.getWriter().println("<i> File is stored in Datastore, deleting it from there </i><br>");
   	    		datastore.delete(k1);
   	    	}
   	    }
   	    if (found == 1) {
   	    	datastore.delete(k2);
   	    }
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

		int contentLength;
		int DATASTORE_LIMIT = 1000*1000;
		
        resp.getWriter().println("<i> field_name = " + item.getFieldName() + "</i><br>");  
        resp.getWriter().println("<i> content-type = " + item.getContentType() + "</i><br>");  
        resp.getWriter().println("<i> is form field = " + item.isFormField() + "</i><br>");  
        resp.getWriter().println("<i> filename = " + item.getName() + "</i><br>");
        resp.getWriter().println("<i> content-length = " + req.getContentLength() + "</i><br>");
        
        contentLength = req.getContentLength();
        	
        if (contentLength < DATASTORE_LIMIT) {
        	storeInDataStore(userName, item, req, resp);
        } else {
            resp.getWriter().println("<i> In datastore, currrently cannot add more than " +  DATASTORE_LIMIT + "</i><br>");  	        	
        }
	}
	
	
	public void storeInDataStore(String userName,
			 FileItemStream item, 
			 HttpServletRequest req, 
			 HttpServletResponse resp) throws IOException,
			 								  EntityNotFoundException {	
	   InputStream stream = item.openStream();
       int len;
       int chunk_count = 0;
       byte[] buffer = new byte[1024*924];
       int complete_len = 0;
        
		Entity stats = statisticsStart("insert", item.getName());  
		
		
       DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       Entity  f;
       Entity  d;
      	
      	Key k2 = new KeyFactory.Builder("user", userName).addChild("file", item.getName()).getKey();
      	Key k3 = new KeyFactory.Builder("user", userName).addChild("fileinfo", item.getName()).getKey();
      	
      	try {
      	  f = datastore.get(k2);
      	} catch (EntityNotFoundException ex) {
      		  f = new Entity(k2);
         	  resp.getWriter().println("<br><i>new file into datastore</i><br>" );       		  
      	}
      	
      	try {
        	  d = datastore.get(k3);
       } catch (EntityNotFoundException ex) {
        		  d = new Entity(k3);
           	  resp.getWriter().println("<br><i>new fileinfo into datastore</i><br>" );       		  
       }
      	
       while ((len = stream.read(buffer, complete_len, (buffer.length-complete_len))) != -1) {
     	complete_len += len;
       	resp.getWriter().println("<br><i>File Chunk " + chunk_count +
       			" with lenth " + len + "</i><br>" );
       	chunk_count++;
       }	
       Blob blob = new Blob(buffer);
      	f.setProperty("file-contentlen", complete_len);
      	f.setProperty("file-content", blob);
      	d.setProperty("file-contentlen", complete_len);
      	d.setProperty("file-name", item.getName());
      	d.setProperty("file-store", "DataStore");       	
      	datastore.put(f);
      	datastore.put(d);       	
		statisticsEnd(stats, complete_len);       	
        resp.getWriter().println("<br><i>File " +  item.getName() + " Added. Length =" + complete_len + "</i><br>" );
	}	
}
