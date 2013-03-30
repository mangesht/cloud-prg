package guestbook;

import java.io.IOException;
import javax.servlet.http.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
//import com.google.storage.onestore.v3.OnestoreEntity.User;

@SuppressWarnings("serial")
public class GuestbookServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String sl ;
		sl = new String ();
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        if (user != null) {
        	resp.setContentType("text/plain");
        	resp.getWriter().println("Hello, " + user.getNickname());
        	//CreateLogoutURL();
        	sl = userService.createLogoutURL(getServletInfo());
        	resp.getWriter().println(sl);
        }else{
        	resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
        }
	}
}
