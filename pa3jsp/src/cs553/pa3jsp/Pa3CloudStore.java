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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;


import com.google.appengine.api.files.AppEngineFile;
/*import com.google.appengine.api.files.FileReadChannel;*/
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;
import java.io.PrintWriter;
import java.nio.channels.Channels;

@SuppressWarnings("serial")
public class Pa3CloudStore extends HttpServlet {
        public static final String BUCKETNAME = "cloud-prg-prg3";      
       
        public void doPost(HttpServletRequest req, HttpServletResponse resp)
                        throws ServletException, IOException  {

                fileOpsUsingCloudStore(req,resp);
        }

        public void fileOpsUsingCloudStore(HttpServletRequest req,
                                                                                                  HttpServletResponse resp)
                        throws ServletException, IOException  {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        if (user != null) {
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
                        }
                    }
                } catch (IOException e) {
                        throw new ServletException("Cannot parse multipart request: " + e.getMessage());
                }
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
        resp.getWriter().println("<h1>PA3 File Storage -- Using CloudStore</h2>");
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
        resp.getWriter().println("<a href=\"/pa3_cloud.jsp\">Go Back</a>.</p>");
        resp.getWriter().println("</div>");                    
        }
       
        public void performListing(User user,
            HttpServletRequest req,
            HttpServletResponse resp)
                          throws IOException,
                    ServletException         {
               
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key k3 = new KeyFactory.Builder("user", user.getNickname()).getKey();      
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
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key k2 = new KeyFactory.Builder("user", user.getNickname())
                   .addChild("fileinfo", filename)
                   .getKey();      
            Query q = new Query(k2);
            PreparedQuery pq = datastore.prepare(q);
            for (Entity result : pq.asIterable()) {  
                found = 1;
                String filestore = (String) result.getProperty("file-store");
                                       
                if (filestore.equals("BlobStore"))      {
                        /* Blobstore retrieval */
                } else if (filestore.equals("DataStore")) {
                        /* Datastore retieval */
                } else if (filestore.equals("CloudStore")) {
                        /* Cloudstore retieval */
                            /*
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
                */
                }
            }
           
            if (found == 0) {
                    outputHeader(user, req,resp);              
            resp.getWriter().println("<p>Filename = " + filename +
                                                                      " Not Found ...</p>");
            outputFooter(user,req, resp);          
            }  
   
        }
       
        public void performCheck(User user,
            HttpServletRequest req,
            HttpServletResponse resp)
                          throws IOException,
                    ServletException         {
                int found = 0;
            String filename=req.getParameter("file_name");      
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            outputHeader(user, req,resp);
        Key k2 = new KeyFactory.Builder("user", user.getNickname())
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
            String filename=req.getParameter("file_name");      
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key k2 = new KeyFactory.Builder("user", user.getNickname())
                   .addChild("fileinfo", filename)
                   .getKey();      
       
            Query q = new Query(k2);
            PreparedQuery pq = datastore.prepare(q);
            outputHeader(user, req,resp);          
        resp.getWriter().println("<i> So you want to remove the file "
                                                 + filename + " ! Right? Here we go ... </i><br><br>");  
            for (Entity result : pq.asIterable()) {  
                String filestore = (String) result.getProperty("file-store");
               
                if (filestore.equals("BlobStore"))      {
                        resp.getWriter().println("<i> File is stored in Blobstore, Please relogin after selecting Blobstore </i><br>");
                } else if (filestore.equals("DataStore")) {
                        resp.getWriter().println("<i> File is stored in Datastore, Please relogin after selecting Blobstore </i><br>");
                } else if (filestore.equals("CloudStore")) {
                        resp.getWriter().println("<i> File is stored in Cloudstore, Deleting the file there. </i><br>");
                }
            }
           
        datastore.delete(k2);
            outputFooter(user,req, resp);          
        }      
       
        public void performInsert(User user,
                                          HttpServletRequest req,
                                          HttpServletResponse resp)
                                                          throws IOException,
                                                  ServletException         {
        // Create a factory for disk-based file items
            outputHeader(user, req,resp);              
        resp.getWriter().println("<i> So you want do the operation " + "insert" + " ! Right?</i><br>");                
        try {
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator iter = upload.getItemIterator(req);
               
                while (iter.hasNext()) {
                        FileItemStream item = iter.next();
                        if (item.isFormField()) continue;
                        storeInStore(user.getNickname(), item, req,resp);
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
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key k3 = new KeyFactory.Builder("user", userName).addChild("fileinfo", item.getName()).getKey();
        Entity  d;
               
        try {
                  d = datastore.get(k3);
        } catch (EntityNotFoundException ex) {
                  d = new Entity(k3);
                  resp.getWriter().println("<br><i>new fileinfo into datastore</i><br>" );                        
        }

       
        resp.getWriter().println("<i>File Service </i><br>" );

        FileService fileService = FileServiceFactory.getFileService();
       
        resp.getWriter().println("<i>options Builder </i><br>" );

        GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
           .setBucket(BUCKETNAME)
           .setKey(item.getName())
           .setMimeType("text/plain")
           .setAcl("public-read");
        resp.getWriter().println("<i>Writable File </i><br>" );

        AppEngineFile writableFile =
                fileService.createNewGSFile(optionsBuilder.build());
       
     
        boolean lock = false;
        resp.getWriter().println("<i>Writable Channel  with lock false</i><br>" );

        FileWriteChannel writeChannel =
                fileService.openWriteChannel(writableFile, lock);
        resp.getWriter().println("<i>Printwriter </i><br>" );

        PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
       
        resp.getWriter().println("<i>Store Contents</i><br>" );

        while ((len = stream.read(buffer, complete_len, (buffer.length-complete_len))) != -1) {
            complete_len += len;
                resp.getWriter().println("<br><i>File Chunk " + chunk_count +
                                " with lenth " + len + "</i><br>" );
                chunk_count++;
                if (complete_len == buffer.length) {
                        out.println(buffer.toString());
                        complete_len = 0;
                }              
        }
       
        out.close();
        resp.getWriter().println("<i>Store Path in filinfo</i><br>" );

        String path = writableFile.getFullPath();
       
        d.setProperty("file-contentlen", complete_len);
        d.setProperty("file-name", item.getName());
        d.setProperty("file-path", new Blob(path.getBytes()));
        d.setProperty("file-store", "CloudStore");              
        datastore.put(d);              
        // Now finalize
        resp.getWriter().println("<i>Finally Close</i><br>" );
        lock = true;
        resp.getWriter().println("<i>Writable Channel with lock true</i><br>" );

        writeChannel =
                fileService.openWriteChannel(writableFile, lock);
        writeChannel.closeFinally();
        resp.getWriter().println("<br><i>File " +  item.getName() + " Added. Length =" + complete_len + "</i><br>" );
       
        }
       
}

