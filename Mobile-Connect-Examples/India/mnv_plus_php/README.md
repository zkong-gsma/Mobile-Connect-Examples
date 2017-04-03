# India MNV Plus

There are 6 main File in this PHP example
should be used in conjunction of the Android client.

mobileConnect.php
...The is the main class object that contains all the function required to perform mobile connect requests.
config.php
...A simple file that contains the discovery credentials and endpoint that will be used by the main class file.
check_network.php
...A simple way to perform IP based discovery lookup to check if a Mobile network is on a Mobile Connect enabled network.
start_mc.php
...Construct the Mobile Connect Auth URL and redirect the "User-Agent" to it.
callback.php
...Manage the callback of the OIDC requests. 
more_info.php
...Simple example of how to extract data from your serverside using PHPSESSION

### High Level Flow.
1. Client calls in the backend "check_network.php" to determin if a client is on a valid Mobile Connect network. Returns JSON true/false.
2. If return True, along side is a session_id parameter that is the "PHPSESSID" to be used for the remaining request.
3. Start request to "start_mc.php?session_id=xxxxx&msisdn=91XXXXXXXX". This will create the Auth URL.
4. Start a Webview and load the Auth_url
5. Webview will handle all the redirect and callback and will end-up at "callback.php" where it will perform a final redirect to "http://complete"
6. Webview will intercept the "http://complete" and close the webview.
7. Clinet then further gets information from "more_info.php" to deterime the details of the MNV session. Eg, if its successful, or even other information.
