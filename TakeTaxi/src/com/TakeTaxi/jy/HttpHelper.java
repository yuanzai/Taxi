package com.TakeTaxi.jy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;

public class HttpHelper {

	
	public static String request(HttpResponse response) {
		String result = "";
		try {
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder str = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				str.append(line + "\n");
			}
			in.close();
			result = str.toString();
		} catch (Exception ex) {
			result = "Error";
		}
		return result;
	}

	
	public static final String domain = "http://hopcabtest.herokuapp.com/drivers/get_driver_positions";

	// public static final String domain = "http://junyuan.phpfogapp.com/";

	 //public static final String domain = "http://192.168.1.17/taxi/";

}