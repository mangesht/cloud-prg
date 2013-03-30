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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class Pa3jspServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
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
            	// Create a factory for disk-based file items
                resp.getWriter().println("<i> So you want do the operation " + "insert" + " ! Right?</i><br>");             	
            	try {
            	
            		ServletFileUpload upload = new ServletFileUpload();
            		FileItemIterator iter = upload.getItemIterator(req);
            		
            		while (iter.hasNext()) {
            			FileItemStream item = iter.next();
            			if (item.isFormField()) continue;
            				storeInDataStore(item, req,resp);
            		}
            	} catch (FileUploadException e) {
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
	
	public void storeInDataStore(FileItemStream item, 
								 HttpServletRequest req, 
								 HttpServletResponse resp) throws IOException {
		
		InputStream stream = item.openStream();
		
        resp.getWriter().println("<i> ops = " + "insert" + "</i><br>");
        resp.getWriter().println("<i> field_name = " + item.getFieldName() + "</i><br>");  
        resp.getWriter().println("<i> content-type = " + item.getContentType() + "</i><br>");  
        resp.getWriter().println("<i> is form field = " + item.isFormField() + "</i><br>");  
        resp.getWriter().println("<i> filename = " + item.getName() + "</i><br>");

        int len;
        byte[] buffer = new byte[8192];
        String str = "";
        while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
            resp.getWriter().println("<br><i>Length Read=" + len + "</i><br>" );
       	    str = "";
       	    for(int i = 0; i < len; i++)
            {
                str += (char)buffer[i];
            }
       	    resp.getWriter().println(str);

        }		 
	}
}
