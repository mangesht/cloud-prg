package guestbook;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.*;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;
import java.util.List; 

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SignGuestbookServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(SignGuestbookServlet.class.getName());
	private Entity entity;
	public String message; 
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        /*
        String content = req.getParameter("content");
        if (content == null){ 
        	content = "(No greeting)";
        }
        if(user != null){ 
        	log.info("Greeting posted by user " + user.getNickname() + ": " + content);
        }else{
        	 log.info("Greeting posted anonymously: " + content); 
        }
        resp.sendRedirect("/guestbook.jsp"); 
        */
        String guestbookName = req.getParameter("guestbookName");
        log.info("GuestbookName " + guestbookName );
        Key guestbookKey = KeyFactory.createKey("Guestbook", guestbookName);
        String content = req.getParameter("content");
        
        Date date = new Date();
        message = new String();
        message =  "Mangesh";
        Entity greeting = new Entity("Greeting", guestbookKey);
        greeting.setProperty("user", user);
        greeting.setProperty("date", date);
        greeting.setProperty("content", content);
        log.info("Content" + content); 
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        //syncCache.
        //syncCache .put(content, 1);
        log.info("Putting info " + greeting);
        Entity f ;
        Filter contF = new FilterPredicate("content",FilterOperator.EQUAL,content);
        Query q = new Query("Person").setFilter(contF);
     // Use PreparedQuery interface to retrieve results
        PreparedQuery pq = datastore.prepare(q);
      //  f = datastore.get(greeting.);
        if(syncCache.get(content)== null) {
        Key contKey = KeyFactory.createKey("Guestbook", guestbookName);
       // Key contKey = KeyFactory.createKey("Entity", content);
        //Query query = new Query("Greeting", contKey);//. addSort("date", Query.SortDirection.DESCENDING);
        Query query = new Query("Greeting").addFilter("content", FilterOperator.EQUAL, content);//. addSort("date", Query.SortDirection.DESCENDING);
        List<Entity> greetings1 = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(20));
        System.out.println("Number of matching entries" + greetings1.size() + req.getParameter("NAME"));
       
         for (Entity result :greetings1) {
        	//  String firstName = (String) result.getProperty("user");
        	 // String lastName = (String) result.getProperty("content");
        	  //Long height = (Long) result.getProperty("height");

        	  //System.out.println(firstName + " " + lastName + ", " + " inches tall");
        	}
        if(greetings1.size() == 0){ 
        	
        	datastore.put(greeting);
        	syncCache.put(greeting.getProperty("content"), 1);
        	message = "Added ";
        }else {
        	
        	message = "Found in data store Not added ";
        }
        syncCache.put(greeting.getProperty("content"), 1);
        }else{
        	message = "Found in memcache Not added ";
        }
        resp.getWriter().write(message);
        System.out.println(message);
        resp.sendRedirect("/guestbook.jsp?guestbookName=" + guestbookName);
        
    }
}
