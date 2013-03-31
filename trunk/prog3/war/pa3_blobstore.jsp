<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.EntityNotFoundException" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
<title> PA3 File Storage </title>
</head>

<body>

<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    String store;
    store = request.getParameter("store");
    if (user != null) {
      pageContext.setAttribute("user", user);
%>

<div id="welcome">
<h1>PA3 File Storage -- Using <%= (request.getParameter("store")) %></h2>
</div>

<%
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Key k = KeyFactory.createKey("user",  user.getNickname());
      try {
      Entity e =  datastore.get(k);
      } catch (EntityNotFoundException e1) {
%>
<div id="Banner">
<p>You are a new user.</p>
<p>Your name is being added as User entity in the datastore.</p>
</div>
<%    
      	Entity e = new Entity("user", user.getNickname());
      	datastore.put(e);
      }	
%>
<div id="Banner">
<p>Hello, ${fn:escapeXml(user.nickname)}! </p>
</div>

<div id="logout">
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">Sign out</a>.</p>
</div>

<div id="briefing">
<p>Here you can insert a file, lookup a file, download a file and see the listing of your files </p>
<p>Select your operation please </p>
</div>

<div id="ops">
<form action="fops_blobstore" enctype="multipart/form-data" method="post">
    <div>
        <input name="file_name" type="file"  value="Select a File" size="40">
        </input> &nbsp;
        <input type="hidden" name="fun" value="insert" />
        <input type="hidden" name="file_size" value="insert" />
        <input type="submit" value="Submit" />
    </div>
</form>

<form action="fops_blobstore" method="post">
    <div>
        <input name="file_name" type="text"></input> &nbsp;
        <input type="hidden" name="fun" value="check" />
        <input type="submit" value="Check File"/>
    </div>
</form>

<form action="fops_blobstore" method="post">
    <div>
        <input name="file_name" type="text"></input> &nbsp;
        <input type="hidden" name="fun" value="find"/>
        <input type="submit" value="Find File"/>
    </div>
</form>

<form action="fops_blobstore" method="post">
    <div>
        <input name="file_name" type="text"></input> &nbsp;
        <input type="hidden" name="fun" value="remove"/>
        <input type="submit" value="Remove File"/>
    </div>
</form>

<form action="fops_blobstore" method="post">
    <div> 
        <input type="hidden" name="fun" value="listing" />
        <input type="submit" value="Listing"/>
    </div>
</form>

</div>
<%
    } else {
%>
<p>Hello! Please 
<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
 to continue.</p>
<%
    }
%>

</body>
</html>
