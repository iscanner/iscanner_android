package com.github.iscanner.iscanner_android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.zxing.WriterException;
import com.zxing.encoding.EncodingHandler;

public class HistoryActivity extends Activity implements OnClickListener {
	private static final String TAG = "iscanner";
	private TextView title;
	private Button leftButton;
	private List<?> parentList;
	private List<String> data;
	private SwipeableCell listView;
	private ImageView imgImageView;
	private RelativeLayout qrDialogView;
	private Animation animationAlpha;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_history);
		getSharedPreferences("list");
		initView();
		Log.i(TAG, "start loading...");
	}

	public void getSharedPreferences(String locastorageKey) {
		SharedPreferences settings = this.getSharedPreferences(
				"localstoregeXML", 0);
		String list = settings.getString(locastorageKey, "");
		Log.i(TAG, list);
		if (list != "") {
			Log.i(TAG, list);
			parentList = JSON.parseArray(list);
			Collections.reverse(parentList);
			initTabel();
		}
	}

	public void initView() {
		title = (TextView) findViewById(R.id.title);
		title.setText("History");
		leftButton = (Button) findViewById(R.id.button_first);
		leftButton.setOnClickListener(this);
		leftButton.setVisibility(View.VISIBLE);
	}

	public void setFullscreen() {

	}

	public void initTabel() {
		listView = (SwipeableCell) findViewById(R.id.historyListView);
		qrDialogView = (RelativeLayout) findViewById(R.id.qr_dialog);
		qrDialogView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				qrDialogView.setVisibility(View.GONE);
				animationAlpha = new AlphaAnimation(1.0f, 0.0f);
				animationAlpha.setStartOffset(200);
				animationAlpha.setDuration(800);
				animationAlpha.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						qrDialogView.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationStart(Animation animation) {
					}
				});
				imgImageView.setAnimation(animationAlpha);
			}
		});
		imgImageView = (ImageView) findViewById(R.id.qr_image_view);
		SwipeableCellAdapter mAdapter = new SwipeableCellAdapter(this,
				getData(), 596);
		mAdapter.setOnRightItemClickListener(new SwipeableCellAdapter.onRightItemClickListener() {
			@Override
			public void clickCreateButton(View view, int position) {
				setFullscreen();
				try {
					String current = (String) data.get(position);
					DisplayMetrics dm = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(dm);
					int screenWidth = dm.widthPixels;
					if (!current.equals("")) {
						Bitmap qrCodeBitmap = EncodingHandler.createQRCode(
								current, screenWidth);
						imgImageView.setImageBitmap(qrCodeBitmap);
					}
				} catch (WriterException e) {
					e.printStackTrace();
				}
				qrDialogView.setVisibility(View.VISIBLE);
				animationAlpha = new AlphaAnimation(0.0f, 1.0f);
				animationAlpha.setDuration(800);
				imgImageView.setAnimation(animationAlpha);
			}

			@SuppressWarnings("deprecation")
			@Override
			public void clickCopyButton(View view, int position) {
				ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				String current = (String) data.get(position);
				c.setText(current);
				Toast toast = Toast.makeText(getApplicationContext(),
						"Copy to clipboard", Toast.LENGTH_SHORT / 10);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		});
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String current = (String) data.get(arg2);
				String regEx = "\\d+-\\d+-\\d+";
				Pattern pattern = Pattern.compile(regEx);
				Matcher matcher = pattern.matcher(current);
				if (!matcher.matches()) {
					Intent viewIntent = new Intent(
							"android.intent.action.VIEW", Uri.parse(current));
					startActivity(viewIntent);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private List<String> getData() {
		data = new ArrayList<String>();
		for (int i = 0; i < parentList.size(); i++) {
			Map<String, Object> current = (Map<String, Object>) parentList
					.get(i);
			Set<String> keys = current.keySet();
			String tempKey = keys.toArray()[0].toString();
			Log.i(TAG, tempKey);
			data.add(tempKey);
			List<String> list = (List<String>) current.get(tempKey);
			Collections.reverse(list);
			for (int j = 0; j < list.size(); j++) {
				data.add(list.get(j));
			}
		}
		return data;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_first:
			finish();
			break;
		case R.id.button_second:
			break;
		default:
			Log.i(TAG, "" + view.getId());
			break;
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
