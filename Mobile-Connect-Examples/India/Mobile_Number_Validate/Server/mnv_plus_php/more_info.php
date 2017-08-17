<?php
if(isset($_GET['session_id'])) {
   session_id($_GET['session_id']);
}
session_start();

   echo $_SESSION['pcr'];
   echo $_SESSION['token_payload'];
   echo $_SESSION['userinfo_payload'];


?>
