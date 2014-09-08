package com.TaxiDriver.jy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class Tutorial extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tutorial);
		LinearLayout ll = (LinearLayout) findViewById(R.id.registerdone);

		ll.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {
				Intent openStart = new Intent(
						"com.TaxiDriver.jy.TaxiDriverActivity");
				startActivity(openStart);
				return false;
			}
			
		
				
	});
		
		
		
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		return;
	}

}
