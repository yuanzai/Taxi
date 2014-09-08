package com.TakeTaxi.jy;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class OnrouteScreen extends MapActivity implements LocationListener {
	MapView map;
	int tempLat, tempLongi, dLat, dLongi;
	List<Overlay> overlayList;
	Drawable clientpng, taxipng;
	String job_id, driver_id;
	// PopupWindow pw;
	boolean handlerboolean;
	boolean thumbsupboolean = false;
	boolean thumbsdownboolean = false;
	GeoPoint mySubmittedPosition, driverPosition;
	int rating = 0;
	
	TextView tvORTop;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		Bundle extras = getIntent().getExtras();
		job_id = extras.getString("job_id");
		tempLat = extras.getInt("tempLat");
		tempLongi = extras.getInt("tempLongi");

		try {
			driver_id = Query.getJobInfo(job_id).getString("driver_id");
		} catch (JSONException e) {
		}

		clientpng = getResources().getDrawable(R.drawable.greendot);
		taxipng = getResources().getDrawable(R.drawable.cabs);

		setContentView(R.layout.onroute);
		map = (MapView) findViewById(R.id.mvOnroute);
		map.setBuiltInZoomControls(true);
		overlayList = map.getOverlays();

		mySubmittedPosition = new GeoPoint(tempLat, tempLongi);
		map.getController().setCenter(mySubmittedPosition);
		map.getController().setZoom(17);
		localpos();

	TextView tvTaxiLicense = (TextView) findViewById(R.id.tvTaxiLicense);
		//TextView tvTaxiType = (TextView) findViewById(R.id.tvTaxiType);

		tvTaxiLicense.setText(Query.getDriverDetail("license", driver_id));
		//tvTaxiType.setText(job_id);

		
		tvORTop = (TextView) findViewById(R.id.tvORTop);
		tvORTop.setText(Query.getDriverDetail("name", driver_id) + " is en route");
		getDriverPosition(driver_id);

		handlerboolean = true;

		handler.postDelayed(r, 5000);

		// ///////////////////////////// CANCEL BUTTON ///////////////
		// ////////////////////////////////////////////////////////////
		Button cancel = (Button) findViewById(R.id.bOnrouteCancelCall);
		cancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				try {

					JSONObject json = Query.getJobInfo(job_id);

					int starttime = json.getInt("datetime");

					// ////////////////// CANCEL BUTTON - REPORT NO SHOW /////
					if (starttime + 300 <= Query.getServerTime()) {
						handlerboolean = false;
						handler.removeCallbacks(r);

						AlertDialog dcancelbuilder = new AlertDialog.Builder(
								OnrouteScreen.this).create();
						dcancelbuilder
								.setMessage("Job has been cancelled.\nWould you like to report a driver no-show?.");

						// ///////// NO button - normal client cancel ////////
						button_cancelJob(dcancelbuilder);
						// ///////// YES button - report driver NO SHOW ////////
						button_cancelJob_driverNoShow(dcancelbuilder);

						dcancelbuilder.show();

					} else {
						handlerboolean = false;
						handler.removeCallbacks(r);

						alertdialog_canceljob();
					}

				} catch (JSONException e) {
				}

			}
		});
	}

	// ////////////////////////////////////////// HANDLER ////////
	Handler handler = new Handler();
	Runnable r = new Runnable() {
		public void run() {
			if (Query.isNetworkAvailable(getBaseContext())) {

				if (handlerboolean) {

					new DownloadDriverPosition().execute();
					handler.postDelayed(this, 5000);
				}
			} else {

				AlertDialog alert = new AlertDialog.Builder(OnrouteScreen.this)
						.create();
				alert.setMessage("No network connection available. Please try again.");
				alert.setButton("Retry", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						handler.removeCallbacks(r);
						handler.post(r);
					}
				});
				alert.show();
			}
		}
	};

	private class DownloadDriverPosition extends
			AsyncTask<String, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... string) {

			return Query.getJobInfo(job_id);
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			overlayList.clear();
			localpos();
			getDriverPosition(driver_id);
			pingpicked(result);

		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////

	public void localpos() {

		OverlayItem myoverlayItem = new OverlayItem(mySubmittedPosition, "ME",
				"2nd String");
		CustomPinpoint mycustom = new CustomPinpoint(clientpng,
				OnrouteScreen.this);
		mycustom.insertPinpoint(myoverlayItem);
		overlayList.add(mycustom);
	}

	public void pingpicked(JSONObject json) {

		try {

			int driverpicked = json.getInt("picked");
			int drivercancelled = json.getInt("dcancel");
			int starttime = json.getInt("datetime");

			// /////////////////////////////// DRIVER CANCEL ////////
			if (drivercancelled == 1) {

				// //// DRIVER CANCEL LATE /////

				if (starttime + 300 <= Query.getServerTime()) {
					handlerboolean = false;
					handler.removeCallbacks(r);

					AlertDialog dcancelbuilder = new AlertDialog.Builder(
							OnrouteScreen.this).create();
					dcancelbuilder
							.setMessage("Job has been cancelled.\nWould you like to report a late cancellation?.");

					// //// DRIVER CANCEL LATE - NO REPORT LATE/////
					button_cancelJob_noquery(dcancelbuilder);
					// //// DRIVER CANCEL LATE - REPORT LATE /////
					button_drivercancel_reportlate(dcancelbuilder);

					dcancelbuilder.show();

				} else {
					// /////////////////////////////// DRIVER CANCEL NO ALERTS -
					// WITHIN TIME LIMIT///////////////
					handlerboolean = false;
					handler.removeCallbacks(r);

					alertdialog_drivercancelintime();

				}
			}
			if (driverpicked == 1) {
				// /////////////////////////////// CONFIRM PICK UP
				// ///////////////////////////////////////////

				handlerboolean = false;
				handler.removeCallbacks(r);
				AlertDialog.Builder alert = new AlertDialog.Builder(
						OnrouteScreen.this);
				final Drawable thumbsup = getResources().getDrawable(
						R.drawable.thumbsup);
				final Drawable thumbsupwhite = getResources().getDrawable(
						R.drawable.thumbsupwhite);
				final Drawable thumbsdown = getResources().getDrawable(
						R.drawable.thumbsdown);
				final Drawable thumbsdownwhite = getResources().getDrawable(
						R.drawable.thumbsdownwhite);
				LinearLayout layout = new LinearLayout(OnrouteScreen.this);
				layout.setOrientation(1);
				layout.setGravity(17);

				TextView tx1 = new TextView(OnrouteScreen.this);
				tx1.setText("Driver says you have been picked up");
				tx1.setGravity(17);
				tx1.setTextSize(20);
				tx1.setTextColor(Color.WHITE);
				tx1.setPadding(10, 10, 10, 10);
				
				
				TextView tx2 = new TextView(OnrouteScreen.this);
				tx2.setText("Please rate your driver");
				tx2.setGravity(17);
				tx2.setTextSize(16);

				LinearLayout imglayout = new LinearLayout(OnrouteScreen.this);
				imglayout.setOrientation(0);
				imglayout.setGravity(17);

				final ImageView ivup = new ImageView(OnrouteScreen.this);
				ivup.setImageDrawable(thumbsupwhite);
				ivup.setClickable(true);
				ivup.setPadding(0, 5, 30, 5);
				final ImageView ivdown = new ImageView(OnrouteScreen.this);
				ivdown.setImageDrawable(thumbsdownwhite);
				ivdown.setClickable(true);
				ivup.setPadding(30, 5, 0, 5);
				imglayout.addView(ivup);
				imglayout.addView(ivdown);

				layout.addView(tx1);
				layout.addView(tx2);

				layout.addView(imglayout);
				// /////////////////////////////// CONFIRM PICK UP - RATINGS
				// ///////////////////////////////////////////

				ivup.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (thumbsupboolean == false) {
							thumbsupboolean = true;
							thumbsdownboolean = false;
							ivup.setImageDrawable(thumbsup);
							ivdown.setImageDrawable(thumbsdownwhite);
							rating = 1;
						} else {
							thumbsupboolean = false;
							ivup.setImageDrawable(thumbsupwhite);
							rating = 0;
						}

					}
				});

				ivdown.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (thumbsdownboolean == false) {
							thumbsdownboolean = true;
							thumbsupboolean = false;
							ivdown.setImageDrawable(thumbsdown);
							ivup.setImageDrawable(thumbsupwhite);

							AlertDialog alert = new AlertDialog.Builder(
									OnrouteScreen.this).create();

							alert.setMessage("Please pick one");
							alert.setButton("No show",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											rating = -1;
										}
									});
							alert.setButton2("Driver late",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											rating = -2;
										}
									});
							alert.setButton3("Poor service",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											rating = -3;
										}
									});
							alert.show();

						} else {
							thumbsupboolean = false;
							ivdown.setImageDrawable(thumbsdownwhite);
							rating = 0;
						}

					}
				});

				button_completed_finish(alert);

				alert.setView(layout);
				alert.create();
				alert.show();
			} else {
			}

		} catch (JSONException e) {
		}
	}

	public void getDriverPosition(String driver_id) {
		int[][] driverPosition = Query.getDriverPosition(driver_id);

		GeoPoint dpos = new GeoPoint(driverPosition[0][1], driverPosition[0][2]);
		OverlayItem doverlayItem = new OverlayItem(dpos, "ME", "2nd String");
		CustomPinpoint dcustom = new CustomPinpoint(taxipng, OnrouteScreen.this);
		dcustom.insertPinpoint(doverlayItem);
		overlayList.add(dcustom);

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
	
	
	//////////////////////////////////////////////////////////////////////////
	//////////////////////// OVERLAYS ////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
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

	
	/////////////// BUTTONS AND DIALOGS////////////////////////
	public void button_cancelJob(AlertDialog dcancelbuilder) {
		dcancelbuilder.setButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String check = Query.jobQuery("clientcancel", job_id, 0,
						driver_id);
				if (check.equals("done")) {
					Intent openStart = new Intent(
							"com.TakeTaxi.jy.TakeTaxiActivity");
					startActivity(openStart);
				} else {
					Toast.makeText(getBaseContext(),
							"Could not connect to server.\nPlease try again.",
							Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	public void button_cancelJob_noquery(AlertDialog dcancelbuilder) {
		dcancelbuilder.setButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				handlerboolean = false;
				handler.removeCallbacks(r);
				Intent openStart = new Intent(
						"com.TakeTaxi.jy.TakeTaxiActivity");
				startActivity(openStart);

			}
		});
	}

	public void alertdialog_canceljob() {

		String check = Query.jobQuery("clientcancel", job_id, 0, driver_id);
		if (check.equals("done")) {
			AlertDialog dcancelbuilder = new AlertDialog.Builder(
					OnrouteScreen.this).create();
			dcancelbuilder.setMessage("Job has been cancelled.\n");
			dcancelbuilder.setButton("Ok",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							Intent openStart = new Intent(
									"com.TakeTaxi.jy.TakeTaxiActivity");
							startActivity(openStart);

						}
					});
			dcancelbuilder.show();
		} else {
			Toast.makeText(getBaseContext(),
					"Could not connect to server.\nPlease try again.",
					Toast.LENGTH_SHORT).show();
		}

	}

	public void button_cancelJob_driverNoShow(AlertDialog dcancelbuilder) {
		dcancelbuilder.setButton2("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String check = Query.jobQuery("drivernoshow", job_id, 0, driver_id);

				if (check.equals("done")) {
					Intent openStart = new Intent(
							"com.TakeTaxi.jy.TakeTaxiActivity");
					startActivity(openStart);
				} else {
					Toast.makeText(getBaseContext(),
							"Could not connect to server.\nPlease try again.",
							Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	public void button_drivercancel_reportlate(AlertDialog dcancelbuilder) {
		dcancelbuilder.setButton2("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String check = Query.jobQuery("drivercancellate", job_id, 0, driver_id);
				
				if (check.equals("done")) {
					Intent openStart = new Intent(
							"com.TakeTaxi.jy.TakeTaxiActivity");
					startActivity(openStart);
				} else {
					Toast.makeText(getBaseContext(),
							"Could not connect to server.\nPlease try again.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void alertdialog_drivercancelintime() {
		AlertDialog dcancelbuilder = new AlertDialog.Builder(OnrouteScreen.this)
				.create();
		dcancelbuilder
				.setMessage("Job has been cancelled.\nPlease look for another driver.");
		dcancelbuilder.setButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Intent openStart = new Intent(
						"com.TakeTaxi.jy.TakeTaxiActivity");
				startActivity(openStart);

			}
		});
		dcancelbuilder.show();
	}

	public void button_completed_finish(AlertDialog.Builder alert) {
		alert.setNegativeButton("Finished!",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						String check = Query.jobQuery("completed", job_id, rating, driver_id);
						
						if (check.equals("done")) {
							Intent openStart = new Intent(
									"com.TakeTaxi.jy.TakeTaxiActivity");
							startActivity(openStart);
						} else {
							Toast.makeText(getBaseContext(),
									"Could not connect to server.\nPlease try again.",
									Toast.LENGTH_SHORT).show();
						}
						

					}
				});

	}
//////////// END OF BUTTON AND DIALOGS ///////////////////////
}