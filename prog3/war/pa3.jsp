<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
<title> PA3 File Storage </title>
<script type="text/javascript">
function update_file_size()
{
	alert(file_size.file[0].size);
}
</script>
</head>

<body>

<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
      pageContext.setAttribute("user", user);
%>

<div id="welcome">
<h1>PA3 File Storage</h2>
</div>

<div id="welcome">
<p>Hello, ${fn:escapeXml(user.nickname)}! Please do a file operation.</p>
</div>

<div id="logout">
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">Sign out</a>.</p>
</div>

<div id="ops">
<form action="/fops" enctype="multipart/form-data" method="post">
    <div>
        <input name="file_name" type="file"  value="Select a File" size="40" onchange="update_file_size()"> </input> &nbsp;
        <input type="submit" value="Submit" />
        <input type="hidden" name="fun" value="insert" />
        <input type="hidden" name="file_size" value="insert" />
    </div>
</form>

<form action="/fops" method="post">
    <div>
        <input name="file_name" type="text"></input> &nbsp;
        <input type="submit" value="Check File"/>
        <input type="hidden" name="fun" value="check" />
    </div>
</form>

<form action="/fops" method="post">
    <div>
        <input name="file_name" type="text"></input> &nbsp;
        <input type="submit" value="Find File"/>
        <input type="hidden" name="fun" value="find"/>
    </div>
</form>

<form action="/fops" method="post">
    <div>
        <input name="file_name" type="text"></input> &nbsp;
        <input type="submit" value="Remove File"/>
        <input type="hidden" name="fun" value="remove"/>
    </div>
</form>

<form action="/fops" method="post">
    <div> 
        <input type="submit" value="listing"/>
        <input type="hidden" name="fun" value="listing" />
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
