package cs553.pa3jsp;

import java.io.IOException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Pa3BlobStore extends HttpServlet {
	public static final String BUCKETNAME = "cloud-prg-prg3";	
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException  {

		fileOpsUsingBlobStore(req,resp);
	}

	public void fileOpsUsingBlobStore(HttpServletRequest req,
												  HttpServletResponse resp)
			throws ServletException, IOException  {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        if (user != null) {
        	outputHeader(user,req,resp);
            resp.getWriter().println("<i> Blobstore not yet implemented </i><br><br>"); 
            outputFooter(user,req,resp);
        } else {
            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
        }		
	}
	
	public void outputHeader(User user,
			HttpServletRequest req,
			HttpServletResponse resp) 
	        		 throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.getWriter().println("<div id=\"welcome\">");
     	resp.getWriter().println("<h1>PA3 File Storage</h2>");
        resp.getWriter().println("</div>");
        
        resp.getWriter().println("<div id=\"Banner\">");
        resp.getWriter().println("<p> Hello " + user.getNickname() + "</p>");
			
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
        resp.getWriter().println("<a href=\"/index.jsp\">Go Back</a>.</p>");
        resp.getWriter().println("</div>");			
	}
	
}
