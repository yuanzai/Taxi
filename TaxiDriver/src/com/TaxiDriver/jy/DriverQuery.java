package com.TaxiDriver.jy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DriverQuery {

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

	public static String jobQuery(String msgtype, String job_id, int rating, String driver_id) {

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
			infoJob.add(new BasicNameValuePair("rating", Integer.toString(rating)));
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

	public static String[][] pingForJobAndPosition(String driver_id, int lat, int longi, ArrayList<Object> rejectList, String av) {
		int jobCount = 0;
		int j = 0;

		HttpPost postJob = new HttpPost(HttpHelper.domain + "pingjob.php");

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
			infoJob.add(new BasicNameValuePair("lat", Integer.valueOf(lat).toString()));
			infoJob.add(new BasicNameValuePair("longi", Integer.valueOf(longi).toString()));
			infoJob.add(new BasicNameValuePair("avail", av));
			postJob.setEntity(new UrlEncodedFormEntity(infoJob));

			HttpResponse response = clientJob.execute(postJob);
			String jsonString = HttpHelper.request(response);
			jArray = new JSONArray(jsonString);
			if (jArray != null) {
				jobCount = jArray.length();
			}
		} catch (Exception ex) {

		}

		
		if (jArray != null && av.equals("1")) {
			String[][] joblist = new String[jobCount][9];

			String geoadd = null;

			try {
				for (int i = 0; i < jobCount; i++) {
					JSONObject json = jArray.getJSONObject(i);

					/*
					 * Geocoder geocoder = new Geocoder(c, Locale.getDefault()); try { List<Address> address = geocoder.getFromLocation( json.getInt("lat") /
					 * 1E6, json.getInt("longi") / 1E6, 1); if (address.size() > 0) { geoadd = ""; for (int j = 0; j < address.get(0) .getMaxAddressLineIndex();
					 * j++) { geoadd += address.get(0).getAddressLine(j) + "\n"; } } } catch (IOException e) { } finally { }
					 */

					if (rejectList != null && rejectList.contains(json.getString("job_id"))) {
					} else {
						joblist[j][0] = json.getString("job_id");
						joblist[j][1] = json.getString("lat");
						joblist[j][2] = json.getString("longi");

						joblist[j][3] = "";
						joblist[j][4] = json.getString("pickup");
						joblist[j][5] = json.getString("destination");
						joblist[j][6] = String.valueOf(getServerTime() - json.getInt("datetime"));
						joblist[j][7] = String.valueOf(json.getInt("number"));
						joblist[j][8] = json.getString("driver_id");
						j++;
					}
				}

			} catch (JSONException e) {

			}

			if (j == 0) {

				return null;
			} else {
				return joblist;
			}
		} else {

			return null;
		}

	}

	public static int getServerTime() {
		int gettime = 0;

		HttpClient client2 = new DefaultHttpClient();
		HttpGet request2 = new HttpGet(HttpHelper.domain + "time.php");
		try {
			HttpResponse response2 = client2.execute(request2);
			gettime = Integer.parseInt(HttpHelper.request(response2).replaceAll("[^0-9.]", ""));

		} catch (IOException e) { // TODO Auto-generated catch block

		}

		return gettime;
	}

	public static void ping(String driver_id, int dLat, int dLongi, String av) {
		// load positions by http mySQL method

		//HttpPost postJob = new HttpPost(HttpHelper.domain + "ping.php");
		
		HttpPost postJob = new HttpPost("http://hopcabtest.herokuapp.com/drivers/1/update_driver_position");

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
/*
			infoJob.add(new BasicNameValuePair("driver_id", driver_id));
			infoJob.add(new BasicNameValuePair("lat", Integer.valueOf(dLat).toString()));
			infoJob.add(new BasicNameValuePair("longi", Integer.valueOf(dLongi).toString()));
			infoJob.add(new BasicNameValuePair("avail", av));
*/
			double templat = (double) dLat;
			templat = templat/1000000;
			
			double templongi = (double) dLongi;
			templongi = templongi/1000000;
			
			infoJob.add(new BasicNameValuePair("latitude", Double.valueOf(templat).toString()));
			infoJob.add(new BasicNameValuePair("longitude", Double.valueOf(templongi).toString()));
			infoJob.add(new BasicNameValuePair("availability", av));

			postJob.setEntity(new UrlEncodedFormEntity(infoJob));

			HttpResponse response = clientJob.execute(postJob);

		} catch (ClientProtocolException e) {

		} catch (IOException e) {

		}
	}

	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	public static String driverPrefQuery(String name, String number, String nric, String license, int company, int type, String querytype) {
		HttpPost postJob = new HttpPost(HttpHelper.domain + "querydriver.php");
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
			infoJob.add(new BasicNameValuePair("name", name));
			infoJob.add(new BasicNameValuePair("number", number));
			infoJob.add(new BasicNameValuePair("nric", nric));
			infoJob.add(new BasicNameValuePair("license", license));
			infoJob.add(new BasicNameValuePair("company", Integer.toString(company)));
			infoJob.add(new BasicNameValuePair("type", Integer.toString(type)));
			infoJob.add(new BasicNameValuePair("querytype", querytype));

			postJob.setEntity(new UrlEncodedFormEntity(infoJob));
			HttpResponse response = clientJob.execute(postJob);

			String driver_id = HttpHelper.request(response);

			return driver_id;

		} catch (Exception ex) {
			return null;

		}

	}

	public static List<String> getPrefList(String s) {

		HttpPost postJob = new HttpPost(HttpHelper.domain + "getPrefOptions.php");
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
			infoJob.add(new BasicNameValuePair("msgtype", s));

			postJob.setEntity(new UrlEncodedFormEntity(infoJob));
			HttpResponse response = clientJob.execute(postJob);

			String jsonString = HttpHelper.request(response);
			JSONArray jArray = new JSONArray(jsonString);

			List<String> list = new ArrayList<String>();
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject json = jArray.getJSONObject(i);
				list.add(json.getString(s));
			}
			return list;

		} catch (Exception ex) {
			return null;

		}

	}

}
