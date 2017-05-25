package org.example.mobileconnect;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.commons.codec.binary.Base64;

public class REST {
	/*
	 * POST request argv1 = url argv2 = <client_id>:<client_secret> argv3 = post
	 * parameters
	 */
	public static String postUrl(String argv1, String argv2, String argv3) {

		String result = "{}";
		try {
			String authStringEnc = new String(Base64.encodeBase64(argv2
					.getBytes()));

			URL url = new URL(argv1);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream());

			writer.write(argv3);
			writer.flush();
			writer.close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String recv = "";
			String recvbuff = "";
			while ((recv = reader.readLine()) != null)
				recvbuff += recv;
			reader.close();
			result = recvbuff;

		} catch (FileNotFoundException e) {
			log("E|postUrl", "404 not found");
		} catch (Exception e) {
			log("E|postUrl",
					argv1 + "|" + argv2 + "|" + argv3 + "|" + e.toString());
		}

		return result;
	}

	public static String getUrlwithHeaders(String argv1, String argv2,
			Map<String, String> argv3) {

		String result = "{}";
		try {
			String authStringEnc = "";
			if (!argv2.isEmpty()) {
				authStringEnc = new String(
						Base64.encodeBase64(argv2.getBytes()));
			}

			URL url = new URL(argv1);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			if (!authStringEnc.isEmpty()) {
				conn.setRequestProperty("Authorization", "Basic "
						+ authStringEnc);
			}
			Iterator<Map.Entry<String, String>> iter = ((Map<String, String>) argv3)
					.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				// System.out.println(entry.getKey() + "|" +
				// entry.setValue(entry.getKey()));
				conn.setRequestProperty(entry.getKey(),
						entry.setValue(entry.getKey()));
			}
			BufferedReader buffread = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));

			String recv = "";
			result = "";
			while ((recv = buffread.readLine()) != null)
				result += recv;
			buffread.close();

		} catch (FileNotFoundException e) {
			log("E|getUrlwithHeaders", "404 not found");
		} catch (Exception e) {
			log("E|getUrlwithHeaders", argv1 + "|" + argv2 + "|" + argv3 + "|"
					+ e.toString());
		}

		return result;
	}

	private static void log(String argv1, String argv2) {
		System.out.println("[REST]" + argv1 + ":" + argv2);
	}

}
