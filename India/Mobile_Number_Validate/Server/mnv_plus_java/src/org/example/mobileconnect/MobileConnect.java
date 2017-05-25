package org.example.mobileconnect;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.example.mobileconnect.REST;
import org.example.mobileconnect.Discovery;

import com.google.gson.*;

import javax.servlet.http.HttpServletRequest;

public class MobileConnect {
	private Discovery disc;
	private String state = "";
	private String nonce = "";
	private String c_state = "-1";
	private boolean mc_state = false;
	private String complete_msg = "";
	private IDTokenPayload token_payload;
	private UserinfoResponse userinfo_payload;
	private String mnv_msisdn;

	public MobileConnect(Discovery argv1) {
		this.disc = argv1;
		this.state = Long.toHexString(Double.doubleToLongBits(Math.random()));
		this.nonce = Long.toHexString(Double.doubleToLongBits(Math.random()));
	}

	public String mobileconnectAuthenticate() {
		return this.mobileconnectAuthenticate("");
	}

	public String mobileconnectAuthenticate(String argv1) {
		String auth_url = this.disc.get_oidc_auth_ep() + "?client_id="
				+ this.disc.get_oidc_client_id() + "&scope="
				+ this.disc.get_config().get_scope() + "&redirect_uri="
				+ this.disc.get_config().get_redirect_uri()
				+ "&response_type=code" + "&state=" + this.state + "&nonce="
				+ this.nonce + "&acr_values="
				+ this.disc.get_config().get_acr_values();

		if (this.disc.get_subscriber_id() != null) {
			auth_url += "&login_hint=ENCR_MSISDN%3A"
					+ this.disc.get_subscriber_id();
		}
		if (!argv1.isEmpty()) {
			this.mnv_msisdn = argv1;
			auth_url += "&login_hint=MSISDN%3A" + this.mnv_msisdn;
		}

		return auth_url;
	}

	public void process_callback(HttpServletRequest argv1) {
		String code = argv1.getParameter("code");
		String state = argv1.getParameter("state");
		String error = argv1.getParameter("error");
		String error_description = argv1.getParameter("error_description");

		if (code != null && state != null) {
			this.token_request(code, state);
		} else {
			log("E|callback", "parameter received.|" + code + "|" + state + "|"
					+ error + "|" + error_description + "|");
			this.c_state = "-10";
			this.complete_msg = "Something Went Wrong. [" + error_description
					+ "]";
		}

	}

	private void token_request(String argv1, String argv2) {
		/*
		 * Check if state value is the same from request.
		 */
		if (argv2.contentEquals(this.state)) {
			String post_data = "grant_type=authorization_code&code=" + argv1
					+ "&redirect_uri="
					+ this.disc.get_config().get_redirect_uri();

			String result = REST.postUrl(
					this.disc.get_oidc_token_ep(),
					this.disc.get_oidc_client_id() + ":"
							+ this.disc.get_oidc_client_secret(), post_data);

			// JSONObject resultObj = String2Json(result);
			Gson parser = new Gson();
			TokenResponse tr = parser.fromJson(result, TokenResponse.class);

			if (tr.id_token != null) {
				/*
				 * extract id_token and claims Check if nonce value is the same
				 * from request.
				 */
				IDToken id_token = decodeIdToken(tr.id_token);

				String nonce = id_token.payload.nonce;
				if (nonce.contentEquals(this.nonce)) {
					this.mobileconnectComplete(id_token, tr.access_token);
				} else {
					log("E|token_request",
							"Nonce returned do not match original>" + nonce
									+ "|" + this.nonce);
					this.c_state = "-11";
					this.complete_msg = "nonce mismatch.";
				}
			} else {
				log("E|token_request", "Token Request failed>" + tr.toString()
						+ "|");
				this.c_state = "-5";
				this.complete_msg = "failed";

			}

		} else {
			log("E|token_request", "State returned do not match original>"
					+ argv2 + "|" + this.state);
			this.c_state = "-12";
			this.complete_msg = "State mismatch.";
		}
		this.state = "";
		this.nonce = "";

		if (!this.c_state.contentEquals("1")) {
			this.token_payload = null;
			this.userinfo_payload = null;
		}
	}

	private void mobileconnectComplete(IDToken argv1, String argv2) {
		this.mc_state = true;
		// $this->mc_state = true;
		// $this->pcr = $argv1["payload"]->sub;
		// $this->token_payload = json_encode($argv1["payload"]);
		// $this->userinfo_payload = $this->get_userinfo($argv2);
		// $this->complete_msg = "?state=1&description=Successful";
		System.out.println("complete");
		this.token_payload = argv1.payload;
		this.userinfo_payload = this.get_userinfo(argv2);
		this.c_state = "1";
		this.complete_msg = "complete.";

	}

	private UserinfoResponse get_userinfo(String argv1) {
		if (this.disc.get_oidc_userinfo_ep() == null) {
			return null;
		}
		Map<String, String> headers = new HashMap<String, String>();

		headers.put("Accept", "application/json");
		headers.put("Authorization", "Bearer " + argv1);

		String response = REST.getUrlwithHeaders(
				this.disc.get_oidc_userinfo_ep(), "", headers);
		Gson parser = new Gson();

		return parser.fromJson(response, UserinfoResponse.class);
	}

	public IDToken decodeIdToken(String argv1) {
		String[] base64Encodedid_token = argv1.toString().split("\\.");
		Gson parser = new Gson();
		IDToken result = new IDToken();

		for (int i = 0; i < base64Encodedid_token.length; i++) {
			if (i == 0) {
				result.head = parser.fromJson(StringUtils.newStringUtf8(Base64
						.decodeBase64(base64Encodedid_token[i])),
						IDTokenHead.class);
			} else if (i == 1) {
				result.payload = parser.fromJson(StringUtils
						.newStringUtf8(Base64
								.decodeBase64(base64Encodedid_token[i])),
						IDTokenPayload.class);
			} else if (i == 2) {
				result.signature = base64Encodedid_token[2];
			}
		}
		if (base64Encodedid_token.length < 3) {
			log("E|decodeIdToken|", "Warning, id_token not complete. " + argv1);
		}
		return result;
	}

	public Boolean check_complete_msg() {
		if (this.complete_msg.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public String get_complete_msg() {
		return "?state=" + this.c_state + "&description=" + this.complete_msg;
	}

	public Boolean get_complete_state() {
		return this.mc_state;
	}

	public String get_complete_c_state() {
		return this.c_state;
	}

	public String get_pcr() {
		return this.token_payload.sub;
	}

	public IDTokenPayload get_token_payload() {
		return this.token_payload;
	}
	
	public String get_token_payload_string() {
		Gson gson = new Gson();
		return gson.toJson(this.token_payload);
	}

	public UserinfoResponse get_userinfo_payload() {
		return this.userinfo_payload;
	}

	public String get_userinfo_payload_string() {
		Gson gson = new Gson();
		return gson.toJson(this.userinfo_payload);
	}
	
	public Boolean hashed_msisdn_match() {
		if (this.userinfo_payload.phone_number != null
				&& this.userinfo_payload.phone_number.contentEquals(this
						.sha_hash(this.mnv_msisdn))) {
			return true;
		}
		return false;
	}

	private String sha_hash(String data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(data.getBytes());
			byte byteData[] = md.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			log("E|sha_hash|", "Warning, can't hash |" + data + "|");
			return "";
		}

	}

	private void log(String argv1, String argv2) {
		System.out.println("[Mobileconnect]" + argv1 + ":" + argv2);
	}

	// Defined Object for used by GSON
	public class TokenResponse {
		private String access_token;
		private String token_type;
		private String refresh_token;
		private int expires_in;
		private String id_token;
		private String error;
		private String error_description;
	}

	public class IDToken {
		private IDTokenHead head;
		private IDTokenPayload payload;
		private String signature;
	}

	public class IDTokenHead {
		private String alg;
	}

	public class IDTokenPayload {
		private String iss;
		private String sub;
		private List<String> aud;
		private int exp;
		private int iat;
		private String nonce;
		private String at_hash;
		private String auth_time;
		private String acr;
		private List<String> amr;
		private String azp;
	}

	public class UserinfoResponse {
		private String sub;
		private String phone_number;
	}
}
