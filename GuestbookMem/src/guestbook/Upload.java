// file Upload.java
package guestbook;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.FileService;


public class Upload extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
// New Code 
    	//ServletFileUpload upload = new ServletFileUpload();
    	
        Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
        BlobKey blobKey = blobs.get("myFile");
        
        if (blobKey == null) {
            res.sendRedirect("/");
        } else {
        	System.out.println("Key = " + blobKey.getKeyString());
          //  res.sendRedirect("/serve?blob-key=" + blobKey.getKeyString());
        	String guestbookName = req.getParameter("guestbookName");
        	res.sendRedirect("/guestbook.jsp?guestbookName=" + guestbookName);
        }
    }
}
