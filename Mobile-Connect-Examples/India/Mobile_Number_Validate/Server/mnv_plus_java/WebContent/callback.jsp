<%@page import="org.example.mobileconnect.*"%>
<%
	MobileConnect mc;
	if (session.getAttribute("mc_obj") != null) {
		mc = (MobileConnect) session.getAttribute("mc_obj");
	} else {
		mc = new MobileConnect(new Discovery(new Config(request)));
	}

	mc.process_callback(request);

	//complete state is true if we get the PCR.
	if (mc.get_complete_state()) {
		String pcr = mc.get_pcr();
		//treating JSESSIONID as the API access token
		session.setAttribute("pcr", pcr);
		session.setAttribute("token_payload", mc.get_token_payload_string());
		session.setAttribute("userinfo_payload",
				mc.get_userinfo_payload_string());
		//Please include your own API to generate a Accesstoken for your Mobile or set WEB Session for your desktop
		//once those has been set, all you need to do is a 302 redirect to the main/landing page.
		//Checking the hased MSISDN to prevent MITM attack
		if (mc.hashed_msisdn_match()) {
			response.sendRedirect("http://complete/"
					+ mc.get_complete_msg());
			return;
		}

	} else {
		session.invalidate();
	}

	response.sendRedirect("http://complete/?state=-1");
%>