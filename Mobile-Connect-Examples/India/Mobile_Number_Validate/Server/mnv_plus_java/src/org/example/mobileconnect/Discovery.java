package org.example.mobileconnect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.*;

import org.example.mobileconnect.Config;
import org.example.mobileconnect.REST;

public class Discovery {
	private Config configs;
	private String oidc_client_id;
	private String oidc_client_secret;
	private String oidc_auth_ep;
	private String oidc_token_ep;
	private String oidc_userinfo_ep;
	private String serving_operator;
	private String subscriber_id;
	private String operatorSelection;
	private String response;
	
	public Discovery (Config input) {
		this.configs = input;
	}
	
	public boolean discover_post(String msisdn) {
		//request argv1 = url argv2 = <client_id>:<client_secret> argv3 = post
		String post_data = "MSISDN=" + msisdn + "&Redirect_URL=" + this.configs.get_redirect_uri();
		String endpoint = this.configs.get_discovery_ep();
		
		this.response = REST.postUrl(endpoint, this.configs.get_client_id()+":"+this.configs.get_client_secret(), post_data);

		parseDiscoveryResponse(this.response);
		return true;
	}
	
	public boolean PerformIPDiscovery(String ip) {
		Map<String, String> headers = new HashMap<String, String>();
		
		headers.put("Accept", "application/json");
		headers.put("X-Source-IP", ip);
		
		this.response = REST.getUrlwithHeaders(this.configs.get_discovery_ep() + "?Redirect_URL=" + this.configs.get_redirect_uri(), this.configs.get_client_id()+":"+this.configs.get_client_secret(), headers);

		if (parseDiscoveryResponse(this.response)) {
			return true;
		} else {
			return false;
		}
	}
	
		
	public boolean parseDiscoveryResponse(String data) {
		Gson argv1 = new Gson();
		DiscoveryResponse dr = argv1.fromJson(data, DiscoveryResponse.class);
		
		//Handle Operator Selection URL
		if (dr.links != null) {
			for (int i = 0; i < dr.links.size(); i++) {
				switch(dr.links.get(i).rel) {
				case "operatorSelection":
					this.operatorSelection = dr.links.get(i).href;
					break;
				}
			}
			return true;
		//Handle Standard Discovery Response.
		} else if (dr.response != null) {
			if (dr.response.client_id == null) {
				log("E|parseDiscoveryResponse", "client_id missing");
				return false;
			} else {
				this.oidc_client_id = dr.response.client_id;
			}
			if (dr.response.client_secret == null) {
				log("E|parseDiscoveryResponse", "client_secret missing");
				return false;
			} else {
				this.oidc_client_secret = dr.response.client_secret;
			}
			if (dr.response.serving_operator == null) {
				log("W|parseDiscoveryResponse", "serving_operator missing");
			} else {
				this.serving_operator = dr.response.serving_operator;
			}	
			
			for (int i = 0; i < dr.response.apis.operatorid.link.size(); i++) {
				switch(dr.response.apis.operatorid.link.get(i).rel) {
				case "authorization":
					this.oidc_auth_ep = dr.response.apis.operatorid.link.get(i).href;
					break;
				case "token":
					this.oidc_token_ep = dr.response.apis.operatorid.link.get(i).href;
					break;
				case "userinfo":
					this.oidc_userinfo_ep = dr.response.apis.operatorid.link.get(i).href;
					break;	
				}
			}
			if (this.oidc_auth_ep == null) {
				log("E|parseDiscoveryResponse", "authorization ep missing");
				return false;
			} else if (this.oidc_token_ep == null) {
				log("E|parseDiscoveryResponse", "token ep missing");
				return false;
			} else if (this.oidc_userinfo_ep == null) {
				log("W|parseDiscoveryResponse", "userinfo ep missing");
			}
		
			if (dr.subscriber_id != null) {
				this.subscriber_id = dr.subscriber_id;
			}
			return true;
		}
		//Something bad Happened.
		return false;
	}
	
	public String get_oidc_client_id() {
		return this.oidc_client_id;
	}

	public String get_oidc_client_secret() {
		return this.oidc_client_secret;
	}
	
	public String get_oidc_auth_ep() {
		return this.oidc_auth_ep;
	}
	
	public String get_oidc_token_ep() {
		return this.oidc_token_ep;
	}

	public String get_oidc_userinfo_ep() {
		return this.oidc_userinfo_ep;
	}
	
	public String get_serving_operator() {
		return this.serving_operator;
	}
	
	public String get_subscriber_id() {
		return this.subscriber_id;
	}
	
	public String get_response() {
		return this.response;
	}
	
	public String get_operatorSelection() {
		return this.operatorSelection;
	}
	
	public DiscoveryResponse get_disc_response() {
		Gson argv1 = new Gson();
		return (DiscoveryResponse) argv1.fromJson(this.response, DiscoveryResponse.class);
	}	
	
	public Config get_config() {
		return this.configs;
	}
	
	private void log(String argv1, String argv2) {
		System.out.println("[Discovery]" + argv1 + ":" + argv2);
	}
	
	
	
	
	
	//Defined Object for Discovey Response used by GSON
	public class DiscoveryResponse {
	    //private int ttl;
	    private disc_response response;
	    private String subscriber_id;
	    private List<disc_link> links;
	}
	
	public class disc_response {
		private String client_id;
		private String client_secret;
		private String serving_operator;
		private String country;
		private String currency;
		private disc_apis apis;
	}
	
	public class disc_apis {
		private disc_operatorid operatorid;
	}
	
	public class disc_operatorid {
		private List<disc_link> link;
	}
	
	public class disc_link {
		private String href;
		private String rel;
	}
	
}