package com.TaxiDriver.jy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TaxiDriver.jy.TaxiDriverActivity.ReceiveMessages;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class DriverOnroute extends MapActivity implements LocationListener {
	MapView map;
	int tempLat, tempLongi, dLat, dLongi, clientLat, clientLongi;
	List<Overlay> overlayList;
	Drawable clientpng, taxipng;
	private static final String driver_id = "1";
	String job_id, destination, geoadd, pickup;
	boolean handlerboolean;
	
	//Error in connection progressdialog //
	ProgressDialog pdDC; 
	
	// Broadcast Receiver //
	ReceiveMessages myReceiver = null;
	Boolean myReceiverIsRegistered = false;
	Intent updateBroadcast;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		Bundle extras = getIntent().getExtras();

		job_id = extras.getString("job_id");
		clientLat = Integer.valueOf(extras.getString("clientLat"));
		clientLongi = Integer.valueOf(extras.getString("clientLongi"));
		destination = extras.getString("destination");
		geoadd = extras.getString("geoadd");
		pickup = extras.getString("pickup");

		setContentView(R.layout.onroute);

		// driver_id = getSharedPreferences("prefFile", 0).getString("driverString", "");

		tempLat = 1321032;
		tempLongi = 103912745;
		clientpng = getResources().getDrawable(R.drawable.greendot);
		taxipng = getResources().getDrawable(R.drawable.cabs);

		map = (MapView) findViewById(R.id.mvOnroute);
		map.setBuiltInZoomControls(true);
		overlayList = map.getOverlays();

		//Error connection progress dialog //
		pdDC = new ProgressDialog(this);

		
		// driver pos
		GeoPoint dpos = new GeoPoint(tempLat, tempLongi);
		map.getController().setCenter(dpos);
		map.getController().setZoom(17);
		OverlayItem doverlayItem = new OverlayItem(dpos, "ME", "2nd String");
		CustomPinpoint dcustom = new CustomPinpoint(taxipng, DriverOnroute.this);
		dcustom.insertPinpoint(doverlayItem);
		overlayList.add(dcustom);

		// client pos
		GeoPoint cpos = new GeoPoint(clientLat, clientLongi);
		map.getController().setCenter(cpos);
		map.getController().setZoom(17);
		OverlayItem coverlayItem = new OverlayItem(cpos, "Client", "2nd String");
		CustomPinpoint ccustom = new CustomPinpoint(clientpng, DriverOnroute.this);
		ccustom.insertPinpoint(coverlayItem);
		overlayList.add(ccustom);

		TextView tvTop = (TextView) findViewById(R.id.tvTopLeft2);
		TextView tvMid = (TextView) findViewById(R.id.tvMidLeft2);
		TextView tvBot = (TextView) findViewById(R.id.tvBotLeft2);
		tvTop.setText(geoadd);
		tvMid.setText(pickup);
		tvBot.setText(destination);

		
		// CANCEL BUTTON //
		Button onrouteCancelButton = (Button) findViewById(R.id.bORnoshow);
		onrouteCancelButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// PAX NO SHOW //
				AlertDialog.Builder noshowalert = new AlertDialog.Builder(DriverOnroute.this);
				noshowalert.setMessage("Do you want to report a no-show?");
				noshowalert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						return;
					}

				});
				noshowalert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						String check = DriverQuery.jobQuery("clientnoshow", job_id, 0, driver_id);
						if (check.equals("done")) {
							Intent openStart = new Intent("com.TaxiDriver.jy.TaxiDriverActivity");
							startActivity(openStart);
						} else {
							Toast.makeText(getBaseContext(), "Could not connect to server.\nPlease try again.", Toast.LENGTH_SHORT).show();
						}
					}
				});

				noshowalert.setNeutralButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String check = DriverQuery.jobQuery("drivercancel", job_id, 0, driver_id);
						if (check.equals("done")) {
							Intent openStart = new Intent("com.TaxiDriver.jy.TaxiDriverActivity");
							startActivity(openStart);
						} else {
							Toast.makeText(getBaseContext(), "Could not connect to server.\nPlease try again.", Toast.LENGTH_SHORT).show();
						}
					}
				});
				noshowalert.create().show();

			}
		});

		Button onRoutePassengerPicked = (Button) findViewById(R.id.bORpicked);
		onRoutePassengerPicked.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String check = DriverQuery.jobQuery("driverpicked", job_id, 0, driver_id);
				if (check.equals("done")) {
					Toast.makeText(DriverOnroute.this, "Thank you for using CabApp!\nHave a nice day!", Toast.LENGTH_SHORT).show();
					Intent openStart = new Intent("com.TaxiDriver.jy.TaxiDriverActivity");
					startActivity(openStart);
				} else {
					Toast.makeText(getBaseContext(), "Could not connect to server.\nPlease try again.", Toast.LENGTH_SHORT).show();
				}
			}
		});

		handlerboolean = true;
		handler.post(run);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		return;
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		

		if (myReceiverIsRegistered) {
			unregisterReceiver(myReceiver);
			myReceiverIsRegistered = false;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (!myReceiverIsRegistered) {
			registerReceiver(myReceiver, new IntentFilter("com.TaxiDriver.jy.DATA_UPDATED"));
			registerReceiver(myReceiver, new IntentFilter("com.TaxiDriver.jy.NO_NETWORK_CONNECTION"));

			myReceiverIsRegistered = true;
		}
	}
	
	
	// ////////////////////////////////////HANDLER/////////////////////////////////////////////////

	final Handler handler = new Handler();
	final Runnable run = new Runnable() {
		public void run() {
			if (DriverQuery.isNetworkAvailable(getBaseContext())) {
				if (handlerboolean) {
					new CheckJobStatus().execute();
				}
				handler.postDelayed(this, 10000);
			}
		}
	};

	private class CheckJobStatus extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... string) {
			JSONObject json = DriverQuery.getJobInfo(job_id);
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			pingJob(result);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////

	public void pingJob(JSONObject json) {
		// /////////////////////// Check if Client Cancelled
		// //////////////////////////////////////////
		int ccancel;
		try {
			ccancel = json.getInt("ccancel");
			if (ccancel == 1) {
				handlerboolean = false;
				handler.removeCallbacks(run);
				AlertDialog builder = new AlertDialog.Builder(DriverOnroute.this).create();
				builder.setMessage("Job has been cancelled.");
				builder.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent openStart = new Intent("com.TaxiDriver.jy.TaxiDriverActivity");
						startActivity(openStart);
					}
				});
				builder.show();
			}
		} catch (JSONException e) {
		}

	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	class CustomPinpoint extends ItemizedOverlay<OverlayItem> {

		private ArrayList<OverlayItem> pinpoints = new ArrayList<OverlayItem>();
		private Context c;

		public CustomPinpoint(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
			// TODO Auto-generated constructor stub
		}

		public CustomPinpoint(Drawable m, Context context) {
			this(m);
			c = context;
			// TODO Auto-generated constructor stub
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return pinpoints.get(i);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return pinpoints.size();
		}

		public void insertPinpoint(OverlayItem item) {
			pinpoints.add(item);
			this.populate();
		}

		@Override
		protected boolean onTap(int index) {
			return true;
		}

	}
	
	// ///////////////////////// Service Interaction /////////////////////

		public class ReceiveMessages extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {

				if (intent.getAction().matches("com.TaxiDriver.jy.DATA_UPDATED")) {
					Log.d("Ping Service", "Onroute connected");
					if (pdDC.isShowing()) {
						pdDC.dismiss();
					}
				} else if (intent.getAction().matches("com.TaxiDriver.jy.NO_NETWORK_CONNECTION")) {
					if (pdDC.isShowing()) {
					
					} else {
					noNetworkConnection();
					}
				}
			}
		}
		
		public void noNetworkConnection() {

			//progress dialog for DC
			pdDC = ProgressDialog.show(DriverOnroute.this, "", "Trying to connect to servers...", true, false);

		}
}
