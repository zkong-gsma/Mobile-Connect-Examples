# India MNV Plus

Should be used in conjunction of the Android client.
(before use, please update the endpoints)

Similar to the php Serverside flow.
there is 3 Main JSP here.


* check_network.jsp
  *A simple way to perform IP based discovery lookup to check if a Mobile network is on a Mobile Connect enabled network.
* start_mc.jsp
  *Construct the Mobile Connect Auth URL and redirect the "User-Agent" to it.
* callback.jsp
  *Manage the callback of the OIDC requests. 

The configuration file of "config.properties" where credential, endpoint and redirect_uri lives under the WEB-INF directory.
Please configure before use.

### High Level Flow.
1. Client calls in the backend "check_network.jsp" to determin if a client is on a valid Mobile Connect network. Returns JSON true/false.
2. If return True, along side is a session_id parameter that is the "JSESSIONID" to be used for the remaining request.
3. Start request to "start_mc.jsp?session_id=xxxxx&msisdn=91XXXXXXXX". This will create the Auth URL.
4. Start a Webview and load the Auth_url
5. Webview will handle all the redirect and callback and will end-up at "callback.jsp" where it will perform a final redirect to "http://complete"
6. Webview will intercept the "http://complete" and close the webview.

Note:
unlike PHP where a given PHPSESSID can be easily resumed, in java, there is no concept of resuming a given session. In this example, we have adopted a "quick & dirty" way of temporary storing JESSIONID under the Serverlet Application Object.

This is to a guide to show how it can work. Will recommended to impliment a RDBMS session_id handling

also included in this directory is a deployable WAR file.
