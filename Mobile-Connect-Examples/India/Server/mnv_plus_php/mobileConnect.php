<?php
include "config.php";
class mobileConnect {
   // discovery used parameter
   private $discovery_ep;
   private $discovery_client_id;
   private $discovery_client_secret;
   private $redirect_uri;

   // mobileconnect used parameter
   private $scope = "openid+mc_mnv_validate_plus+mc_identity_phonenumber_hashed";
   private $acr_values = "2";
   private $max_age = "3600";
   private $prompt = "login";

   private $operatorSelection;
   private $ttl;
   private $login_hint;

   private $oidc_state;
   private $oidc_nonce;
   private $oidc_auth_ep;
   private $oidc_token_ep;
   private $oidc_userinfo_ep;
   private $oidc_client_id;
   private $oidc_client_secret;

   private $mnv_msisdn;

   private $pcr;
   private $token_payload;
   private $complete_msg;
   private $userinfo_payload = "{}";
   private $mc_state = false;
   private $logfile = "./log.txt";
   
   // Initiate Class
   // store information into class
   function mobileConnect($argv1) {
      foreach($argv1 as $k => $v) {
         if ($k == "discovery_url") {
            $this->discovery_ep = $v;
         } else if ($k == "discovery_client_id") {
            $this->discovery_client_id = $v;
         } else if ($k == "discovery_client_secret") {
            $this->discovery_client_secret = $v;
         } else if ($k == "redirect_uri") {
            $this->redirect_uri = $v;
         }

      }
   } 


   // Function to perform discovery.
   // pass parameter true to always perform discovery.
   // else it will check for session/cachaed information to attempt mobile connect.
   public function discover($argv = false) {
      // if $argv=true, force re-discovery
      // unset all cached result..
      if ($argv) {
         $this->clearCache();
      }

      if (empty($this->ttl)) {
         // no cached information found.
         // perform new discovery
         $response = $this->PerformGetDiscovery();
         if ($this->parseDiscoveryResponse($response)) {
            if (empty($this->operatorSelection)) {
               $this->mobileconnectAuthenticate();
            } else {
               header("Location: ".$this->operatorSelection);
            }
         } else {
            // something went wrong
            // bad Discovery Respones
            echo "Something Went Wrong [Bad discovery response]";
         }
      } else {
         // there is cached information
         // check if cached information has expired.
         if ($this->ttl < time()) {
            $this->discover();
         } else {
            $this->mobileconnectAuthenticate();
         }
      }
   }


   // function to construct the Mobile Connect Authentication URL and redirect to it,
   public function mobileconnectAuthenticate($argv1 = "") {
      $this->oidc_state = "state".substr(base64_encode(md5( mt_rand() )), 0, 13);
      $this->oidc_nonce = "nonce".substr(base64_encode(md5( mt_rand() )), 0, 13);

      $url = $this->oidc_auth_ep;
      $url .= "?client_id=" . $this->oidc_client_id;
      $url .= "&scope=" . $this->scope;
      $url .= "&redirect_uri=" . urlencode($this->redirect_uri);
      $url .= "&response_type=code";
      $url .= "&state=" . $this->oidc_state;
      $url .= "&nonce=" . $this->oidc_nonce;
      $url .= "&acr_values=" . $this->acr_values;
      if (!empty($this->max_age)) {
         $url .= "&max_age=" . $this->max_age;
      }
      if (!empty($this->prompt)) {
         $url .= "&prompt=" . $this->prompt;
      }
      if (!empty($this->login_hint)) {
         $url .= "&login_hint=ENCR_MSISDN:" . $this->login_hint;
      }
      if (strlen($argv1) > 0) {
         $url .= "&login_hint=MSISDN:" . $argv1;
         $this->mnv_msisdn =  $argv1;
      }

      #header("Location: ".$url);
      return $url;
   }


   // perform Discovery GET request
   private function PerformGetDiscovery($argv1 = "") {
      $url = $this->discovery_ep . "?Redirect_URL=" . $this->redirect_uri;
      if (!empty($argv1)) {
         $url .= $argv1;
      }

      $ip = '127.0.0.1';
      if (!empty($_SERVER['HTTP_CLIENT_IP'])) {
         $ip = $_SERVER['HTTP_CLIENT_IP'];
      } elseif (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
         $ip = $_SERVER['HTTP_X_FORWARDED_FOR'];
      } else {
         $ip = $_SERVER['REMOTE_ADDR'];
      }

      $headers = array('Accept: application/json', 'x-source-ip: '.$ip);

      $ch = curl_init($url);
      curl_setopt($ch, CURLOPT_HEADER, 0);
      curl_setopt($ch, CURLOPT_USERPWD, $this->discovery_client_id . ":" . $this->discovery_client_secret);
      curl_setopt($ch, CURLOPT_FOLLOWLOCATION, false);
      curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

      $result = json_decode(curl_exec($ch));
      curl_close($ch);

      return $result;
   }


   // perform Discovery GET request
   public function PerformIPDiscovery($argv1 = "") {
      $url = $this->discovery_ep . "?Redirect_URL=" . $this->redirect_uri;
      if (!empty($argv1)) {
         $url .= $argv1;
      }

      $ip = '127.0.0.1';
      if (!empty($_SERVER['HTTP_CLIENT_IP'])) {
         $ip = $_SERVER['HTTP_CLIENT_IP'];
      } elseif (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
         $ip = $_SERVER['HTTP_X_FORWARDED_FOR'];
      } else {
         $ip = $_SERVER['REMOTE_ADDR'];
      }

      if (strlen($argv1) > 0) {
         $ip = $argv1;
      }

      $headers = array('Accept: application/json', 'x-source-ip: '.$ip);

      $ch = curl_init($url);
      curl_setopt($ch, CURLOPT_HEADER, 0);
      curl_setopt($ch, CURLOPT_USERPWD, $this->discovery_client_id . ":" . $this->discovery_client_secret);
      curl_setopt($ch, CURLOPT_FOLLOWLOCATION, false);
      curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

      $result = json_decode(curl_exec($ch));
      curl_close($ch);

      return $result;
   }

   // Generic CURL POST for used by token request
   private function curl_post($url, $auth, $data) {
      $ch = curl_init($url);
      curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false); // don't check certificate
      curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // don't check certificate
      curl_setopt($ch, CURLOPT_HEADER, 0);
      curl_setopt($ch, CURLOPT_USERPWD, $auth);
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
      curl_setopt($ch, CURLOPT_POST, 1);
      curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/x-www-form-urlencoded'));
      curl_setopt($ch, CURLOPT_POSTFIELDS, $data);

      $response = json_decode(curl_exec($ch));
      curl_close($ch);

      return $response;
   }


   // Handle callback (redirect_uri) based on url parameter.
   public function callback($argv1) {
      if (!empty($argv1['mcc_mnc'])) {
         $mcc_mnc = $argv1['mcc_mnc'];
      }
      if (!empty($argv1['code'])) {
         $oidc_code = $argv1['code'];
      }
      if (!empty($argv1['state'])) {
         $oidc_state = $argv1['state'];
      }
      if (!empty($argv1['subscriber_id'])) {
         $this->login_hint = $argv1['subscriber_id'];
      }

      if (!empty($mcc_mnc)) {
         $this->discoveryCallback($mcc_mnc);
      } else if (!empty($oidc_code) && !empty($oidc_state)) {
         $this->mobileconnectCallback($oidc_code, $oidc_state);
      } else {
         $this->complete_msg = "?state=-1&description=Something Went Wrong. [" . $argv1['error_description'] . "]";
      }
   }


   // handle Discovery Callback. extract mcc_mnc and perform a selected discovery request.
   private function discoveryCallback($argv1) {
      $mcc_mnc = explode("_", $argv1);

      $response = $this->PerformGetDiscovery("&Selected-MCC=". $mcc_mnc[0] . "&Selected-MNC=" . $mcc_mnc[1]);
      if ($this->parseDiscoveryResponse($response)) {
         if (empty($this->operatorSelection)) {
            $this->mobileconnectAuthenticate();
         } else {
            header("Location: ".$this->operatorSelection);
         }
      } else {
         $this->complete_msg = "?state=-1&description=Something Went Wrong. [" . $argv1['error_description'] . "]";
      }
   }


   // handle mobile connect callback.
   // happens on a successful mobile connect autorization request.
   private function mobileconnectCallback($argv1, $argv2) {
      // verify that state matches with the original request.
      if ($argv2 == $this->oidc_state) {
         // construct and send token request
         $post_data = "grant_type=authorization_code&code=" . $argv1 . "&redirect_uri=" . urlencode($this->redirect_uri);
         $response = $this->curl_post($this->oidc_token_ep, $this->oidc_client_id . ":" . $this->oidc_client_secret, $post_data);

         if (!empty($response->error)) {
            $this->complete_msg = "?state=-1&description=Something Went Wrong. [" . $response->error_description . "]";
         } else {
            if (!empty($response->id_token)) {
               $token = $this->decodeIdtoken($response->id_token);
               // check if nonce value matches with original request
               // nonce value is obtain from the id_token payload claims
               if ( $token['payload']->nonce == $this->oidc_nonce) {
                  $this->mobileconnectComplete($token, $response->access_token);
               } else {
                  $this->complete_msg = "?state=-1&description=Something Went Wrong. [nonce do not match]";
               }
            } else {
               $this->complete_msg = "?state=-1&description=Something Went Wrong. [id_token not found]";
            }
         }
      } else {
         $this->complete_msg = "?state=-1&description=Something Went Wrong. [state do not match]";
      }
   }

   // function called when everything has been verified.
   // contains the PCR to the user.
   private function mobileconnectComplete($argv1,$argv2) {
      $this->mc_state = true;
      $this->pcr = $argv1["payload"]->sub;
      $this->token_payload = json_encode($argv1["payload"]);
      $this->userinfo_payload = $this->get_userinfo($argv2);
      $this->complete_msg = "?state=1&description=Successful";
   }


   // parse the Discovery API response
   public function parseDiscoveryResponse($argv1) {
      if (!empty($argv1->links)) {
         foreach ($argv1->links as $k => $v) {
            if($v->rel == "operatorSelection") {
               $this->operatorSelection = $v->href;
               return true;
            }
         }
      }

      if (empty($argv1->ttl)) {
         return false;
      } else {
         $this->ttl = $argv1->ttl;
         if ($argv1->ttl <= time()) {
            //invalid ttl
            //TODO
         }
      }

      if (empty($argv1->response->client_id)) {
         return false;
      } else {
         $this->oidc_client_id = $argv1->response->client_id;
      }

      if (empty($argv1->response->client_secret)) {
         return false;
      } else {
         $this->oidc_client_secret = $argv1->response->client_secret;
      }

      foreach ($argv1->response->apis->operatorid->link as $k => $v) {
         if($v->rel == "authorization") {
            $auth_url = $v->href;
         } else if ($v->rel == "token") {
            $token_url = $v->href;
         } else if ($v->rel == "userinfo") {
            $userinfo_url = $v->href;
         }
      }

      if (empty($auth_url)) {
         return false;
      } else {
         $this->oidc_auth_ep = $auth_url;
      }

      if (empty($token_url)) {
         return false;
      } else {
         $this->oidc_token_ep = $token_url;
      }

      if (empty($userinfo_url)) {
         //optional endpoint if available
      } else {
         $this->oidc_userinfo_ep = $userinfo_url;
      }

      if (empty($argv1->subscriber_id)) {
         //optional if we have subscriber_id
      } else {
         $this->login_hint = $argv1->subscriber_id;
      }

      return true;
   }


   // decode id_token from base64url encoded form.
   private function decodeIdtoken($argv1) {
      $result = Array();

      $parts = explode(".", $argv1);

      if (!empty($parts[0])) {
         $result['header'] = json_decode(base64_decode(str_replace(array('-', '_'), array('+', '/'), $parts[0])));
      }      

      if (!empty($parts[1])) {
         $result['payload'] = json_decode(base64_decode(str_replace(array('-', '_'), array('+', '/'), $parts[1])));
      } 

      if (!empty($parts[2])) {
         $result['signature'] = $parts[2];
      }

      return $result;
   }

   private function get_userinfo($token) {
      $res = "";
      $ch = curl_init($this->oidc_userinfo_ep);
      //curl_setopt($ch, CURLOPT_FILE, $fp);
      curl_setopt($ch, CURLOPT_HEADER, 0);
      curl_setopt($ch, CURLOPT_HTTPHEADER, array('Authorization: Bearer ' . $token));
      curl_setopt($ch, CURLOPT_FOLLOWLOCATION, false);
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

      $res = curl_exec($ch);

      curl_close($ch);

      return $res;
   }

   // function used by callback.php to handle close of popup
   public function check_complete_msg() {
     if (empty($this->complete_msg)) {
        return false;
     } else {
        return true;
     }
   }


   // function used by callback.php to handle close of popup
   public function get_complete_msg() {
      return $this->complete_msg;
   }

   // function used by callback.php to handle close of popup
   public function get_complete_state() {
      return $this->mc_state;
   }

   // function used by callback.php to handle close of popup
   public function get_pcr() {
      return $this->pcr;
   }

   // function used by callback.php to handle close of popup
   public function get_token_payload() {
      return $this->token_payload;
   }

   // function used by callback.php to handle close of popup
   public function get_userinfo_payload() {
      return $this->userinfo_payload;
   }

   public function hashed_msisdn_match() {
      $userinfo = json_decode($this->userinfo_payload);
      if (isset($userinfo->phone_number) && ($userinfo->phone_number == hash("sha256", $this->mnv_msisdn))) {
         return true;
      }
      return false;
   }

   // future used, for extra serverside logging
   private function logger($argv1 = "", $argv2 = "") {
      $fp = fopen($this->logfile, 'a+');
      fwrite($fp, date("Y-m-d H:i:s")."|". $argv1 ."|". $argv2);
      fclose($fp);
   }    
} 
?>
