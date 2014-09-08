package com.TakeTaxi.jy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MainMapScreen extends MapActivity implements LocationListener {
	/** Called when the activity is first created. */
	private MapView map = null;
	long start;
	long stop;
	int lat, longi, tempLat, tempLongi, selectedDriver, did;
	public int x, y, newLat, newLongi;
	public GeoPoint localPositionGeoPoint, pt;
	GeoPoint touchedPoint;

	// Location Services //
	LocationManager lm;
	String towers;

	// Buttons //
	Button profile, remap, tools, bFlag;
	View popUp;

	// Map + Overlays //
	Drawable clientMarker, selectedDriverMarker, findnearest, findnearestdown;
	Drawable driverMarker, clientpng;
	OverlayItem item, ditem;
	List<Overlay> overlayList;

	// job details //
	public String driver_id, duration;

	// handler //
	boolean handlerboolean, fromGetNearest = false;

	// Geocoding //
	TextView tvCurrentGeocode;
	String geoadd;
	// Nearest Taxi Time//
	TextView tvNearest;

	// shared pref
	SharedPreferences sharedPref;
	public static String prefFile = "MysharedString";
	public static String nameString = "nameString";
	public static String numString = "numString";

	// starbar
	Drawable starhalf;
	Drawable starwhole;
	Drawable starempty;
	ImageView star1;
	ImageView star2;
	ImageView star3;
	ImageView star4;
	ImageView star5;
	LinearLayout starbar;

	//Error in connection progressdialog //
	ProgressDialog pdDC; 	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// map drawables
		map = (MapView) findViewById(R.id.mvMain);
		driverMarker = getResources().getDrawable(R.drawable.cabs);
		clientMarker = getResources().getDrawable(R.drawable.greendot);
		selectedDriverMarker = getResources().getDrawable(R.drawable.marker);

		// Star bar
		starhalf = getResources().getDrawable(R.drawable.starhalf);
		starwhole = getResources().getDrawable(R.drawable.starwhole);
		starempty = getResources().getDrawable(R.drawable.starempty);
		starbar = (LinearLayout) findViewById(R.id.starbar);
		starbar.setVisibility(8);
		star1 = (ImageView) findViewById(R.id.star1);
		star2 = (ImageView) findViewById(R.id.star2);
		star3 = (ImageView) findViewById(R.id.star3);
		star4 = (ImageView) findViewById(R.id.star4);
		star5 = (ImageView) findViewById(R.id.star5);

		// ////// Temporary Position //////
		tempLat = 1318200;
		tempLongi = 103911651;
		// ////////////////////////////////

		// //// check prefs for name num details ///////
		String prefname = getSharedPreferences(prefFile, 0).getString(nameString, "");
		String prefnum = getSharedPreferences(prefFile, 0).getString(numString, "");
		if (prefname == null || prefnum == null || prefname.equals("") || prefnum.equals("")) {
			Intent openStart = new Intent("com.TakeTaxi.jy.Prefs");
			startActivity(openStart);
		}
		
		// Position information
		newLat = tempLat;
		newLongi = tempLongi;
		overlayList = map.getOverlays();

		getLocalPosition();
		selectedDriver = 0;
		map.getOverlays().add(new MyPosition(clientMarker));
		localPositionGeoPoint = new GeoPoint(tempLat, tempLongi);
		
		// mapview settings //
		map.getController().setCenter(localPositionGeoPoint);
		map.getController().setZoom(17);
		
		// flag button //
		bFlag = (Button) findViewById(R.id.bFlag);
		bFlag.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent openStart = new Intent(getBaseContext(), SubmitJobScreen.class);
				openStart.putExtra("driver_id", driver_id);
				openStart.putExtra("tempLat", newLat);
				openStart.putExtra("tempLongi", newLongi);
				openStart.putExtra("geoadd", geoadd);
				startActivity(openStart);
			}
		});
		tvNearest = (TextView) findViewById(R.id.tvNearest);
		setGeoText();
		getNearestTime();

		handlerboolean = true;

		//  PROFILE BUTTON //
		profileButton();

		// CENTRE MAP BUTTON //
		centreMapButton();

		// BROADCAST BUTTON //
		broadcastButton();

		//Error in connection progressdialog //
		pdDC = new ProgressDialog(this);

	}

	@Override
	public void onBackPressed() {
		return;
	}

	// //////////////////////////HANDLER ///////////////////////////////////////

	Handler handler = new Handler();
	final Runnable r = new Runnable() {
		public void run() {
			if (Query.isNetworkAvailable(getBaseContext())) {

				if (pdDC.isShowing()) {
					pdDC.dismiss();
				}
				
				if (handlerboolean) {
					new DownloadDriverPosition().execute();
					Log.d("TakeTaxi Log", "Handler Ping");
				}
				
			} else {
				if (pdDC.isShowing()) {
				} else {
				noNetworkConnection();
				}
			}
			
			handler.postDelayed(this, 10000);


		}

	};

	private class DownloadDriverPosition extends AsyncTask<String, Void, int[][]> {
		@Override
		protected int[][] doInBackground(String... string) {
			int[][] driverPositionArray = Query.getDriverPosition("all");
			return driverPositionArray;
		}

		@Override
		protected void onPostExecute(int[][] result) {

			if (result == null) {
				handler.removeCallbacks(r);

				AlertDialog alert = new AlertDialog.Builder(MainMapScreen.this).create();
				alert.setMessage("No network connection available. Please try again.");
				alert.setButton("Retry", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						handler.post(r);
					}
				});
				alert.show();
			} else {
				overlayList.clear();
				map.getOverlays().add(new MyPosition(clientMarker));
				map.getOverlays().add(new DriverPosition(driverMarker, result));

				if (selectedDriver != 0) {
					map.getOverlays().add(new selectedDriverPosition(selectedDriverMarker, result));
				}
				map.invalidate();

			}

		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		handler.removeCallbacks(r);
		// lm.removeUpdates(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		lm.requestLocationUpdates(towers, 500, 1, this);
		handler.post(r);

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLocationChanged(Location l) {
		// TODO Auto-generated method stub

		tempLat = (int) (l.getLatitude() * 1E6);
		tempLongi = (int) (l.getLongitude() * 1E6);
		/*
		 * GeoPoint ourLocation = new GeoPoint(lat, longi); OverlayItem overlayItem = new OverlayItem(ourLocation, "Whats up", "2nd String"); CustomPinpoint
		 * custom = new CustomPinpoint(clientpng, TakeTaxiActivity.this); custom.insertPinpoint(overlayItem); overlayList.add(custom);
		 */
	}

	public void onProviderDisabled(String provider) {

	}

	public void onProviderEnabled(String provider) {

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////// OVERLAYS ////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////
	private class MyPosition extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> myposition = new ArrayList<OverlayItem>();
		private Drawable marker = null;
		private OverlayItem inDrag = null;
		private ImageView dragImage = null;
		private int xDragImageOffset = 0;
		private int yDragImageOffset = 0;
		private int xDragTouchOffset = 0;
		private int yDragTouchOffset = 0;

		public MyPosition(Drawable marker) {
			super(boundCenterBottom(marker));
			this.marker = marker;
			GeoPoint aaa = new GeoPoint(newLat, newLongi);

			myposition.add(new OverlayItem(aaa, "me", ""));

			populate();

		}

		@Override
		protected OverlayItem createItem(int i) {
			return myposition.get(i);
		}

		@Override
		public int size() {
			return myposition.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			boundCenterBottom(marker);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			final int action = event.getAction();
			x = (int) event.getX();
			y = (int) event.getY();
			boolean result = false;
			dragImage = (ImageView) findViewById(R.id.drag);
			xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
			yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();

			if (action == MotionEvent.ACTION_DOWN) {
				for (OverlayItem item : myposition) {
					Point p = new Point(0, 0);

					map.getProjection().toPixels(item.getPoint(), p);

					if (hitTest(item, marker, x - p.x, y - p.y)) {
						result = true;
						inDrag = item;
						myposition.remove(inDrag);
						populate();

						xDragTouchOffset = 0;
						yDragTouchOffset = 0;

						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset = x - p.x;
						yDragTouchOffset = y - p.y;
						handlerboolean = false;

						break;
					}
				}
			} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
				setDragImagePosition(x, y);
				handlerboolean = false;
				result = true;
			} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
				dragImage.setVisibility(View.GONE);

				pt = map.getProjection().fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
				OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(), inDrag.getSnippet());

				myposition.add(toDrop);
				populate();

				inDrag = null;

				newLat = pt.getLatitudeE6();
				newLongi = pt.getLongitudeE6();
				setGeoText();
				if (bFlag.isShown()) {
					clickDriverOnMap();
				} else {

					getNearestTime();
				}

				handlerboolean = true;
				result = true;
			}

			return (result || super.onTouchEvent(event, mapView));
		}

		private void setDragImagePosition(int x, int y) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage.getLayoutParams();

			lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y - yDragImageOffset - yDragTouchOffset, 0, 0);
			dragImage.setLayoutParams(lp);
		}
	}

	class DriverPosition extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> driverPositionPoints = new ArrayList<OverlayItem>();
		private Drawable marker = null;

		public DriverPosition(Drawable marker, int[][] driverPositionArray) {
			super(boundCenterBottom(marker));
			this.marker = marker;

			for (int i = 0; i < driverPositionArray.length; i++) {
				if (selectedDriver == driverPositionArray[i][0]) {
				} else {
					GeoPoint driverGeoPoint = new GeoPoint(driverPositionArray[i][1], driverPositionArray[i][2]);
					driverPositionPoints.add(new OverlayItem(driverGeoPoint, Integer.toString(driverPositionArray[i][0]), ""));
				}
			}
			this.populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return driverPositionPoints.get(i);
		}

		@Override
		public int size() {
			return driverPositionPoints.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
		}

		@Override
		protected boolean onTap(int index) {
			ditem = driverPositionPoints.get(index);
			driver_id = ditem.getTitle();
			fromGetNearest = false;
			clickDriverOnMap();
			return true;
		}
	}

	class selectedDriverPosition extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> selectedDriverPositionPoints = new ArrayList<OverlayItem>();
		private Drawable marker = null;

		public selectedDriverPosition(Drawable marker, int[][] driverPositionArray) {
			super(boundCenterBottom(marker));
			this.marker = marker;

			for (int i = 0; i < driverPositionArray.length; i++) {
				if (selectedDriver == driverPositionArray[i][0]) {
					GeoPoint driverGeoPoint = new GeoPoint(driverPositionArray[i][1], driverPositionArray[i][2]);
					selectedDriverPositionPoints.add(new OverlayItem(driverGeoPoint, Integer.toString(driverPositionArray[i][0]), ""));
				}
			}
			this.populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return selectedDriverPositionPoints.get(i);
		}

		@Override
		public int size() {
			return selectedDriverPositionPoints.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
		}

		@Override
		protected boolean onTap(int index) {
			ditem = selectedDriverPositionPoints.get(index);
			driver_id = ditem.getTitle();
			fromGetNearest = false;
			clickDriverOnMap();
			return true;
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	// ///////////// Ontap on map //////////////////////////
	public void clickDriverOnMap() {

		selectedDriver = Integer.valueOf(driver_id);
		handler.removeCallbacks(r);
		handler.post(r);

		/*
		 * map.removeView(popUp); popUp = getLayoutInflater().inflate(R.layout.popup, (ViewGroup) findViewById(R.id.popup_element), false);
		 * 
		 * MapView map = (MapView) findViewById(R.id.mvMain); MapView.LayoutParams mapParams = new MapView.LayoutParams( LayoutParams.FILL_PARENT,
		 * LayoutParams.WRAP_CONTENT, 0, 0, Gravity.NO_GRAVITY); mapParams.mode = MapView.LayoutParams.MODE_VIEW;
		 * 
		 * map.addView(popUp, mapParams);
		 * 
		 * TextView popMid = (TextView) findViewById(R.id.tvMid); TextView popBot = (TextView) findViewById(R.id.tvBot);
		 */
		class asyncGetETA extends AsyncTask<String, Void, String> {
			@Override
			protected String doInBackground(String... string) {

				String fromLat = Double.toString(ditem.getPoint().getLatitudeE6() / 1E6);
				String fromLongi = Double.toString(ditem.getPoint().getLongitudeE6() / 1E6);
				String toLat = Double.toString(newLat / 1E6);
				String toLongi = Double.toString(newLongi / 1E6);

				/*
				 * String urlETA = "http://maps.googleapis.com/maps/api/directions/json?origin=" + fromLat + "," + fromLongi + "&destination=" + toLat + "," +
				 * toLongi + "&sensor=true";
				 */

				// String durationASYNC = Query.getETA(fromLat, fromLongi, toLat, toLongi, "duration");

				String durationASYNC = Query.getETA("", fromLat, fromLongi, toLat, toLongi, "duration");

				Log.d("TakeTaxi Log", fromLat + " " + fromLongi + " " + toLat + " " + toLongi);

				// Log.d("TakeTaxi Log", durationASYNC);
				return durationASYNC;
			}

			@Override
			protected void onPostExecute(String result) {
				Log.d("TakeTaxi Log", "ASYNC duration done " + result);
				Log.d("TakeTaxi Log", "ASYNC duration done " + Query.getDriverDetail("license", driver_id));

				duration = result;
				if (duration.equals("") || duration.equals("null")) {
					duration = Query.getDriverDetail("license", driver_id);
					tvNearest.setText(duration);
				} else {
					duration = Query.getDriverDetail("license", driver_id) + " is about " + duration + " min away";
					tvNearest.setText(duration);
				}

			}
		}
		new asyncGetETA().execute();

		// starcode //
		if (Query.getDriverDetail("thumbsup", driver_id) == null) {

		} else {
			int thumbsup = Integer.valueOf(Query.getDriverDetail("thumbsup", driver_id));
			int thumbsdown = Integer.valueOf(Query.getDriverDetail("thumbsdown", driver_id));
			Log.d("TakeTaxi Log", "thumbsdown " + thumbsdown);
			Log.d("TakeTaxi Log", "thumbsup " + thumbsup);

			int total = thumbsup + thumbsdown;

			Log.d("TakeTaxi Log", "total " + total);

			int rating = thumbsup * 10 / total;
			if (rating != 10)
				rating++;

			Log.d("TakeTaxi Log", "rating " + rating);
			switch (rating) {
			case 1:
				star1.setImageDrawable(starhalf);
				star2.setImageDrawable(starempty);
				star3.setImageDrawable(starempty);
				star4.setImageDrawable(starempty);
				star5.setImageDrawable(starempty);
				break;
			case 2:
				star2.setImageDrawable(starempty);
				star3.setImageDrawable(starempty);
				star4.setImageDrawable(starempty);
				star5.setImageDrawable(starempty);
				break;
			case 3:
				star2.setImageDrawable(starhalf);
				star3.setImageDrawable(starempty);
				star4.setImageDrawable(starempty);
				star5.setImageDrawable(starempty);
				break;
			case 4:
				star3.setImageDrawable(starempty);
				star4.setImageDrawable(starempty);
				star5.setImageDrawable(starempty);
				break;
			case 5:
				star3.setImageDrawable(starhalf);
				star4.setImageDrawable(starempty);
				star5.setImageDrawable(starempty);
				break;
			case 6:
				star4.setImageDrawable(starempty);
				star5.setImageDrawable(starempty);
				break;
			case 7:
				star4.setImageDrawable(starhalf);
				star5.setImageDrawable(starempty);
				break;
			case 8:
				star5.setImageDrawable(starempty);
				break;
			case 9:
				star5.setImageDrawable(starhalf);
				break;
			case 10:
				break;
			}
			starbar.setVisibility(1);
		}

		Log.d("TakeTaxi Log", "clicked selected driver");

		bFlag.setVisibility(1);

		Log.d("TakeTaxi Log", "finish selected driver");

	}

	public void getLocalPosition() {

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		towers = lm.getBestProvider(crit, false);
		Location location = lm.getLastKnownLocation(towers);

		if (location != null) {

			// changed lat/longi to temp for device testing
			tempLat = (int) (location.getLatitude() * 1E6);
			tempLongi = (int) (location.getLongitude() * 1E6);

		} else {
			// ///////////////////////////////////////////////////////////////////////////////////
			// SHOULD PUT A NO NETWORK/GPS LOC ALERT
			// /////////////////////////////////////////////////////////////////////////////////
			// /////////////////////////////////////////////////////////////////////////////////
			// /////////////////////////////////////////////////////////////////////////////////
			// /////////////////////////////////////////////////////////////////////////////////
			// /////////////////////////////////////////////////////////////////////////////////

		}
	}

	public void setGeoText() {

		geoadd = null;
		Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {
			List<Address> address = geocoder.getFromLocation(newLat / 1E6, newLongi / 1E6, 1);
			if (address.size() > 0) {
				geoadd = "";
				for (int i = 0; i < address.get(0).getMaxAddressLineIndex(); i++) {
					if (i == 0) {
						geoadd += address.get(0).getAddressLine(i);
					} else {
						geoadd += "\n" + address.get(0).getAddressLine(i);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		tvCurrentGeocode = (TextView) findViewById(R.id.tvCurrentGeocode);

		tvCurrentGeocode.setText(geoadd);
	}

	public void centreMapButton() {
		ImageButton ibmap = (ImageButton) findViewById(R.id.ibMap);
		ibmap.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub

				selectedDriver = 0;
				new DownloadDriverPosition().execute();
				map.removeAllViews();

				getLocalPosition();
				getNearestTime();
				starbar.setVisibility(8);

				bFlag.setVisibility(8);
				map.invalidate();
				newLat = tempLat;
				newLongi = tempLongi;
				map.getController().setCenter(localPositionGeoPoint);
				map.getController().setZoom(17);
			}
		});
	}

	public void profileButton() {
		ImageButton ibprofile = (ImageButton) findViewById(R.id.ibProfile);
		ibprofile.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent openStart = new Intent("com.TakeTaxi.jy.Prefs");
				startActivity(openStart);
			}
		});

	}

	public void broadcastButton() {
		
		
		
		final Button bBroadcast = (Button) findViewById(R.id.bBroadcast);
		bBroadcast.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent openStart = new Intent(getBaseContext(), SubmitJobScreen.class);
				openStart.putExtra("driver_id", "0");
				openStart.putExtra("tempLat", newLat);
				openStart.putExtra("tempLongi", newLongi);
				openStart.putExtra("geoadd", geoadd);

				startActivity(openStart);

			}
		});
		
		
		
		
		
		
		
	}

	public void getNearestTime() {
		Log.d("TakeTaxi Log", "getting nearest");
		String tempDur = null;
		try {

			if (Query.getNearestDriver(newLat, newLongi) == null) {
			} else {
				tempDur = Query.getNearestDriver(newLat, newLongi).getString("time");
			}

		} catch (JSONException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (tempDur == null) {
			duration = "Welcome to Tap-a-Taxi";

		} else {
			duration = "Nearest taxi is about " + tempDur + " min away";

		}
		tvNearest.setText(duration);
		Log.d("TakeTaxi Log", duration);
	}
	
	
	public void noNetworkConnection() {

		//progress dialog for DC
		pdDC = ProgressDialog.show(MainMapScreen.this, "", "Trying to connect to servers...\nPlease check internet connection.", true, false);

	}
	
}