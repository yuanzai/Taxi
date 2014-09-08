package com.TakeTaxi.jy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Prefs extends Activity {

	EditText sharedData1, sharedData2;
	TextView dataResults, tvPrefHeader;
	public static String prefFile = "MysharedString";
	public static String nameString = "nameString";
	public static String numString = "numString";
	// public static String filename2 = "MysharedString2";
	SharedPreferences sharedPref;
	Button mainmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.prefs);

		setupVariables();
		tvPrefHeader = (TextView) findViewById(R.id.tvPrefHeader);
		sharedPref = getSharedPreferences(prefFile, 0);

		if (sharedPref.getString(nameString, "") == null
				|| sharedPref.getString(numString, "") == null
				|| sharedPref.getString(nameString, "").equals("")
				|| sharedPref.getString(numString, "").equals("")) {
			tvPrefHeader
					.setText("Hello.\n\nThanks for using Tap-a-Taxi.\nPlease enter your particulars below to get started!");
		} else {

			tvPrefHeader.setText("Your profile");
		}

		sharedData1.setText(sharedPref.getString(nameString, ""));

		sharedData2.setText(sharedPref.getString(numString, ""));

		mainmap = (Button) findViewById(R.id.bMainmap);
		mainmap.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (sharedData1.getText().toString().equals("")
						|| sharedData2.getText().toString().equals("")) {
					Toast.makeText(getBaseContext(),
							"Please fill in your particulars",
							Toast.LENGTH_SHORT).show();

				} else {
					Intent openStart = new Intent(
							"com.TakeTaxi.jy.TakeTaxiActivity");
					startActivity(openStart);
				}
			}
		});
	}

	private void setupVariables() {
		sharedData1 = (EditText) findViewById(R.id.etName);
		sharedData2 = (EditText) findViewById(R.id.etNumber);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		String stringData1 = sharedData1.getText().toString();
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(nameString, stringData1);

		String stringData2 = sharedData2.getText().toString();
		editor.putString(numString, stringData2);

		editor.commit();

	}

}
