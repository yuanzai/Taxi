package com.TaxiDriver.jy;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class CameraDemo extends Activity {

	
	private static final String TAG = "CameraDemo";
	Camera camera;
	Preview preview;
	Button buttonClick;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		preview = new Preview(this);
		((FrameLayout) findViewById(R.id.preview)).addView(preview);
		/*
		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new OnClickListener() {
			
			 * public void onClick(View v) {
				preview.camera.takePicture(shutterCallback, rawCallback,
						jpegCallback);
				Toast.makeText(getBaseContext(), "PHOTO TAKEN",
						Toast.LENGTH_LONG).show();
				pop();

			}
			
		});
*/
		Log.d(TAG, "onCreate'd");
	}

	public void pop() {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final PopupWindow pw = new PopupWindow(inflater.inflate(
				R.layout.preview, null, false), 200, 200, true);

		final LinearLayout test = (LinearLayout) findViewById(R.id.layout);
		
		final ImageView ivpic = (ImageView) findViewById(R.id.ivpreview);
		
		pw.showAtLocation(test, Gravity.CENTER, 0, 0);

	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	/** Handles data for raw picture */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	/** Handles data for jpeg picture */
	String filename;
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				// write to local sandbox file system
				// outStream =
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
				// System.currentTimeMillis()), 0);
				// Or write to sdcard

				// filename =
				// Integer.toString(Calendar.getInstance().get(Calendar.SECOND))
				// + ".jpg";
				outStream = new FileOutputStream(String.format(
						"/sdcard/%d.jpg", System.currentTimeMillis()));
				outStream.write(data);
				outStream.close();

				/*
				 * Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
				 * data.length,null);
				 * 
				 * 
				 * 
				 * 
				 * AlertDialog.Builder d=new
				 * AlertDialog.Builder(CameraDemo.this);
				 * 
				 * LinearLayout imglayout = new LinearLayout(CameraDemo.this);
				 * imglayout.setOrientation(1); imglayout.setGravity(17);
				 * 
				 * ImageView ivPic = new ImageView(CameraDemo.this);
				 * ivPic.setImageBitmap(bitmap); imglayout.addView(ivPic);
				 * d.setView(imglayout); d.create(); d.show();
				 */

				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");

			// //////////////
			/*
			 * HttpClient httpclient = new DefaultHttpClient(); HttpPost
			 * httppost = new HttpPost(HttpHelper.domain + "uploadtdvl.php");
			 * 
			 * try { MultipartEntity entity = new MultipartEntity();
			 * 
			 * entity.addPart("file", new FileBody(imgFile));
			 * httppost.setEntity(entity); HttpResponse response =
			 * httpclient.execute(httppost); } catch (ClientProtocolException e)
			 * { } catch (IOException e) { }
			 */
			// ///////////////

		}
	};
	
	
	
}
