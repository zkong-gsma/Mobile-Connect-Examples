<%@page import="org.example.mobileconnect.*"%>
<%@ page language="java" import="java.util.*"%>
<%
	//require HttpServletRequest.getServletContext() to find base directory to load config.properties
	Discovery disc = new Discovery(new Config(request));

	//get client_ip here.
	String remoteAddr = "127.0.0.1";
	if (request.getHeader("x-forwarded-for") != null) {
		remoteAddr = request.getHeader("x-forwarded-for");
	} else {
		remoteAddr = request.getRemoteAddr();
	}

	//remoteAddr = "106.222.81.82";
	System.out.println(remoteAddr);
	System.out.println(request.getRemoteAddr());

	//Debug to make sure we obtain the correct client IP
	for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
		String attribName = (String) e.nextElement();
		System.out.println("<<<" + attribName + "|"
				+ request.getHeader(attribName));
	}

	response.setContentType("application/json");
	response.setCharacterEncoding("UTF-8");
	//Discovery response is true for both OperatorSelection or Positive match
	//further check if the response is Positive match. Not OperatorSelection.
	if (disc.PerformIPDiscovery(remoteAddr)
			&& disc.get_serving_operator() != null) {
		session.setAttribute("disc_response", disc);
		application.setAttribute(session.getId(), session);
		out.print("{\"status\":true, \"session_id\":\""
				+ session.getId() + "\"}");
	} else {
		out.print("{\"status\":false}");
	}
%>