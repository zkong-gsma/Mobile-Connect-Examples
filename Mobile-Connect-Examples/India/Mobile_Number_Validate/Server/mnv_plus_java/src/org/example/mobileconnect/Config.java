package org.example.mobileconnect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

public class Config {
	private String client_id;
	private String client_secret;
	private String redirect_uri;
	private String discovery_ep;
	private String scope;
	private String acr_values;

	public Config(HttpServletRequest req) {
		Properties prop = new Properties();
		InputStream input = null;
		
		try {

			input = new FileInputStream(req.getServletContext().getRealPath("/WEB-INF") + File.separator + "config.properties");
			prop.load(input);
			
			if (prop.getProperty("client_id") == null) {
				System.out.println("Error, Mandatory client_id in config.properties missing");
				System.exit(0);
			} else {
				this.client_id = prop.getProperty("client_id");
			}
			if (prop.getProperty("client_secret") == null) {
				System.out.println("Error, Mandatory client_secret in config.properties missing");
				System.exit(0);
			} else {
				this.client_secret = prop.getProperty("client_secret");
			}
			if (prop.getProperty("redirect_uri") == null) {
				System.out.println("Error, Mandatory redirect_uri in config.properties missing");
				System.exit(0);
			} else {
				this.redirect_uri = prop.getProperty("redirect_uri");
			}
			if (prop.getProperty("discovery_ep") == null) {
				System.out.println("Error, Mandatory discovery_ep in config.properties missing");
				System.exit(0);
			} else {
				this.discovery_ep = prop.getProperty("discovery_ep");
			}
			if (prop.getProperty("scope") == null) {
				System.out.println("Warning, optional scope missing in config.properties default to openid");
				this.scope = "openid";
			} else {
				this.scope = prop.getProperty("scope");
			}
			if (prop.getProperty("acr_values") == null) {
				System.out.println("Warning, optional acr_values missing in config.properties default to 2");
				this.acr_values = "2";
			} else {
				this.acr_values = prop.getProperty("acr_values");
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void set_client_id(String input) {
		this.client_id = input;
	}
	
	public String get_client_id() {
		return this.client_id;
	}
	
	public void set_client_secret(String input) {
		this.client_secret = input;
	}
	
	public String get_client_secret() {
		return this.client_secret;
	}
	
	public void set_redirect_uri(String input) {
		this.redirect_uri = input;
	}
	
	public String get_redirect_uri() {
		try {
			return URLEncoder.encode(this.redirect_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	public String get_scope() {
		if (this.scope == null) {
			return "openid";
		}
		return this.scope;
	}
	
	public void set_scope(String input) {
		this.scope = input;
	}
	
	public String get_acr_values() {
		if (this.acr_values == null) {
			return "2";
		}
		return this.acr_values;
	}
	
	public void set_acr_values(String input) {
		this.acr_values = input;
	}
	
	public String get_discovery_ep() {
		return this.discovery_ep;
	}
	
	public void set_discovery_ep(String input) {
		this.discovery_ep = input;
	}
	
}