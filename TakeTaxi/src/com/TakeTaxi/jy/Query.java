package com.TakeTaxi.jy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class Query extends Activity {

	public static String getETA(String url, String fromLat, String fromLongi, String toLat, String toLongi, String type) {

		HttpPost postJob = new HttpPost(HttpHelper.domain + "getduration.php");
		
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 4900;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient clientJob = new DefaultHttpClient(httpParameters);
		
		try {
			List<NameValuePair> infoJob = new ArrayList<NameValuePair>(2);
			infoJob.add(new BasicNameValuePair("url", url));

			infoJob.add(new BasicNameValuePair("fromLongi", fromLongi));
			infoJob.add(new BasicNameValuePair("fromLat", fromLat));
			infoJob.add(new BasicNameValuePair("toLongi", toLongi));
			infoJob.add(new BasicNameValuePair("toLat", toLat));
			infoJob.add(new BasicNameValuePair("type", type));

			postJob.setEntity(new UrlEncodedFormEntity(infoJob));

			HttpResponse response = clientJob.execute(postJob);

			String jsonString = HttpHelper.request(response);
			String duration = jsonString.trim();

			return duration;
			
		} catch (Exception ex) {
			return null;
		}
		
		
	}
	
	public static int getServerTime() {
		int gettime = 0;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(HttpHelper.domain + "time.php");
		try {
			HttpResponse response = client.execute(request);
			gettime = Integer.parseInt(HttpHelper.request(response).replaceAll(
					"[^0-9.]", ""));

		} catch (IOException e) {
		}
		return gettime;

	}

	public static String clientSubmitJob(String driver_id, String name,
			String number, String lat, String longi, String pickup,
			String destination, String pax_id) {


		HttpPost postJob = new HttpPost(HttpHelper.domain + "postjob.php");
		
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 4900;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient clientJob = new DefaultHttpClient(httpParameters);
		
		
		

		try {
			List<NameValuePair> infoJob = new ArrayList<NameValuePair>(2);
			infoJob.add(new BasicNameValuePair("driver_id", driver_id));
			infoJob.add(new BasicNameValuePair("name", name));

			infoJob.add(new BasicNameValuePair("number", number));

			infoJob.add(new BasicNameValuePair("lat", lat));
			infoJob.add(new BasicNameValuePair("longi", longi));
			infoJob.add(new BasicNameValuePair("pickup", pickup));
			infoJob.add(new BasicNameValuePair("destination", destination));
			infoJob.add(new BasicNameValuePair("pax_id", pax_id));

			infoJob.add(new BasicNameValuePair("open", "1"));

			postJob.setEntity(new UrlEncodedFormEntity(infoJob));

			HttpResponse response = clientJob.execute(postJob);
			String job_id = HttpHelper.request(response);
			return job_id;
		} catch (ClientProtocolException e) {
			
			
			return null;
		} catch (IOException e) {
			return null;
		}

	}

	public static JSONObject getNearestDriver(int lat, int longi) {
		HttpClient clientJob = new DefaultHttpClient();
		String urlJob = HttpHelper.domain + "nearest.php";
		HttpPost postJob = new HttpPost(urlJob);
		try {
			List<NameValuePair> infoJob = new ArrayList<NameValuePair>(2);
			infoJob.add(new BasicNameValuePair("lat", Integer.toString(lat)));
			infoJob.add(new BasicNameValuePair("longi", Integer.toString(longi)));
			postJob.setEntity(new UrlEncodedFormEntity(infoJob));
			HttpResponse response = clientJob.execute(postJob);
			String jsonString = HttpHelper.request(response);
			JSONObject json = new JSONObject(jsonString);
			return json;
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (JSONException e) {
			return null;
		}
	}

	public static int[][] getDriverPosition(String driver_id) {
		
		int driverCount = 0;
		

		//HttpPost postJob = new HttpPost(HttpHelper.domain + "drivers.php");
		HttpPost postJob = new HttpPost(HttpHelper.domain + ".json");
		
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 4900;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient clientJob = new DefaultHttpClient(httpParameters);
		
		
		
		JSONArray jArray = null;
		try {
			List<NameValuePair> infoJob = new ArrayList<NameValuePair>(2);
			infoJob.add(new BasicNameValuePair("driver_id", driver_id));
			postJob.setEntity(new UrlEncodedFormEntity(infoJob));
			HttpResponse response = clientJob.execute(postJob);
			
			String jsonString = HttpHelper.request(response);
			jArray = new JSONArray(jsonString);
			
		} catch (Exception ex) {
		}
		
		if (jArray != null){
		driverCount = jArray.length();
		
		
		int[][] driverPositionArray = new int[driverCount][3];
		try {
			for (int i = 0; jArray.getJSONObject(i) != null; i++) {
				JSONObject json = jArray.getJSONObject(i);
				driverPositionArray[i][0] = json.getInt("driver_id");
				
				int templat = (int) (json.getDouble("latitude") * 1000000);
				int templong = (int) (json.getDouble("longitude") * 1000000);

				driverPositionArray[i][1] = templat;
				driverPositionArray[i][2] = templong;
				
				//driverPositionArray[i][1] = json.getInt("lat");
				//driverPositionArray[i][2] = json.getInt("longi");
			}
		} catch (JSONException e) {
						
		}
		return driverPositionArray;
		} else {
			return null;
		}
		
		

	}

	public static String getDriverDetail(String s, String driver_id) {
		// load positions by http mySQL method

		HttpPost postJob = new HttpPost(HttpHelper.domain + "drivers.php");
		
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 4900;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient clientJob = new DefaultHttpClient(httpParameters);
		
		try {
			List<NameValuePair> infoJob = new ArrayList<NameValuePair>(2);
			infoJob.add(new BasicNameValuePair("driver_id", driver_id));
			postJob.setEntity(new UrlEncodedFormEntity(infoJob));
			HttpResponse response = clientJob.execute(postJob);

			String jsonString = HttpHelper.request(response);
			JSONArray jArray = new JSONArray(jsonString);
			
			JSONObject json = jArray.getJSONObject(0);
			String result = json.getString(s);
			return result;

		} catch (Exception ex) {
			return null;
		}

	}

	public static String jobQuery(String msgtype, String job_id, int rating,
			String driver_id) {
	
		HttpPost postJob = new HttpPost(HttpHelper.domain + "jobquery.php");
		

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 4900;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient clientJob = new DefaultHttpClient(httpParameters);
		
		try {
			List<NameValuePair> infoJob = new ArrayList<NameValuePair>(2);
			infoJob.add(new BasicNameValuePair("msgtype", msgtype));
			infoJob.add(new BasicNameValuePair("job_id", job_id));
			infoJob.add(new BasicNameValuePair("rating", Integer
					.toString(rating)));
			infoJob.add(new BasicNameValuePair("driver_id", driver_id));
			postJob.setEntity(new UrlEncodedFormEntity(infoJob));
			HttpResponse response = clientJob.execute(postJob);

			String result = "done";
			
				return result;
				
			
		} catch (Exception e) {
			
			return null;
		}
		
	}
	
	
	public static JSONObject getJobInfo(String job_id) {

		HttpPost postJob = new HttpPost(HttpHelper.domain + "job.php");
		
		
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 4900;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient clientJob = new DefaultHttpClient(httpParameters);

		try {

			List<NameValuePair> infoJob = new ArrayList<NameValuePair>(2);
			infoJob.add(new BasicNameValuePair("job_id", job_id));

			postJob.setEntity(new UrlEncodedFormEntity(infoJob));

			HttpResponse response = clientJob.execute(postJob);
			String jsonString = HttpHelper.request(response);
			JSONArray jArray = new JSONArray(jsonString);
			JSONObject json = jArray.getJSONObject(0);

			return json;
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (JSONException e) {
			return null;
		}
	}

	
	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
	
	
	/*
	public static JSONObject onrouteDriverPosition(int driver_id) {
		HttpClient clientJob = new DefaultHttpClient();
		String url = HttpHelper.domain + "drivers.php";
		HttpPost postJob = new HttpPost(url);
		try {
			List<NameValuePair> infoJob = new ArrayList<NameValuePair>(2);
			infoJob.add(new BasicNameValuePair("driver_id", Integer
					.toString(driver_id)));
			postJob.setEntity(new UrlEncodedFormEntity(infoJob));
			HttpResponse response = clientJob.execute(postJob);

			String jsonString = HttpHelper.request(response);
			JSONArray jArray = new JSONArray(jsonString);
			JSONObject json = jArray.getJSONObject(0);

			return json;

		} catch (Exception e) {
			return null;

		}

	}
*/
}
