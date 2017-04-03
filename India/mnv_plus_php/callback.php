<?php
#*************************
# sharing the same session_id since the begining of checl_network.php
# the client can then further request for more iformation seperately by using the same token.
#************************
session_start();
include "mobileConnect.php";

$mc = new mobileConnect($config);
if (isset($_SESSION['mc_obj'])) {
   $mc = unserialize($_SESSION['mc_obj']);
}

$mc->callback($_GET);

//complete state is true if we get the PCR.
if($mc->get_complete_state()) {
   $pcr = $mc->get_pcr();
   //treating PHPSESSID as the API access token
   $_SESSION['pcr'] = $pcr;
   $_SESSION['token_payload'] = $mc->get_token_payload();
   $_SESSION['userinfo_payload'] = $mc->get_userinfo_payload();
   //Please include your own API to generate a Accesstoken for your Mobile or set WEB Session for your desktop
   //once those has been set, all you need to do is a 302 redirect to the main/landing page.
} else {
  session_destroy();
}


header("Location: http://complete/");
exit;
?>
