package cs553.pa3jsp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class Pa3StoreSelect extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException  {
		doPost(req, resp);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException  {
		String store=req.getParameter("store");
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        if (user != null) {
        	if (store.equals("cloud")) {
        		resp.sendRedirect("pa3_cloud.jsp");
        	} else if (store.equals("datastore")) {
        		resp.sendRedirect("pa3_datastore.jsp");
        	} else if (store.equals("blobstore")) {
        		resp.sendRedirect("pa3_blobstore.jsp");
        	}
        } else {
                 resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
        }	
        
	}
}
