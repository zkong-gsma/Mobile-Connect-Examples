<?php
#*************************
# Using Discovery IP based lookup to confirm if a network is from a correct operator.
# Running this in the backend before offering Mobile Connect Options to the user.
# if discovery resoinse returns true, you can follow up with the mobile connect request.
# optional to pass the source client IP as ?ip=xxx.xxx.xxx.xxx if you wish. else it will auto get the client's IP
# session_id is passed back so that if we invoke mobile connect next, we nolonger need to re-discover the operator.
#************************
session_start();
header('Content-Type: application/json');
include "mobileConnect.php";

$mc = new mobileConnect($config);

$ip = "";
if (isset($_GET["ip"])) {
   $ip = $_GET["ip"];
}
$response = $mc->PerformIPDiscovery($ip);


if (isset($response->serving_operator)) {
   $_SESSION['disc_response'] = $response;
   echo json_encode(Array("status"=>true, "session_id"=>session_id()), JSON_PRETTY_PRINT);
} else {
   echo json_encode(Array("status"=>false), JSON_PRETTY_PRINT);
}
?>
