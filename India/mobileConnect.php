<?php
class mobileConnect {
   // discovery used parameter
   private $auth_ep;
   private $token_ep;
   private $userinfo_ep;
   private $client_id;
   private $client_secret;
   private $redirect_uri;

   private $state;
   private $nonce;
   // mobileconnect used parameter
   private $scope = "openid+phone";
   private $acr_values = "2";
   private $max_age;
   private $prompt = "login";


   private $pcr;
   private $token_payload;
   private $complete_msg = 'n/a';
   private $userinfo_payload = "{}";
   private $mc_state = false;
   private $logfile = "./log.txt";
   
   // Initiate Class
   // store information into class
   function mobileConnect($argv1) {
      foreach($argv1 as $k => $v) {
         if ($k == "auth_url") {
            $this->auth_ep = $v;
         } else if ($k == "token_url") {
            $this->token_ep = $v;
         } else if ($k == "userinfo_url") {
            $this->userinfo_ep = $v;
         } else if ($k == "client_id") {
            $this->client_id = $v;
         } else if ($k == "client_secret") {
            $this->client_secret = $v;
         } else if ($k == "redirect_uri") {
            $this->redirect_uri = $v;
         }

      }
      $this->state = "state".substr(base64_encode(md5( mt_rand() )), 0, 13);
      $this->nonce = "nonce".substr(base64_encode(md5( mt_rand() )), 0, 13);
   } 

   // function to construct the Mobile Connect Authentication URL and redirect to it,
   public function mobileconnectAuthenticate() {

      $url = $this->auth_ep;
      $url .= "?client_id=" . $this->client_id;
      $url .= "&scope=" . $this->scope;
      $url .= "&redirect_uri=" . urlencode($this->redirect_uri);
      $url .= "&response_type=code";
      $url .= "&state=" . $this->state;
      $url .= "&nonce=" . $this->nonce;
      $url .= "&acr_values=" . $this->acr_values;
      if (!empty($this->max_age)) {
         $url .= "&max_age=" . $this->max_age;
      }
      if (!empty($this->prompt)) {
         $url .= "&prompt=" . $this->prompt;
      }

      header("Location: ".$url);
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
      if (!empty($argv1['code'])) {
         $oidc_code = $argv1['code'];
      }
      if (!empty($argv1['state'])) {
         $oidc_state = $argv1['state'];
      }

      if (!empty($oidc_code) && !empty($oidc_state)) {
         $this->mobileconnectCallback($oidc_code, $oidc_state);
      } else {
         $this->complete_msg = "?state=-1&description=Something Went Wrong. [" . $argv1['error_description'] . "]";
      }
   }

   // handle mobile connect callback.
   // happens on a successful mobile connect autorization request.
   private function mobileconnectCallback($argv1, $argv2) {
      // verify that state matches with the original request.
      if ($argv2 == $this->state) {
         // construct and send token request
         $post_data = "grant_type=authorization_code&code=" . $argv1 . "&redirect_uri=" . urlencode($this->redirect_uri);
         $response = $this->curl_post($this->token_ep, $this->client_id . ":" . $this->client_secret, $post_data);

         if (!empty($response->error)) {
            $this->complete_msg = "?state=-1&description=Something Went Wrong. [" . $response->error_description . "]";
         } else {
            if (!empty($response->id_token)) {
               $token = $this->decodeIdtoken($response->id_token);


               // check if nonce value matches with original request
               // nonce value is obtain from the id_token payload claims
               if ( $token['payload']->nonce == $this->nonce) {
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
   private function mobileconnectComplete($argv1, $argv2) {
      $this->mc_state = true;
      $this->pcr = $argv1["payload"]->sub;
      $this->token_payload = json_encode($argv1["payload"]);
      $this->userinfo_payload = $this->get_userinfo($argv2);
      $this->complete_msg = "?state=1&description=Successful";
   }

   private function get_userinfo($token) {
      $res = "";
      $ch = curl_init($this->userinfo_ep);
      //curl_setopt($ch, CURLOPT_FILE, $fp);
      curl_setopt($ch, CURLOPT_HEADER, 0);
      curl_setopt($ch, CURLOPT_HTTPHEADER, array('Authorization: Bearer ' . $token));
      curl_setopt($ch, CURLOPT_FOLLOWLOCATION, false);
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

      $res = curl_exec($ch);

      curl_close($ch);

      return $res;
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

   // future used, for extra serverside logging
   private function logger($argv1 = "", $argv2 = "") {
      $fp = fopen($this->logfile, 'a+');
      fwrite($fp, date("Y-m-d H:i:s")."|". $argv1 ."|". $argv2);
      fclose($fp);
   }    
} 
?>
