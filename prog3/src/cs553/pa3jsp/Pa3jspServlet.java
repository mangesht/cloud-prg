package cs553.pa3jsp;

import java.io.IOException;
import javax.servlet.http.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyFactory.Builder;

@SuppressWarnings("serial")
public class Pa3jspServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException  {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        if (user != null) {
    
        	boolean isMultipart = ServletFileUpload.isMultipartContent(req);
            resp.setContentType("text/html");
            resp.getWriter().println("<div id=\"logout\">");
            resp.getWriter().println("<a href=\"<%= userService.createLogoutURL(request.getRequestURI()) %>\">sign out</a>.</p>");
            resp.getWriter().println("</div>");
            
            resp.getWriter().println("<div id=\"welcome\">");
            resp.getWriter().println("<h1> Hello " + user.getNickname() + "</h1>");
            resp.getWriter().println("</div>");
            
            resp.getWriter().println("<div id=\"ops\">");
            if (isMultipart == true) {
            	try {
            	performInsert(user, req,resp);
            	} catch (IOException e) {
            		throw new ServletException("Cannot parse multipart request: " + e.getMessage());
            	}
            } else {
                	
                String ops=req.getParameter("fun");
            	String filename = req.getParameter("file_name");            		
            	resp.getWriter().println("<i> ops = " + ops + "</i><br>");
            	resp.getWriter().println("<i> filename = " + filename + "</i><br>");
            }
            	    
            resp.getWriter().println("</div>");
       	    resp.getWriter().println("<div id=\"nav\">");
            resp.getWriter().println("<a href=\"/pa3.jsp\">Go Back</a>.</p>");
            resp.getWriter().println("</div>");		
        } else {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
        }

	}
	
	public void performInsert(User user,
			                  HttpServletRequest req, 
			                  HttpServletResponse resp)
			                		  throws IOException,
			                          ServletException         {
    	// Create a factory for disk-based file items
        resp.getWriter().println("<i> So you want do the operation " + "insert" + " ! Right?</i><br>");             	
    	try {
    		ServletFileUpload upload = new ServletFileUpload();
    		FileItemIterator iter = upload.getItemIterator(req);
    		
    		while (iter.hasNext()) {
    			FileItemStream item = iter.next();
    			if (item.isFormField()) continue;
    				storeInSomeStore(user.getNickname(), item, req,resp);
    		}
    	} catch (FileUploadException e) {
    		throw new ServletException("Cannot parse multipart request: " + e.getMessage());
    	}catch (EntityNotFoundException e) {
    		throw new ServletException("Cannot parse multipart request: " + e.getMessage());
    	}
    	
    } 
	
	public void storeInSomeStore(String userName,
								 FileItemStream item, 
								 HttpServletRequest req, 
								 HttpServletResponse resp) throws IOException,
								  EntityNotFoundException {

		int contentLength;
		int ONE_MB = 1024*1024;
		
        resp.getWriter().println("<i> ops = " + "insert" + "</i><br>");
        resp.getWriter().println("<i> field_name = " + item.getFieldName() + "</i><br>");  
        resp.getWriter().println("<i> content-type = " + item.getContentType() + "</i><br>");  
        resp.getWriter().println("<i> is form field = " + item.isFormField() + "</i><br>");  
        resp.getWriter().println("<i> filename = " + item.getName() + "</i><br>");
        resp.getWriter().println("<i> content-length = " + req.getContentLength() + "</i><br>");
        
        contentLength = req.getContentLength();
        
        if (contentLength < ONE_MB)
        {
        	storeInDataStore(userName, item, req, resp);
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
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key k = KeyFactory.createKey("user",  userName);
       	Entity e =  datastore.get(k);
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
       	
        String str = "";
        while ((len = stream.read(buffer, complete_len, (buffer.length-complete_len))) != -1) {
       	    //str = "";
       	    //String content_property = "file-content-" + String.valueOf(chunk_count);
       	    //String content_len_property = "file-contentlen-" + String.valueOf(chunk_count);
       	    
       	    //for(int i = 0; i < len; i++)
            //{
            //    str += (char)buffer[i];
            //}
            
           	//f.setProperty(content_property, str);
           	//f.setProperty(content_len_property, len);
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
       	datastore.put(f);
       	datastore.put(d);       	
        resp.getWriter().println("<br><i>File " +  item.getName() + "Added. Length =" + complete_len + "</i><br>" );
	}
}
