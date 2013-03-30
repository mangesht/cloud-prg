package guestbook;

import java.io.IOException;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.logging.Logger;

import javax.servlet.http.*;
import javax.servlet.http.*;

import org.mortbay.log.Log;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
//import com.google.storage.onestore.v3.OnestoreEntity.User;

@SuppressWarnings("serial")
public class GuestbookServlet extends HttpServlet {
	public static final String BUCKETNAME = "cloud-prg-prg3";
	public static final String FILENAME = "f.txt";
	private static final Logger log = Logger.getLogger(GuestbookServlet.class.getName());
/*
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
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException {
		resp.setContentType("text/plain");
	    resp.getWriter().println("Hello, world from java");
	 
	    FileService fileService = FileServiceFactory.getFileService();
	    GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
	       .setBucket(BUCKETNAME)
	       .setKey(FILENAME)
	       .setMimeType("text/html")
	       .setAcl("public-read");
	      // .addUserMetadata("myfield1", "my field value");
	    
	    AppEngineFile writableFile =
	            fileService.createNewGSFile(optionsBuilder.build());
	 // Open a channel to write to it
	     boolean lock = false;
	     FileWriteChannel writeChannel =
	         fileService.openWriteChannel(writableFile, lock);
	     // Different standard Java ways of writing to the channel
	     // are possible. Here we use a PrintWriter:
	     PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
	     out.println("The woods are lovely dark and deep.");
	     out.println("But I have promises to keep.");
	     // Close without finalizing and save the file path for writing later
	     out.close();
	     String path = writableFile.getFullPath();
	     log.info("File saved at " + path );
	     // Write more to the file in a separate request:
	     writableFile = new AppEngineFile(path);
	     // Lock the file because we intend to finalize it and
	     // no one else should be able to edit it
	     lock = true;
	     writeChannel = fileService.openWriteChannel(writableFile, lock);
	     // This time we write to the channel directly
	     writeChannel.write(ByteBuffer.wrap
	               ("And miles to go before I sleep.".getBytes()));

	     // Now finalize
	     writeChannel.closeFinally();
	     resp.getWriter().println("Done writing...");

	     // At this point, the file is visible in App Engine as:
	     // "/gs/BUCKETNAME/FILENAME"
	     // and to anybody on the Internet through Cloud Storage as:
	     // (http://storage.googleapis.com/BUCKETNAME/FILENAME)
	     // We can now read the file through the API:
	     String filename = "/gs/" + BUCKETNAME + "/" + FILENAME;
	     AppEngineFile readableFile = new AppEngineFile(filename);
	     FileReadChannel readChannel =
	         fileService.openReadChannel(readableFile, false);
	     // Again, different standard Java ways of reading from the channel.
	     BufferedReader reader =
	             new BufferedReader(Channels.newReader(readChannel, "UTF8"));
	     String line = reader.readLine();
	     resp.getWriter().println("READ:" + line);

	    // line = "The woods are lovely, dark, and deep."
	     readChannel.close();
	
	}

}