package com.TakeTaxi.jy;

import java.util.Calendar;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class SubmitJobScreen extends Activity {
	PopupWindow pw;
	Boolean handlerboolean = false;
	int tempLat, tempLongi;
	String driver_id, job_id, geoadd;
	AlertDialog.Builder alert;
	CountDownTimer timer;
	EditText etDestination, etPickup;
	Button call;

	// Sounds//
	private SoundPool soundPool;
	private int soundID;
	boolean loaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();

		driver_id = extras.getString("driver_id");
		tempLat = extras.getInt("tempLat");
		tempLongi = extras.getInt("tempLongi");
		geoadd = extras.getString("geoadd");
		setContentView(R.layout.submitjob);

		// Load Sound
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundID = soundPool.load(this, R.raw.alert, 1);

		TextView tvAddress = (TextView) findViewById(R.id.tvAddress);
		tvAddress.setText(geoadd);

		etPickup = (EditText) findViewById(R.id.etPickup);
		etDestination = (EditText) findViewById(R.id.etDestination);
		((LastAddress) this.getApplication()).lastAddtime(Calendar.getInstance().get(Calendar.SECOND) + 600);

		// //////////////////// Saved Address from last
		// input--------------------------------

		try {
			String lastPickup = ((LastAddress) this.getApplication()).lastPickup();
			String lastDesti = ((LastAddress) this.getApplication()).lastDesti();

			if (((LastAddress) this.getApplication()).lastAddtime() >= Calendar.getInstance().get(Calendar.SECOND)) {
				etPickup.setText(lastPickup);
				etDestination.setText(lastDesti);

			}

		} catch (Exception e) {

		}
		// //////////////////////////// Call
		// Button-------------------------------------------

		call = (Button) findViewById(R.id.bOK);
		call.setEnabled(true);
		call.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				call.setEnabled(false);

				int driverAvailability;
				// check driver availability //
				driverAvailability = 0;

				if (driver_id.equals("0")) {
					driverAvailability = 1;

				} else {

					driverAvailability = 0;

					String getAvailability = Query.getDriverDetail("avail", driver_id);
					if (getAvailability == null) {
						nullResponseAlert(call);
					} else {
						driverAvailability = Integer.valueOf(getAvailability);
					}

				}

				if (etDestination.getText().toString().equals("")) {
					Toast toast = Toast.makeText(SubmitJobScreen.this, "Please enter a destination", Toast.LENGTH_SHORT);
					toast.show();
					call.setEnabled(true);

				} else
				// ////////////////////// Driver Unavailable///////////////////////////////
				if (driverAvailability == 0) {
					driverUnavailableAction();

				} else {

					// //////////////////////Driver Available///////////////////////////////

				    final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

				    final String tmDevice, tmSerial, tmPhone, androidId;
				    tmDevice = "" + tm.getDeviceId();
				    tmSerial = "" + tm.getSimSerialNumber();
				    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

				    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
				    String deviceId = deviceUuid.toString();
				    
				    /// temp device id//
				    //deviceId = "tempdevice id";
					
					// Post Job to database //

					job_id = Query.clientSubmitJob(driver_id, getSharedPreferences("MysharedString", 0).getString("nameString", ""),
							getSharedPreferences("MysharedString", 0).getString("numString", ""), String.valueOf(tempLat), String.valueOf(tempLongi), etPickup
									.getText().toString(), etDestination.getText().toString(), deviceId);
					// Check posting sucess //

					if (job_id == null) {
						call.setEnabled(true);
						nullResponseAlert(call);

					} else {

						// Upon posting success //
						alert = new AlertDialog.Builder(SubmitJobScreen.this);

						LinearLayout layout2 = new LinearLayout(SubmitJobScreen.this);
						layout2.setOrientation(1);
						layout2.setGravity(17);

						final TextView tx = new TextView(SubmitJobScreen.this);
						tx.setGravity(17);

						timer = new CountDownTimer(60000, 1000) {

							public void onTick(long millisUntilFinished) {
								tx.setText("Waiting for driver\nTime remaining: " + millisUntilFinished / 1000 + "s");
							}

							public void onFinish() {
								Query.jobQuery("drivercancel", job_id, 0, driver_id);
								tx.setText("Driver did not respond.");
								call.setEnabled(true);
							}
						}.start();

						layout2.addView(tx);

						// cancel job call //
						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								Query.jobQuery("clientcancel", job_id, 0, driver_id);
								timer.cancel();
								Intent openStart = new Intent("com.TakeTaxi.jy.TakeTaxiActivity");
								startActivity(openStart);
								handlerboolean = false;
							}
						});

						alert.setView(layout2);
						alert.create();
						alert.show();

						alert.setCancelable(false);

						// ping job acceptance by driver
						handlerboolean = true;
						handler.removeCallbacks(r);
						handler.post(r);
					}

				}

			}

		});
		// ////////////////////////////////////////////////////////////////////////////////////////////////

		// ////////////////////////////Cancel
		// Button-------------------------------------------
		cancelButtonReturntoMain();

	}

	@Override
	public void onBackPressed() {

		return;

	}

	// //////////////////////////////////////////////////////////////////////////////////////////////

	// ///////////////HANDLER-------------------------------------------
	Handler handler = new Handler();
	Runnable r = new Runnable() {
		public void run() {
			if (Query.isNetworkAvailable(getBaseContext())) {
				if (handlerboolean) {
					new CheckJobStatus().execute();
					handler.postDelayed(this, 5000);
				}
			} else {
				AlertDialog alert = new AlertDialog.Builder(SubmitJobScreen.this).create();
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

	private class CheckJobStatus extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... string) {
			JSONObject json = Query.getJobInfo(job_id);

			return json;

		}

		@Override
		protected void onPostExecute(JSONObject result) {
			pingForDriverAccept(result);

		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////

	// ////// Pinging to check if driver accepted job ///////////
	public void pingForDriverAccept(JSONObject json) {
		try {

			int accept = json.getInt("accepted");
			int dcancel = json.getInt("dcancel");
			// driver cancel, ie driver unavailable //
			if (dcancel == 1) {
				handlerboolean = false;

				driverUnavailableAction();
			}
			// driver accept //
			if (accept == 1) {
				// Sound //

				AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				final float volume = actualVolume / maxVolume;
				// play sound
				soundPool.play(soundID, volume, volume, 1, 0, 1f);

				handlerboolean = false;
				timer.cancel();
				Intent openStart = new Intent(getBaseContext(), OnrouteScreen.class);
				openStart.putExtra("job_id", job_id);
				openStart.putExtra("tempLat", tempLat);
				openStart.putExtra("tempLongi", tempLongi);
				startActivity(openStart);
			} else {
			}
		} catch (JSONException e) {
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(r);
		// /// registering last input address + pickup /////
		String lastPickup = etPickup.getText().toString();
		String lastDesti = etDestination.getText().toString();

		((LastAddress) this.getApplication()).lastDesti(lastDesti);
		((LastAddress) this.getApplication()).lastPickup(lastPickup);
	}

	public void nullResponseAlert(final Button call) {
		AlertDialog alert = new AlertDialog.Builder(SubmitJobScreen.this).create();
		alert.setMessage("No network connection available. Please try again.");
		alert.setButton("Retry", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				call.performClick();
			}
		});
		alert.setButton2("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alert.show();

	}

	public void driverUnavailableAction() {
		AlertDialog builder = new AlertDialog.Builder(SubmitJobScreen.this).create();

		builder.setMessage("Driver is unavailable.\nPlease look for another driver.");
		builder.setButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Intent openStart = new Intent("com.TakeTaxi.jy.TakeTaxiActivity");
				startActivity(openStart);

			}
		});
		builder.show();

	}

	public void cancelButtonReturntoMain() {
		Button cancelcall = (Button) findViewById(R.id.bCancelCall);
		cancelcall.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				Intent openStart = new Intent("com.TakeTaxi.jy.TakeTaxiActivity");
				startActivity(openStart);
			}
		});
	}
}
