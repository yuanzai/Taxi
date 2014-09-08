package com.TaxiDriver.jy;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DriverInfo extends Activity {
	Spinner spinCompany, spinType;
	EditText etName, etNumber, etNRIC, etLicense;
	Button bPrefOK;
	TextView tvProfile, tvPlan;
	CheckBox cbTnC;

	public static String prefFile = "prefFile";
	public static String nameString = "nameString";
	public static String numString = "numString";
	public static String NRICString = "NRICString";
	public static String licenseString = "licenseString";
	public static String companyString = "companyString";
	public static String typeString = "typeString";
	public static String driverString = "driverString";
	SharedPreferences sharedPref;
	boolean isNewUser = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.driverinfo);
		Log.d("TDA", "DI GV " + ((GlobalVar) this.getApplication()).toString());
		setupViews();
		sharedPref = getSharedPreferences(prefFile, 0);
		etName.setText(sharedPref.getString(nameString, ""));
		etNumber.setText(sharedPref.getString(numString, ""));
		etNRIC.setText(sharedPref.getString(NRICString, ""));
		etLicense.setText(sharedPref.getString(licenseString, ""));
		tvProfile = (TextView) findViewById(R.id.tvProfile);
		cbTnC = (CheckBox) findViewById(R.id.cbTnC);

		if (sharedPref.getString(driverString, "").equals("")) {
			isNewUser = true;
		}

		if (isNewUser) {
			bPrefOK.setText("Register");
			tvProfile.setText("Register your profile");
			cbTnC.setVisibility(1); // //// check vis int setting

		} else {
			bPrefOK.setText("Done!");
			tvProfile.setText("Change your profile");
			tvPlan = (TextView) findViewById(R.id.tvPlan);
			tvPlan.setVisibility(1);
			tvPlan.setText("Free"); // // for now
		}

		List<String> companyArray = DriverQuery.getPrefList("company");
		ArrayAdapter<String> companyspinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, companyArray);
		companyspinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The
																											// drop
																											// down
																											// vieww
		spinCompany.setAdapter(companyspinnerArrayAdapter);

		List<String> typeArray = DriverQuery.getPrefList("type");
		ArrayAdapter<String> typespinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeArray);
		typespinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The
																										// drop
																										// down
																										// vieww
		spinType.setAdapter(typespinnerArrayAdapter);

		spinCompany.setSelection(sharedPref.getInt(companyString, 0));
		spinType.setSelection(sharedPref.getInt(typeString, 0));

		bPrefOK.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String querytype;
				if (isNewUser) {
					querytype = "new";
				} else {
					querytype = "update";
				}
				String name = etName.getText().toString();
				String number = etNumber.getText().toString();
				String nric = etNRIC.getText().toString();
				String license = etLicense.getText().toString();
				int company = spinCompany.getSelectedItemPosition();
				int type = spinType.getSelectedItemPosition();

				SharedPreferences.Editor editor = sharedPref.edit();

				editor.putString(nameString, name);
				editor.putString(numString, number);
				editor.putString(NRICString, nric);
				editor.putString(licenseString, license);
				editor.putInt(companyString, company);
				editor.putInt(typeString, type);

				editor.commit();

				if (name.isEmpty() || nric.isEmpty() || number.isEmpty() || license.isEmpty() || company == 0 || type == 0) {
					Toast.makeText(getBaseContext(), "Please fill up all the fields before submitting", Toast.LENGTH_SHORT).show();

				} else {

					if (isNewUser) {

						if (cbTnC.isChecked()) {
							Intent openStart = new Intent("com.TaxiDriver.jy.CameraPreview");
							startActivity(openStart);
						} else {
							Toast.makeText(getBaseContext(), "Please confirm that you have read our Terms and Conditions", Toast.LENGTH_SHORT).show();

						}

					} else {
						DriverQuery.driverPrefQuery(name, number, nric, license, company, type, querytype);

						Intent openStart = new Intent("com.TaxiDriver.jy.TaxiDriverActivity");
						startActivity(openStart);
					}
				}

				/*
				 * } else { ////// Query No Update Error Catch ////// AlertDialog alert = new AlertDialog.Builder( DriverInfo.this).create(); alert.setMessage(
				 * "No network connection available. Please try again."); alert.setButton("Retry", new DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int which) { bPrefOK.performClick(); } }); alert.setButton2("Cancel", new DialogInterface.OnClickListener() {
				 * public void onClick(DialogInterface dialog, int which) { return; } }); alert.show();
				 * 
				 * }
				 */
			}
		});

		Button clearer = (Button) findViewById(R.id.bPrefClear);
		clearer.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(driverString, "");
				editor.commit();

				Intent openStart = new Intent("com.TaxiDriver.jy.TaxiDriverActivity");
				startActivity(openStart);
			}
		});

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	private void setupViews() {

		spinCompany = (Spinner) findViewById(R.id.spinCompany);
		spinType = (Spinner) findViewById(R.id.spinType);
		etName = (EditText) findViewById(R.id.etName);
		etNumber = (EditText) findViewById(R.id.etNumber);
		etNRIC = (EditText) findViewById(R.id.etNRIC);
		etLicense = (EditText) findViewById(R.id.etLicense);
		bPrefOK = (Button) findViewById(R.id.bPrefOK);
	}

}
