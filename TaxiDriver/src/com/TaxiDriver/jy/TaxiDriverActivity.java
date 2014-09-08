package com.TaxiDriver.jy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class TaxiDriverActivity extends MapActivity implements LocationListener {
	//private static int tempLat = 1321032;
	//private static int tempLongi = 103912745;
	private static final String driver_id = "1";
	
	private static int tempLat = 3138116;
	private static int tempLongi = 101687393;
	

	// Location Services //
	LocationManager lm;
	String towers;

	// Map OVerlay related //
	MapView map;
	List<Overlay> overlayList;
	Drawable d, jobmarker, taxipng, selectedjobmarker;
	OverlayItem tapPaxItem;
	GeoPoint tempdriverpos = new GeoPoint(3138116, 101687393);

	// Forms used //
	RadioButton rbOn, rbOff;
	RadioGroup rbAvail;
	ImageButton ibProfile;
	LinearLayout buttonGroup, jobLayout;

	// View related //
	View popUp2, popUp;
	CountDownTimer timer;
	ProgressDialog pd, pdDC; // error connection pd
	RelativeLayout mv;

	// Job related variables//
	String destination, avail, geoadd;
	ArrayList<Object> rejectList = new ArrayList<Object>();
	int counter, lat, longi, clientLat, clientLongi, clientnumber;
	public int k;
	Button ping, job;

	// Broadcast Receiver //
	ReceiveMessages myReceiver = null;
	Boolean myReceiverIsRegistered = false;
	Intent updateBroadcast;

	// Global var declarations
	GlobalVar gv;
	

	// Handler //
	boolean handlerboolean;

	// Sounds//
	private SoundPool soundPool;
	private int soundID;
	boolean loaded = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		gv = (GlobalVar) this.getApplication();

		gv.isTDAActive(true);
		gv.driverLat(tempLat);
		gv.driverLongi(tempLongi);
		// driver_id = getSharedPreferences("prefFile", 0).getString("driverString", "");
		gv.driver_id(driver_id);

		// ///// Service Binding ///////
		Intent service = new Intent(getBaseContext(), PingService.class);
		startService(service);
		doBindService();
		Log.d("PingService", "Ping Service Started");
		myReceiver = new ReceiveMessages();
		updateBroadcast = new Intent("com.TaxiDriver.jy.DO_UPDATE");

		// ///// Popup for new user settings ///////

		map = (MapView) findViewById(R.id.mvMain);

		if (driver_id.equals("")) {
			pwHandler.postDelayed(pwRunnable, 1000);

		}

		// Map overlay //
		jobmarker = getResources().getDrawable(R.drawable.greendot);
		taxipng = getResources().getDrawable(R.drawable.cabs);
		selectedjobmarker = getResources().getDrawable(R.drawable.marker);
		overlayList = map.getOverlays();
		driverLocalPosition();

		map.getController().setCenter(tempdriverpos);
		map.getController().setZoom(13);
		buttonGroup = (LinearLayout) findViewById(R.id.buttonGroup);
		driverLocalPosition();
		handlerboolean = false;

		// layout test//
		jobLayout = (LinearLayout) findViewById(R.id.jobLayout);
		jobLayout.bringToFront();
		jobLayout.setVisibility(8);

		ibProfile = (ImageButton) findViewById(R.id.ibProfile);
		ibProfile.bringToFront();
		ibProfile.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent openStart = new Intent("com.TaxiDriver.jy.DriverInfo");
				startActivity(openStart);
			}

		});

		rbAvail = (RadioGroup) findViewById(R.id.rbAvail);
		rbOn = (RadioButton) findViewById(R.id.rbOn);
		rbOff = (RadioButton) findViewById(R.id.rbOff);
		buttonDisplay(true);

		if (gv.driverAvail() == null) {
			gv.driverAvail("0");
			rbOff.setChecked(true);
			sendBroadcast(updateBroadcast);

		} else {
			if (gv.driverAvail() == "1") {
				rbOn.setChecked(true);
				sendBroadcast(updateBroadcast);

			} else {
				rbOff.setChecked(true);
				sendBroadcast(updateBroadcast);

			}

		}

		rbAvail.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (rbOn.isChecked()) {
					gv.driverAvail("1");
					sendBroadcast(updateBroadcast);
				}
				if (rbOff.isChecked()) {
					gv.driverAvail("0");
					sendBroadcast(updateBroadcast);
				}

			}
		});
		// Load Sound
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundID = soundPool.load(this, R.raw.alert, 1);

		pdDC = new ProgressDialog(this);

		
		
		sendBroadcast(updateBroadcast);
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

		gv.isTDAActive(false);

		if (myReceiverIsRegistered) {
			unregisterReceiver(myReceiver);
			myReceiverIsRegistered = false;
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		gv.isTDAActive(true);
		if (!myReceiverIsRegistered) {
			registerReceiver(myReceiver, new IntentFilter("com.TaxiDriver.jy.DATA_UPDATED"));
			registerReceiver(myReceiver, new IntentFilter("com.TaxiDriver.jy.NO_NETWORK_CONNECTION"));

			myReceiverIsRegistered = true;
		}
	}

	// ////////////////////////////////////driverLocalPosition
	// /////////////////////////////////

	public void driverLocalPosition() {
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		towers = lm.getBestProvider(crit, false);
		Location location = lm.getLastKnownLocation(towers);
		if (location != null) {

			// changed lat/longi to temp for device testing
			tempLat = (int) (location.getLatitude() * 1E6);
			tempLongi = (int) (location.getLongitude() * 1E6);

		}

		GeoPoint tempdriverpos = new GeoPoint(tempLat, tempLongi);
		OverlayItem overlayItem1 = new OverlayItem(tempdriverpos, "ME", "2nd String");
		DriverPositionOverlay icustom = new DriverPositionOverlay(taxipng, TaxiDriverActivity.this);
		icustom.insertPinpoint(overlayItem1);
		overlayList.add(icustom);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////

	TextView popCounter = null, popTop = null, popMid = null, popBot = null, tvTimer = null, tvTopRight = null;
	boolean anyOpenJobs = false;
	String lastJobViewed;
	int starttime;

	// get Jobs from jobList array - processes the data to determine what IU
	// should do //
	@SuppressWarnings("unused")
	public void getJobs(final String[][] joblist) {

		overlayList.clear();
		driverLocalPosition();
		map.invalidate();

		if ((joblist == null || joblist.length == 0) && anyOpenJobs == true) {
			anyOpenJobs = false;
			map.removeAllViews();
			overlayList.clear();
			buttonDisplay(true);
			lastJobViewed = null;

		}

		if (joblist != null) {
			final int jobCount = joblist.length;
			if (anyOpenJobs == false) {
				k = 0;

				// Sound //
				AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				final float volume = actualVolume / maxVolume;
				// play sound
				soundPool.play(soundID, volume, volume, 1, 2, 1f);

				buttonDisplay(false);
				anyOpenJobs = true;
				lastJobViewed = joblist[k][0];

			} else {
				// timer.cancel();

			}

			for (int i = 0; i < jobCount; i++) {
				k = 0;
				if (lastJobViewed.equals(joblist[i][0])) {
					k = i;
				} else {
				}
			}

			starttime = Calendar.getInstance().get(Calendar.SECOND);

			displayJobInfo(joblist, k, jobCount);

			Button minus = (Button) findViewById(R.id.nJobMinus);
			minus.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (k > 0) {
						k--;
						displayJobInfo(joblist, k, jobCount);
					}
				}
			});

			Button plus = (Button) findViewById(R.id.nJobPlus);
			plus.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (k < jobCount - 1) {
						k++;
						displayJobInfo(joblist, k, jobCount);
					}
				}
			});

			acceptJobButton(joblist, k);
			rejectJobButton(joblist, k);

		}

	}

	// //////////////// Method that presents joblist array in various Textviews
	// etc//////////////
	public void displayJobInfo(final String[][] joblist, final int jobno, int jobCount) {

		popCounter = (TextView) findViewById(R.id.tvCount);
		popTop = (TextView) findViewById(R.id.tvTopLeft);
		popMid = (TextView) findViewById(R.id.tvMidLeft);
		popBot = (TextView) findViewById(R.id.tvBotLeft);
		tvTimer = (TextView) findViewById(R.id.tvTimer);
		tvTopRight = (TextView) findViewById(R.id.tvTopRight);
		
		
		////
		class setTopRightText extends AsyncTask<String, Void, String> {
			@Override
			protected String doInBackground(String... string) {
				String toLat = Double.toString(Integer.valueOf(joblist[jobno][1]) / 1E6);
				String toLongi = Double.toString(Integer.valueOf(joblist[jobno][2]) / 1E6);
				String fromLat = Double.toString(tempLat / 1E6);
				String fromLongi = Double.toString(tempLongi / 1E6);

				String distance = DriverQuery.getETA("", fromLat, fromLongi, toLat, toLongi, "distance");
				return distance;
			}

			@Override
			protected void onPostExecute(String result) {
				tvTopRight.setText(result + " km");

			}
		}
		
		//new setTopRightText().execute();

		popTop.setText(joblist[jobno][3]);
		popMid.setText(joblist[jobno][4]);
		popBot.setText(joblist[jobno][5]);
		popCounter.setText("Job " + String.valueOf(jobno + 1) + " of " + jobCount);
		try {
			timer.cancel();
		} catch (Exception e) {
		}

		int timeLeft = 60000 - (Integer.valueOf(joblist[jobno][6]) * 1000) - ((Calendar.getInstance().get(Calendar.SECOND) - starttime) * 1000);

		if (timeLeft < 0) {

			pd = ProgressDialog.show(TaxiDriverActivity.this, "", "Processing", true, false);
			DriverQuery.jobQuery("drivercancel", joblist[jobno][0], 0, "");
			pd.dismiss();
			sendBroadcast(updateBroadcast);

		} else {
			timer = new CountDownTimer((timeLeft), 1000) {
				public void onTick(long millisUntilFinished) {
					tvTimer.setText("Time remaining: " + millisUntilFinished / 1000 + "s");
				}

				public void onFinish() {
					DriverQuery.jobQuery("drivercancel", joblist[jobno][0], 0, "");

					// new CheckJob().execute();
				}
			}.start();

		}
		overlayList.clear();
		driverLocalPosition();
		map.getOverlays().add(new selectedJobPosition(selectedjobmarker, joblist));
		map.getOverlays().add(new JobPosition(jobmarker, joblist));

		lastJobViewed = joblist[k][0];
		map.invalidate();
	}

	// /////////////////////////// Accept Job Button ///////////////////////

	public void acceptJobButton(final String[][] joblist, final int jobno) {
		final Button send = (Button) findViewById(R.id.bSend);
		send.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				pd = ProgressDialog.show(TaxiDriverActivity.this, "", "Processing", true, false);

				String check = DriverQuery.jobQuery("driveraccept", joblist[jobno][0], 0, driver_id);
				if (check.equals("done")) {
					timer.cancel();

					Handler myHandler = new Handler();
					Runnable mMyRunnable = new Runnable() {
						public void run() {
							gv.driverAvail("0");

							sendBroadcast(updateBroadcast);
							pd.dismiss();
							Intent openStart = new Intent(getBaseContext(), DriverOnroute.class);
							openStart.putExtra("job_id", joblist[jobno][0]);
							openStart.putExtra("clientLat", joblist[jobno][1]);
							openStart.putExtra("clientLongi", joblist[jobno][2]);
							openStart.putExtra("destination", joblist[jobno][5]);
							openStart.putExtra("clientnumber", joblist[jobno][7]);
							openStart.putExtra("geoadd", joblist[jobno][3]);
							openStart.putExtra("pickup", joblist[jobno][4]);
							startActivity(openStart);
						}
					};

					myHandler.postDelayed(mMyRunnable, 5000);

				} else {
					pd.dismiss();

					AlertDialog alert = new AlertDialog.Builder(TaxiDriverActivity.this).create();
					alert.setMessage("No network connection available. Please try again.");
					alert.setButton("Retry", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							send.performClick();
						}
					});
					alert.setButton2("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});
					alert.show();

				}

			}

		});

	}

	// ////////////////////// Reject Job Button /////////////////////////
	public void rejectJobButton(final String[][] joblist, final int jobno) {
		final Button cancel = (Button) findViewById(R.id.bReject);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (joblist[jobno][8].equals("0")) {
					String rejectBroadcastjob = new String(joblist[jobno][0]);
					rejectList.add(rejectBroadcastjob);
					gv.rejectList(rejectList);

					sendBroadcast(updateBroadcast);

				} else {
					pd = ProgressDialog.show(TaxiDriverActivity.this, "", "Processing", true, false);

					String check = DriverQuery.jobQuery("drivercancel", joblist[jobno][0], 0, driver_id);
					pd.dismiss();
					if (check.equals("done")) {
						sendBroadcast(updateBroadcast);

					} else {
						AlertDialog alert = new AlertDialog.Builder(TaxiDriverActivity.this).create();
						alert.setMessage("No network connection available. Please try again.");
						alert.setButton("Retry", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								cancel.performClick();
							}
						});
						alert.setButton2("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						});
						alert.show();

					}
				}

			}

		});

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLocationChanged(Location arg0) {
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

	// ////////////////////////////////////////////////////////////////////////
	// ////////////////////// OVERLAYS ////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////

	// ///////////////////////// Overlay - not selected /////////////////////
	
	class JobPosition extends ItemizedOverlay<OverlayItem> {
		String[][] internalJoblist;
		int internalJobCount;
		private ArrayList<OverlayItem> jobPositionPoints = new ArrayList<OverlayItem>();
		private Drawable marker = null;

		public JobPosition(Drawable marker, String[][] joblist) {
			super(boundCenterBottom(marker));
			this.marker = marker;
			internalJoblist = joblist;
			internalJobCount = joblist.length;
			for (int i = 0; i != joblist.length; i++) {
				if (i != k && joblist != null) {
					GeoPoint jobGeoPoint = new GeoPoint(Integer.valueOf(joblist[i][1]), Integer.valueOf(joblist[i][2]));
					jobPositionPoints.add(new OverlayItem(jobGeoPoint, Integer.toString(i), ""));
				}
			}
			this.populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return jobPositionPoints.get(i);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return jobPositionPoints.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
		}

		@Override
		protected boolean onTap(int index) {
			tapPaxItem = jobPositionPoints.get(index);
			k = Integer.valueOf(tapPaxItem.getTitle());
			displayJobInfo(internalJoblist, k, internalJobCount);
			return true;
		}
	}

	// ///////////////////////// Overlay - selected /////////////////////

	class selectedJobPosition extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> selectedJobPositionPoints = new ArrayList<OverlayItem>();
		private Drawable marker = null;

		public selectedJobPosition(Drawable marker, String[][] joblist) {
			super(boundCenterBottom(marker));
			this.marker = marker;

			GeoPoint jobGeoPoint = new GeoPoint(Integer.valueOf(joblist[k][1]), Integer.valueOf(joblist[k][2]));
			selectedJobPositionPoints.add(new OverlayItem(jobGeoPoint, "", ""));
			this.populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return selectedJobPositionPoints.get(i);
		}

		@Override
		public int size() {
			return selectedJobPositionPoints.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
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
				getJobs(gv.jobList());
				Log.d("Ping Service", "PingService works - getJobs called");
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

	private PingService mBoundService;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mBoundService = ((PingService.LocalBinder) service).getService();

			// Tell the user about this for our demo.
			Log.d("TDA", "Bind service connected");
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
			Log.d("TDA", "Bind service disconnected");

		}
	};
	boolean mIsBound;

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(TaxiDriverActivity.this, PingService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
		Intent service = new Intent(getBaseContext(), PingService.class);
		stopService(service);
	}

	// /// New User Popup /////
	Handler pwHandler = new Handler();
	Runnable pwRunnable = new Runnable() {
		public void run() {
			Display display = getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();

			LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			final PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.newuser, null, false), width, height, true);
			RelativeLayout pwlayout = (RelativeLayout) findViewById(R.id.main);
			pw.showAtLocation(pwlayout, Gravity.CENTER, 0, 0);
			pw.setBackgroundDrawable(new BitmapDrawable());
			pw.setTouchInterceptor(new OnTouchListener() {

				public boolean onTouch(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					pw.dismiss();
					Intent openStart = new Intent("com.TaxiDriver.jy.DriverInfo");
					startActivity(openStart);
					return true;
				}
			});

		}
	};

	// /// Set button display /////
	public void buttonDisplay(boolean boo) {
		if (boo) {
			rbAvail.setVisibility(1);
			buttonGroup.setVisibility(8);
			ibProfile.setVisibility(1);
			jobLayout.setVisibility(8);

		} else {
			rbAvail.setVisibility(8);
			buttonGroup.setVisibility(1);
			ibProfile.setVisibility(8);
			jobLayout.setVisibility(1);

		}

	}

	// /// No Network Alert /////

	public void noNetworkConnection() {

		// progress dialog for DC
		pdDC = ProgressDialog.show(TaxiDriverActivity.this, "", "Trying to connect to servers...", true, false);

	}
}