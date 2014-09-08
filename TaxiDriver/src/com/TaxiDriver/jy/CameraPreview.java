package com.TaxiDriver.jy;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

// ----------------------------------------------------------------------

public class CameraPreview extends Activity {
	private Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;
	Button buttonClick, buttonRetry, buttonSend;
	ImageView takenPicture;
	SharedPreferences sharedPref;
	public static String prefFile = "prefFile";
	public static String nameString = "nameString";
	public static String numString = "numString";
	public static String NRICString = "NRICString";
	public static String licenseString = "licenseString";
	public static String companyString = "companyString";
	public static String typeString = "typeString";
	public static String driverString = "driverString";
	Bitmap bmp;

	ProgressDialog pDialog;

	// The first rear facing camera
	int defaultCameraId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera);

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		mPreview = new Preview(this);
		((FrameLayout) findViewById(R.id.preview)).addView(mPreview);
		buttonRetry = (Button) findViewById(R.id.buttonRetry);
		buttonSend = (Button) findViewById(R.id.buttonSend);
		
		
		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new OnClickListener() {
			

			public void onClick(View v) {
				mCamera.takePicture(null, null, jpegCallback);

			}
		});

		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
	}

	PictureCallback jpegCallback = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFileDir = getDir();
			if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
				Toast.makeText(getBaseContext(),
						"Can't create directory to save image.",
						Toast.LENGTH_LONG).show();
				return;
			}

			bmp = BitmapFactory.decodeByteArray(data, 0,
					data.length);
			showTakenPicture(bmp);

			buttonRetry.setVisibility(0);
			buttonSend.setVisibility(0);
			buttonClick.setVisibility(8);

			buttonSend.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					// TODO Auto-generated method stub

					new SendDriverInfo().execute();
					Intent openStart = new Intent(
							"com.TaxiDriver.jy.Tutorial");
					startActivity(openStart);

					
				}

			});
			
			buttonRetry.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = getIntent();
					finish();
					startActivity(intent);

				}

			});

		}

		private File getDir() {
			File sdDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			return new File(sdDir, "CameraAPIDemo");
		}
	};

	private class SendDriverInfo extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			/*pDialog = new ProgressDialog(getBaseContext());

			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.setIndeterminate(true);
			pDialog.show();
*/
		}

		@Override
		protected String doInBackground(String... string) {
			
			sharedPref = getSharedPreferences(prefFile, 0);

			String driver_id = DriverQuery.driverPrefQuery(
					sharedPref.getString(nameString, ""),
					sharedPref.getString(numString, ""),
					sharedPref.getString(NRICString, ""),
					sharedPref.getString(licenseString, ""),
					sharedPref.getInt(companyString, 0),
					sharedPref.getInt(typeString, 0), "new");
			String msg = sendPicture(bmp, driver_id);
			return driver_id;

		}

		@Override
		protected void onPostExecute(String result) {
			SharedPreferences.Editor editor = sharedPref.edit();

			editor.putString(driverString, result);
			editor.commit();
			//pDialog.dismiss();
		}
	}

	public void showTakenPicture(Bitmap bmp) {
		takenPicture = new ImageView(this);
		takenPicture.setImageBitmap(bmp);
		((FrameLayout) findViewById(R.id.preview)).removeView(mPreview);
		((FrameLayout) findViewById(R.id.preview)).addView(takenPicture);
	}

	public String sendPicture(Bitmap bmp, String driver_id) {

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 70, bao);
		byte[] ba = bao.toByteArray();
		String ba1 = Base64.encodeBytes(ba);
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("image", ba1));
		nameValuePairs.add(new BasicNameValuePair("driver_id", driver_id));
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(HttpHelper.domain
					+ "uploadtdvl.php");
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// is = entity.getContent();

			String check = HttpHelper.request(response);

			return check;

		} catch (Exception e) {
			Log.e("log_tag", "Error in http connection " + e.toString());
			return "null";

		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * 
	 * // Inflate our menu which can gather user input for switching camera //
	 * MenuInflater inflater = getMenuInflater(); //
	 * inflater.inflate(R.menu.camera_menu, menu); return true; }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { // Handle
	 * item selection switch (item.getItemId()) { case R.id.switch_cam: // check
	 * for availability of multiple cameras if (numberOfCameras == 1) {
	 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
	 * builder.setMessage(this.getString(R.string.camera_alert))
	 * .setNeutralButton("Close", null); AlertDialog alert = builder.create();
	 * alert.show(); return true; }
	 * 
	 * // OK, we have multiple cameras. // Release this camera ->
	 * cameraCurrentlyLocked if (mCamera != null) { mCamera.stopPreview();
	 * mPreview.setCamera(null); mCamera.release(); mCamera = null; }
	 * 
	 * // Acquire the next camera and request Preview to reconfigure //
	 * parameters. mCamera = Camera .open((cameraCurrentlyLocked + 1) %
	 * numberOfCameras); cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) %
	 * numberOfCameras; mPreview.switchCamera(mCamera);
	 * 
	 * // Start the preview mCamera.startPreview(); return true; default: return
	 * super.onOptionsItemSelected(item); } }
	 */
}

// ----------------------------------------------------------------------

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
class Preview extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "Preview";

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;
	Button snap;

	Preview(Context context) {
		super(context);

		mSurfaceView = new SurfaceView(context);
		snap = new Button(context);
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;

		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters()
					.getSupportedPreviewSizes();
			requestLayout();
		}
	}

	public void switchCamera(Camera camera) {
		setCamera(camera);
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();

		camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		final int width = resolveSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
					height);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height
						/ previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
						(width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width
						/ previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2, width,
						(height + scaledChildHeight) / 2);
			}
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {

				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	protected void setDisplayOrientation(Camera camera, int angle) {
		Method downPolymorphic;
		try {
			downPolymorphic = camera.getClass().getMethod(
					"setDisplayOrientation", new Class[] { int.class });
			if (downPolymorphic != null)
				downPolymorphic.invoke(camera, new Object[] { angle });
		} catch (Exception e1) {
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

}