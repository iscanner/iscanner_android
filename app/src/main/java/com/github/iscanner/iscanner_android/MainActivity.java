package com.github.iscanner.iscanner_android;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity implements Callback, OnClickListener {
	private static final String TAG = "iscanner";
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private Button rightButton;
	private TextView copyright;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
		Log.i(TAG, "start loading...");
	}

	public void initView() {
		rightButton = (Button) findViewById(R.id.button_second);
		rightButton.setOnClickListener(this);
		rightButton.setVisibility(View.VISIBLE);
		copyright = (TextView) findViewById(R.id.copyright);
		copyright.setOnClickListener(this);
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	private void continuePreview() {
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		initCamera(surfaceHolder);
		if (handler != null) {
			handler.restartPreviewAndDecode();
		}
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		final String resultString = result.getText();
		final String pureResultString = resultString.trim();
		final String barcodeFormat = result.getBarcodeFormat().toString();
		if (pureResultString.equals("")) {
			Toast.makeText(MainActivity.this, "Scan failed!",
					Toast.LENGTH_SHORT).show();
		} else {
			Dialog alertDialog = new AlertDialog.Builder(this)
					.setTitle(barcodeFormat)
					.setMessage(pureResultString)
					.setIcon(R.drawable.ic_launcher)
					.setPositiveButton("ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									setSharedPreferences("list", pureResultString);
									Intent viewIntent = new Intent(
											"android.intent.action.VIEW", Uri
													.parse(pureResultString));
									startActivity(viewIntent);
									continuePreview();
								}
							})
					.setNegativeButton("continue",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									continuePreview();
								}
							}).create();
			alertDialog.show();
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);
			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_second:
			Intent intentHistory = new Intent(this, HistoryActivity.class);
			startActivity(intentHistory);
			break;
		case R.id.copyright:
			Intent intentAbout = new Intent(this, AboutActivity.class);
			startActivity(intentAbout);
			break;
		}
	}

	public void clearSharedPreferences() {
		SharedPreferences settings = this.getSharedPreferences(
				"localstoregeXML", 0);
		SharedPreferences.Editor localEditor = settings.edit();
		localEditor.putString("list", "");
		localEditor.commit();
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("CommitPrefEdits")
	public void setSharedPreferences(String localStorageKey, String resultString) {
		Date date = new Date();
		SimpleDateFormat dateFormtter = new SimpleDateFormat("yyyy-MM-dd");
		SharedPreferences settings = this.getSharedPreferences(
				"localstoregeXML", 0);
		SharedPreferences.Editor localEditor = settings.edit();
		String dateString = dateFormtter.format(date);
		String list = settings.getString(localStorageKey, "");
		if (list == "") {
			Log.i(TAG, "list is empty.");
			List<Map<String, Object>> parentList = new ArrayList<Map<String, Object>>();
			List<String> childrenList = new ArrayList<String>();
			childrenList.add(resultString);
			Map<String, Object> dictionary = new HashMap<String, Object>();
			dictionary.put(dateString, childrenList);
			parentList.add(dictionary);
			String parentListStr = JSON.toJSONString(parentList);
			Log.i(TAG, "create list " + parentListStr);
			localEditor.putString(localStorageKey, parentListStr);
			localEditor.commit();
		} else {
			Log.i(TAG, "list data is" + list);
			@SuppressWarnings("rawtypes")
			List parentList = JSON.parseArray(list);
			Map<String, List<String>> dictionary = (Map<String, List<String>>) parentList
					.get(parentList.size() - 1);
			Set<String> keys = dictionary.keySet();
			String tempKey = keys.toArray()[0].toString();

			if (tempKey.equals(dateString)) {
				Log.i(TAG, "old key " + tempKey);
				List<String> childrenList = dictionary.get(tempKey);
				childrenList.add(resultString);
				String parentListStr = JSON.toJSONString(parentList);
				Log.i(TAG, "update list " + parentListStr);
				localEditor.putString(localStorageKey, parentListStr);
				localEditor.commit();
			} else {
				Log.i(TAG, "new key " + tempKey);
				if (parentList.size() == 3) {
					parentList.remove(0);
				}
				List<String> childrenList = new ArrayList<String>();
				childrenList.add(resultString);
				Map<String, Object> newDictionary = new HashMap<String, Object>();
				newDictionary.put(dateString, childrenList);
				parentList.add(newDictionary);
				String parentListStr = JSON.toJSONString(parentList);
				Log.i(TAG, "new update list " + parentListStr);
				localEditor.putString(localStorageKey, parentListStr);
				localEditor.commit();
			}
		}
	}

}
