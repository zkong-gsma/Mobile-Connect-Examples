<?php
#*************************
# calling this after check_network.php returns true with the session_id passed as ?session_id=xxxxxxxxxxx&msisdn=XXXXXXXXXX
# and the MSISDN to be validated.
# we will be using the discovered endpoint from check_network.php (using session_id) instead of performing a new discovery 
# with a MSISDN.
#************************
if(isset($_GET['session_id'])) {
   session_id($_GET['session_id']);
}
session_start();
include "mobileConnect.php";

$mc = new mobileConnect($config);

$mc->parseDiscoveryResponse($_SESSION['disc_response']);
$auth_url = $mc->mobileconnectAuthenticate($_GET['msisdn']);
$_SESSION['mc_obj'] = serialize($mc);
header("Location:".$auth_url);
exit;
?>
