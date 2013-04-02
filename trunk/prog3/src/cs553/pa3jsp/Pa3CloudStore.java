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
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.io.PrintWriter;
import java.nio.channels.Channels;

@SuppressWarnings("serial")
public class Pa3CloudStore extends HttpServlet {
	public static final String BUCKETNAME = "cloud-prg-prg3";	
	public static  Entity statistics_insert;
	public static  Entity statistics_find;
	public static  Entity statistics_remove;
	public static  Key k_insert;
	public static  Key k_find;
	public static  Key k_remove;
	
	public static  int init_statistics = 0;
    MemcacheService  syncCache ;
	boolean memCacheEnable = true;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException  {
        syncCache = MemcacheServiceFactory.getMemcacheService();
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
        /* Initialise memcache enable to true
         * users can disable it run time using the combo box.
         * this result needs to be persistant till it is changed again 
         * so we store it in data store
         */
        initMemCacheEnable(user);
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
                } if (ops.equals("memcache")) {
                	performMemCacheToggle(user, req,resp);
                }
            }
    	} catch (IOException e) {
    		throw new ServletException("Cannot parse multipart request: " + e.getMessage());
    	}
	}
	
	public void initStatistics(String ops) {
		/* We store the statistics in datastore */
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		/* One key for each operation */
		
		/* That key contains entities that contains counter */
		/* We store counter value as a property in the statistics entity 
		 */
		if (ops.equals("insert")) {
			k_insert = KeyFactory.createKey("stats_op", "insert");
			try {
				statistics_insert = datastore.get(k_insert);
			} catch (EntityNotFoundException e) {
				statistics_insert = new Entity(k_insert);
				statistics_insert.setProperty("counter", 0);
				datastore.put(statistics_insert);
			}
	    }
	    
		if (ops.equals("find")) {
			k_find =  KeyFactory.createKey("stats_op", "find");
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
			k_remove = KeyFactory.createKey("stats_op", "remove");
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
        /* One key for each operation */
      
        
		initStatistics(operation);
		/* Each statistics is stored as an entity with the running index as primary key */
        if (operation.equals("insert")) {
        	nextCount = Integer.valueOf(String.valueOf(statistics_insert.getProperty("counter")));
        	nextCount++;
       		statistics_insert.setProperty("counter", nextCount);
	      	k = new KeyFactory.Builder("stats_op", "insert")
	      				.addChild("stats_index_insert", nextCount)
	      				.getKey();
			datastore.put(statistics_insert);	      	
        } else if (operation.equals("find")){
        	nextCount = Integer.valueOf(String.valueOf(statistics_find.getProperty("counter")));
        	nextCount++;
       		statistics_find.setProperty("counter", nextCount);
	      	k = new KeyFactory.Builder("stats_op", "find")
				.addChild("stats_index_find", nextCount)
				.getKey();
			datastore.put(statistics_find);	      	
		} else if (operation.equals("remove")){
        	nextCount = Integer.valueOf(String.valueOf(statistics_remove.getProperty("counter")));
        	nextCount++;
       		statistics_remove.setProperty("counter", nextCount);
	      	k = new KeyFactory.Builder("stats_op", "remove")
				.addChild("stats_index_remove", nextCount)
				.getKey();
			datastore.put(statistics_remove);	      	
        }
        
        if (null != k) {
	   		s = new Entity(k);
			/* Properties stored for statistics (before executing operation)*/	   		
	   		s.setProperty("stats_index", nextCount);
	   		s.setProperty("startTime", System.currentTimeMillis());
	        s.setProperty("filename", filename);
	        if (memCacheEnable == true) {
	        	s.setProperty("operation", operation + " with memache");
	        } else {
	        	s.setProperty("operation", operation);
	        }
        }
   		return s;
	}
	
	public void statisticsEnd(Entity s, int datasize) {
		/* Properties stored for statistics (after executing operation)*/	   		
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
		
		/* Common Header  */
   	    String login_user;
   	    /* Following block enables looging in through automation script
   	     * without user authentication.
   	     */
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
		/* Common Footer  */		
		
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
		/* In this function we dump the statistics */	   		
		
   	    String login_user;
   	    /* Following block enables looging in through automation script
   	     * without user authentication.
   	     */
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
  	    Query q = new Query("stats_index_insert", k_insert);
   	    PreparedQuery pq = datastore.prepare(q); 
   	    outputHeader(user, req,resp); /* Header (User name and Sign Out)*/
        resp.getWriter().println("<br><br><i> Statistics for Insert </i><br>");  
        
        resp.getWriter().println("<table border=1><tr>" +
        		"<td>Index</td>" +
        		"<td>Filename</td>" +
        		"<td>Filesize</td>" +
        		"<td>Operation</td>" +
        		"<td>StartTime (ms)</td>" +
        		"<td>EndTime (ms) </td>" +
        		"<td>TotalTime (ms) </td>" +
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
  	    q = new Query("stats_index_find", k_find);
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
  	    q = new Query("stats_index_remove", k_remove);
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
		    
	    outputFooter(user,req, resp);/* Footer; Navigational Links */	    
	}
	
	public void performClearStatistics(User user,
            HttpServletRequest req, 
            HttpServletResponse resp)
          		  throws IOException,
                    ServletException         {
   	    String login_user;
   	    /* Following block enables looging in through automation script
   	     * without user authentication.
   	     */
   	    
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
   	    outputHeader(user, req,resp);  /* Header (User name and Sign Out)*/      
        /* Insert */
        resp.getWriter().println("<i> Deleting Statistics for Insert </i><br><br>");          
  	    Query q = new Query("stats_index_insert", k_insert);
   	    PreparedQuery pq = datastore.prepare(q); 
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	datastore.delete(result.getKey());
   	    }
		initStatistics("insert");
   		statistics_insert.setProperty("counter", 0);
   		datastore.put(statistics_insert);
	    
       /* Find */
  	    q = new Query("stats_index_find", k_find);
   	    pq = datastore.prepare(q); 
        resp.getWriter().println("<i> Deleting Statistics for Find </i><br><br>");  
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	datastore.delete(result.getKey());
   	    	
   	    }
		initStatistics("find");
   		statistics_find.setProperty("counter", 0);
   		datastore.put(statistics_find);
		    
       /* Remove */
  	    q = new Query("stats_index_remove", k_remove);
   	    pq = datastore.prepare(q); 
        resp.getWriter().println("<i> Deleting Statistics for Remove </i><br><br>");  
        
   	    for (Entity result : pq.asIterable()) {
   	    	
   	    	datastore.delete(result.getKey());

   	    }
		initStatistics("remove");
   		statistics_remove.setProperty("counter", 0);
   		datastore.put(statistics_remove);
   		
	    outputFooter(user,req, resp);/* Footer; Navigational Links */	    
	}
	
	public void initMemCacheEnable(User user)
          		  throws IOException,
                    ServletException         {
		
   	    String login_user;
   	    
   	    /* Following block enables looging in through automation script
   	     * without user authentication.
   	     */
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       	Key k3 = new KeyFactory.Builder("user", login_user)
       						.addChild("memcache", 1)
       						.getKey();  	    
       	Entity  m;
        
       	try {
         	  m = datastore.get(k3);
        } catch (EntityNotFoundException ex) {
     		  m = new Entity(k3);
     		  /* By default set the value of memcachenable to true */
        	  m.setProperty("memcache_enable", "true");
        	  datastore.put(m);
        }
   	    
       	String menable = (String) m.getProperty("memcache_enable");
       	
       	if (menable.equals("true")) {
       		memCacheEnable = true;
       	} else if (menable.equals("false")) {
       		memCacheEnable = false;
       	}

	}	
	
	public void performMemCacheToggle(User user,
            HttpServletRequest req, 
            HttpServletResponse resp)
          		  throws IOException,
                    ServletException         {
   	    String login_user;
   	    /* Following block enables looging in through automation script
   	     * without user authentication.
   	     */   	    
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }			
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       	Key k3 = new KeyFactory.Builder("user", login_user)
			.addChild("memcache", 1)
			.getKey();  	    
   	    outputHeader(user, req,resp); /* Header (User name and Sign Out)*/
   	   
       	Entity  m;
        
       	try {
         	  m = datastore.get(k3);
        } catch (EntityNotFoundException ex) {
     		  m = new Entity(k3);
        }
   	    
       	/* Set the value as per the user request. Passed through
       	 * the form field memcache-enable
       	 */
       	String menable = req.getParameter("memcache-enable");
       	
       	if (menable.equals("Enable")) {
       		memCacheEnable = true;
       		m.setProperty("memcache_enable", "true");
            resp.getWriter().println("<i> Updated Memcache to true</i><br><br>");
       	} else if (menable.equals("Disable")) {
       		memCacheEnable = false;
       		m.setProperty("memcache_enable", "false");
            resp.getWriter().println("<i> Updated Memcache to false</i><br><br>");
       	}
  	    datastore.put(m);       	
	    outputFooter(user,req, resp);/* Footer; Navigational Links */
	}	
	
	public void performListing(User user,
            HttpServletRequest req, 
            HttpServletResponse resp)
          		  throws IOException,
                    ServletException         {
   	    String login_user;
   	    /* Following block enables looging in through automation script
   	     * without user authentication.
   	     */    	    
		if (user == null)
   	    {
   	    	login_user = "harsha.matadhikari";
   	    }
   	    else
   	    {
   	    	login_user = user.getNickname();   	    	
   	    }
		/* We do the listing using the fileinfo table that we have 
		 * stored in datastore. FIles are stored however in cloudstore
		 */
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       	Key k3 = new KeyFactory.Builder("user", login_user).getKey();  	    
  	    Query q = new Query("fileinfo", k3);
   	    PreparedQuery pq = datastore.prepare(q); 
   	    outputHeader(user, req,resp); /* Header (User name and Sign Out)*/
        resp.getWriter().println("<i> So you want see the listing of your files "
						 + " ! Right? Here we go ... </i><br><br>");  
        
        resp.getWriter().println("<table border=1><tr><td>Filename</td><td>File Size in Bytes</td><td>File Store</td></tr> "); 
   	    for (Entity result : pq.asIterable()) {  
   	    	String filename = (String) result.getProperty("file-name");
   	    	String filesize = String.valueOf(
   	    	result.getProperty("file-contentlen"));
   	    	String filestore = (String) result.getProperty("file-store");
   	    	if (filestore.equals("CloudStore")) {
   	    		resp.getWriter().println("<tr><td>" + filename +
    							"</td><td>" + filesize + "</td>" + 
    							"</td><td>" + filestore + "</td>" + 
    	    					"</tr>");
   	    	}
   	    }
	    resp.getWriter().println("</table>");
	    outputFooter(user,req, resp);/* Footer; Navigational Links */
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
  	    // Check memCache first 
  	    Entity memEnt = (Entity) syncCache.get(filename);
  	    
  	    if(memEnt == null || memCacheEnable == false) { 
		/* Memcache is false;
		 * We have to retrieve from cloud */
		/* Using the datastore fileinfo check where is this
		 * file stored. We can download a file only from cloud store
		 * in this function.
		 * This complexity is beause we support DataStore also  and 
		 * have the same fileinfo table */
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
   	    		/* We found the file lets download */
   	   	    	filesize = String.valueOf(
   	   	   	    	result.getProperty("file-contentlen"));   	    		
   	            FileService fileService = FileServiceFactory.getFileService();   	    		
   	    		String cloud_filename = "/gs/" + BUCKETNAME + "/" + filename;
                AppEngineFile readableFile = new AppEngineFile(cloud_filename);
                FileReadChannel readChannel =
                    fileService.openReadChannel(readableFile, false);
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
                String memStr = ""; 
                boolean storeInMemCache = false ;
                if(Integer.valueOf(filesize ) < 1000*1000 && memCacheEnable == true ) {
                	// This file should have been in memCache
                	storeInMemCache = true ;
                }
                
                /* We read line by line 
                 * Also we add CR-LF which gets removed.
                 * This is not a good approach. Should read some bytes till 
                 * end of file. But that immplementation had some issues. 
                 * Temprararily we have this slow + hacked implentation 
                 * which we should relook later. A TODO.
                 */
                while ((str = reader.readLine()) != null) {
                	str += "\r\n";
                	if (storeInMemCache == true ) {
                		memStr = memStr + str ;	
                	}
               		outStream.write(str.getBytes(), 0, str.length());
               		complete_len += str.length();
                }        	
                if (storeInMemCache == true ) {
                	Entity d = new Entity(filename);
                	Blob blob = new  Blob(memStr.getBytes());
                   	d.setProperty("file-contentlen", Integer.valueOf(filesize ));
                   	d.setProperty("file-name", filename);
                   	d.setProperty("file-path", filename);
                   	
                   	d.setProperty("file-store", "CloudStore");
                   	d.setProperty("content", blob);
                	syncCache.put(filename, d);
                }
                readChannel.close();     	        
   	    	}
   	    }
  	    }else {
  	    	// File found in memCache, get the contents
  	    	//resp.getWrite().println("MemCache: File Found getting from here ");
  	    	 
  	    	 String str;
  	    	 Blob blob;
  	    	 int content_len ;
  	    	 found  = 1 ; 
  	    	 blob  = (Blob) memEnt.getProperty("content");
  	    	 byte[] buffer2 = blob.getBytes();
  	    	 content_len = (Integer) memEnt.getProperty("file-contentlen");
  	    	 filesize = String.valueOf(content_len);
  	    	 
  	    	String mimeType = "application/octet-stream";
	        	resp.setContentType(mimeType);
	            resp.setContentLength(buffer2.length );
	            String headerKey = "Content-Disposition";
	            String headerValue = String.format("attachment; filename=\"%s\"", filename);
	            resp.setHeader(headerKey, headerValue);   	        	
	            OutputStream outStream = resp.getOutputStream();
	            
  	    	if(content_len <= 1 ) { 
  	    		//resp.getWriter().println("<p>Content Len = " + content_len +" </p>");
  	    		outStream.write(((String)( "<p>Content Len = " + content_len +" </p>" 
  	    				+ "\n" + "Blob len " + buffer2.length )).getBytes());
  	    		
  	    	}else{
  	    		outStream.write(buffer2, 0,buffer2.length);	
  	    	}
  	    	
  	    }
   	    if (found == 0) {
   	   	    outputHeader(user, req,resp);   	    	
    	    resp.getWriter().println("<p>Filename = " + filename +
					                              " Not Found ...</p>");
    	    outputFooter(user,req, resp);     	    
   	    }
   	    /* Update the size of download and time taken in statistics */
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
            if ( syncCache.contains(filename)) { 
            	resp.getWriter().println("<i> " + filename + " exists </i><br><br>");  
            }else { 
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
                syncCache.put(filename1,result);
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
        if(syncCache.contains(filename)){
        	syncCache.delete(filename);
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

        resp.getWriter().println("<i> field_name = " + item.getFieldName() + "</i><br>");  
        resp.getWriter().println("<i> content-type = " + item.getContentType() + "</i><br>");  
        resp.getWriter().println("<i> is form field = " + item.isFormField() + "</i><br>");  
        resp.getWriter().println("<i> filename = " + item.getName() + "</i><br>");
        resp.getWriter().println("<i> content-length = " + req.getContentLength() + "</i><br>");
        if (memCacheEnable == true)
        {
        	resp.getWriter().println("<i> Memcache : Enabled </i><br>");        	
	        if(syncCache.get(item.getName()) == null ) {
	        	resp.getWriter().println("<i> Memcache : " + item.getName() + " Not found in memcache </i><br>");
	        }else { 
	        	resp.getWriter().println("<i> Memcache : " + item.getName() + " Found in memcache </i><br>");
	        }
        } else {
        	resp.getWriter().println("<i> Memcache : Disabled </i><br>");        	
        }
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
        
		Entity stats = statisticsStart("insert", item.getName());  
		
		
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
      	int content_len = req.getContentLength() ;  
        boolean storeInMemCache = false ;
        if((content_len < 1000*1000) && memCacheEnable == true) {
            storeInMemCache = true ;
        }
     
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
	// memcache update for these entries 
        if(storeInMemCache == true) { 
            Entity mEnt = new Entity(item.getName());
            Blob blob = new Blob(buffer);
                        
            mEnt.setProperty("file-contentlen", complete_len);
            mEnt.setProperty("file-name", item.getName());
            mEnt.setProperty("file-path", item.getName());
            mEnt.setProperty("file-store", "CloudStore");
            mEnt.setProperty("content", blob);
            syncCache.put(item.getName(),mEnt);
	}	
        // Now finalize
        lock = true;

        writeChannel =
                fileService.openWriteChannel(writableFile, lock);
       	writeChannel.closeFinally();

       	
		statisticsEnd(stats, complete_len);       	
        resp.getWriter().println("<br><i>File " +  item.getName() + " Added. Length =" + complete_len + "</i><br>" );
        
	}
	
}
