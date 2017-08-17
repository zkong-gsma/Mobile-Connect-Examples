<%@page import="org.example.mobileconnect.*"%>
<%@ page language="java" import="java.util.*"%>
<%
	if (request.getParameter("session_id") != null) {
		//resume session from a given Session parameter.
		//Note, old session Object will be replace with current if found
		HttpSession temp_session = application.getAttribute(request
				.getParameter("session_id")) == null ? request
				.getSession() : (HttpSession) application
				.getAttribute(request.getParameter("session_id"));

		//the Session temporary stored on Application object is removed to prevent overflow.
		application.removeAttribute(request.getParameter("session_id"));
		session.setAttribute("disc_response", (Discovery) temp_session.getAttribute("disc_response"));
	}

	//This is to protect application Object ooverflow with unclaimed session
	//if a Stored session is older than 30 seconds, remove from application
	//Assumption is that a request should not take more than 30 seconds to complete.
	//can tweak to be a smaller value.
	for (Enumeration e = application.getAttributeNames(); e
			.hasMoreElements();) {
		String attribName = (String) e.nextElement();
		Object attribObj = application.getAttribute(attribName);
		if (attribObj instanceof HttpSession) {
			HttpSession temp = (HttpSession) attribObj;
			if ((temp.getCreationTime() + 300000) < System
					.currentTimeMillis()) {
				application.removeAttribute(attribName);
				System.out.println("<<<" + attribName
						+ "| removed because its old.");
			}
		}
	}

	//if session successfully restored from previous, "disc_response" should exist.
	//else auth can't proceed.
	if (session.getAttribute("disc_response") != null) {
		MobileConnect mc = new MobileConnect(
				(Discovery) session.getAttribute("disc_response"));
		session.removeAttribute("disc_response");
		session.setAttribute("mc_obj", mc);
		response.sendRedirect(mc.mobileconnectAuthenticate(request.getParameter("msisdn")));
	} else {
		out.print("Something went wrong! Session is lost. Please check network again to obtain new session_id");
	}
%>