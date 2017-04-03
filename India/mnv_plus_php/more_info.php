<?php
if(isset($_GET['session_id'])) {
   session_id($_GET['session_id']);
}
session_start();

print_r($_SESSION);

?>
