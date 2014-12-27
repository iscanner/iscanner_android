package com.github.iscanner.iscanner_android;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener {
	private static final String TAG = "iscanner";
	private Button leftButton;
	private TextView title;
	private TextView copyright;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_about);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		initView();
		Log.i(TAG, "start loading...");
	}

	public void initView() {
		leftButton = (Button) findViewById(R.id.button_first);
		leftButton.setOnClickListener(this);
		leftButton.setVisibility(View.VISIBLE);
		title = (TextView) findViewById(R.id.title);
		title.setText("About");
		copyright = (TextView) findViewById(R.id.copyright);
		copyright.setTextColor(Color.rgb(180, 180, 180));
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_first:
			finish();
			break;
		case R.id.button_second:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
