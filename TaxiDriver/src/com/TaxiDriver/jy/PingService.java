package com.TaxiDriver.jy;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.TaxiDriver.jy.TaxiDriverActivity.ReceiveMessages;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class PingService extends Service implements LocationListener {
	// Run Interval //
	private Timer timer = new Timer();
	private static final long UPDATE_INTERVAL = 10000;

	// Global Var//
	public static GlobalVar gv;
	String driver_id;
	String driverAvail;
	int driverLat;
	int driverLongi;
	ArrayList<Object> rejectList;

	// BroadCast Receivers//
	ReceiveMessagesService myReceiver = null;
	Boolean myReceiverIsRegistered = false;

	public void onCreate() {
		super.onCreate();
		myReceiver = new ReceiveMessagesService();
		registerReceiver(myReceiver, new IntentFilter("com.TaxiDriver.jy.DO_UPDATE"));

		gv = ((GlobalVar) this.getApplication());
		pollForUpdates();
	}

	private void pollForUpdates() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {

				runJob();

			}
		}, 0, UPDATE_INTERVAL);
		Log.d(getClass().getSimpleName(), "Ping Timer started.");
		
	}

	public void runJob() {
		if (DriverQuery.isNetworkAvailable(getBaseContext())) {

			Log.d("TDA", "PS GV " + gv.toString());

			driver_id = gv.driver_id();
			driverAvail = gv.driverAvail();
			driverLat = gv.driverLat();
			driverLongi = gv.driverLongi();
			rejectList = gv.rejectList();
			// PING CODE HERE!!!!!!
			String[][] jobList = DriverQuery.pingForJobAndPosition(driver_id, driverLat, driverLongi, rejectList, driverAvail);

			// Debug Logs
			Log.d("Ping Service", driver_id + " " + driverLat + " " + driverLongi + " " + rejectList + " " + driverAvail);
			int jobcount;
			if (jobList != null && jobList.length != 0) {
				jobcount = jobList.length;
				gv.jobList(jobList);

				if (!gv.isTDAActive()) {
					Intent intent = new Intent(getBaseContext(), OutsideAlert.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}

			} else {
				jobcount = 0;
				gv.jobList(null);
			}
			Intent i = new Intent("com.TaxiDriver.jy.DATA_UPDATED");
			sendBroadcast(i);
			Log.d("Ping Service", "jobList has " + jobcount);

		} else {
			Intent i = new Intent("com.TaxiDriver.jy.NO_NETWORK_CONNECTION");
			sendBroadcast(i);

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
		Log.d(getClass().getSimpleName(), "Ping Timer stopped.");

	}

	public class LocalBinder extends Binder {
		PingService getService() {
			return PingService.this;
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	public class ReceiveMessagesService extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			runJob();
			Log.d("Ping Service", "Receiving Message from activity broadcast");
		}
	}

	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

}