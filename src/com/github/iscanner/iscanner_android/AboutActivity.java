package com.github.iscanner.iscanner_android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class AboutActivity extends Activity implements OnClickListener {
	private static final String TAG = "iscanner";
	private Button leftButton;
	private TextView title;
	private TextView copyright;
	private Button updateButton;
	private static final String API = "http://iscanner.github.io/api/latest.json";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_about);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		initView();
		Log.i(TAG, "start loading...");
	}

	public String getVersion() {
		String version = "1.0";
		PackageManager manager;
		PackageInfo info = null;
		manager = this.getPackageManager();
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			version = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	public void initView() {
		leftButton = (Button) findViewById(R.id.button_first);
		leftButton.setOnClickListener(this);
		leftButton.setVisibility(View.VISIBLE);
		updateButton = (Button) findViewById(R.id.button_update);
		updateButton.setOnClickListener(this);
		title = (TextView) findViewById(R.id.title);
		title.setText("About");
		copyright = (TextView) findViewById(R.id.copyright);
		copyright.setText("iScanner v" + getVersion());
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_first:
			finish();
			break;
		case R.id.button_update:
			getRemoteDate();
			break;
		}
	}

	public void getRemoteDate() {
		Task task = new Task();
		task.execute(100);
	}

	class Task extends AsyncTask<Integer, Integer, String> {
		public Task() {
		}

		@Override
		protected String doInBackground(Integer... params) {
			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			HttpGet myget = new HttpGet(API);
			String lastVersionStr = null;
			try {
				HttpResponse response = client.execute(myget);
				int statusCode = response.getStatusLine().getStatusCode();
				Log.i(TAG, "status code: " + statusCode);
				if (statusCode == 200) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					for (String s = reader.readLine(); s != null; s = reader
							.readLine()) {
						builder.append(s);
					}
					JSONObject res = JSON.parseObject(builder.toString());
					JSONObject android = (JSONObject) res.get("android");
					lastVersionStr = (String) android.get("version");
				} else {
					Log.i(TAG, "failed");
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return lastVersionStr;
		}

		@Override
		protected void onPostExecute(String lastVersionStr) {
			Version currentVersion = new Version(getVersion());
			Version lastVersion = new Version(lastVersionStr);
			if (currentVersion.compareTo(lastVersion) == -1) {
				new AlertDialog.Builder(AboutActivity.this)
						.setMessage(
								"update iscanner to version " + lastVersionStr)
						.setIcon(R.drawable.ic_launcher)
						.setPositiveButton("update",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent viewIntent = new Intent(
												"android.intent.action.VIEW",
												Uri.parse("http://iscanner.github.io"));
										startActivity(viewIntent);
									}
								})
						.setNegativeButton("next time",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).create().show();
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"iscanner is up to date", Toast.LENGTH_SHORT / 10);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

class Version implements Comparable<Version> {

	private String version;

	public final String get() {
		return this.version;
	}

	public Version(String version) {
		if (version == null)
			throw new IllegalArgumentException("Version can not be null");
		if (!version.matches("[0-9]+(\\.[0-9]+)*"))
			throw new IllegalArgumentException("Invalid version format");
		this.version = version;
	}

	@Override
	public int compareTo(Version that) {
		if (that == null)
			return 1;
		String[] thisParts = this.get().split("\\.");
		String[] thatParts = that.get().split("\\.");
		int length = Math.max(thisParts.length, thatParts.length);
		for (int i = 0; i < length; i++) {
			int thisPart = i < thisParts.length ? Integer
					.parseInt(thisParts[i]) : 0;
			int thatPart = i < thatParts.length ? Integer
					.parseInt(thatParts[i]) : 0;
			if (thisPart < thatPart)
				return -1;
			if (thisPart > thatPart)
				return 1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		if (that == null)
			return false;
		if (this.getClass() != that.getClass())
			return false;
		return this.compareTo((Version) that) == 0;
	}

}
