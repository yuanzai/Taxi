package com.TaxiDriver.jy;


import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;

public class OutsideAlert extends Activity {
Button bOpenApp, bPutUnavail;
GlobalVar gv;

// Sounds//
private SoundPool soundPool;
private int soundID;
boolean loaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.outsidealert);
		
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight()/2;
		getWindow().setLayout(LayoutParams.FILL_PARENT,height);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().setTitle("Tap-a-Taxi");
		
		
		// Sound //
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// Load the sound
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			public void onLoadComplete(SoundPool soundPool, int sampleId,int status) {
				loaded = true;
			}
		});
		
		soundID = soundPool.load(this, R.raw.alert, 1);
		
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		float actualVolume = (float) audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		final float volume = actualVolume / maxVolume;
		// Is the sound loaded already?
		
		
		Handler sHandler = new Handler();
		Runnable sRun = new Runnable() {
			public void run() {
				soundPool.play(soundID, volume, volume, 1, 2, 1f);


			}
		};

	
			sHandler.postDelayed(sRun, 3000);
		


		
		handler.post(run);
		
		
		
		gv = (GlobalVar) this.getApplication();
		gv.isOAActive(true);
		bOpenApp = (Button) findViewById(R.id.bOpenApp);
		bPutUnavail = (Button) findViewById(R.id.bPutUnavail);
		
		bOpenApp.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent openStart = new Intent(
						"com.TaxiDriver.jy.TaxiDriverActivity");
				startActivity(openStart);
				handler.removeCallbacks(run);

				finish();

			}
		});
		
		bPutUnavail.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
			gv.driverAvail("0");
			Intent updateBroadcast = new Intent("com.TaxiDriver.jy.DO_UPDATE");
			sendBroadcast(updateBroadcast);
			handler.removeCallbacks(run);

			finish();
			}
		});	

	}
	
	final Handler handler = new Handler();
	final Runnable run = new Runnable() {
		public void run() {
			if (gv.jobList() == null){
				handler.removeCallbacks(run);
				finish();
			}
				handler.postDelayed(this, 10000);
			
			}
	};
	
	
	
	
	@Override
	public void onBackPressed() {
		bPutUnavail.performClick();
	}

}
