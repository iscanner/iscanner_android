package com.github.iscanner.iscanner_android;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SwipeableCellAdapter extends BaseAdapter {
	private static final String TAG = "iscanner";
	private Context mContext = null;
	private List<?> data;
	private int mRightWidth = 0;
	private onRightItemClickListener mListener = null;

	public SwipeableCellAdapter(Context ctx, List<?> data, int rightWidth) {
		mContext = ctx;
		this.data = data;
		mRightWidth = rightWidth;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Log.i(TAG, (String) this.data.get(position));
		holder = new ViewHolder();
		String regEx = "\\d+-\\d+-\\d+";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher((String) this.data.get(position));
		if (!matcher.matches()) {
			holder.needTouch = true;
		}
		if (!holder.needTouch) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_header,
					parent, false);
			holder.cell_content = (RelativeLayout) convertView
					.findViewById(R.id.cell_content);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			LinearLayout.LayoutParams linearLayout1 = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			holder.cell_content.setLayoutParams(linearLayout1);
			holder.content.setText((CharSequence) data.get(position));
			convertView.setTag(holder);
			return convertView;
		}
		convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_body,
				parent, false);
		holder.cell_content = (RelativeLayout) convertView
				.findViewById(R.id.cell_content);
		holder.item_right = (RelativeLayout) convertView
				.findViewById(R.id.item_right);
		holder.content = (TextView) convertView.findViewById(R.id.content);
		holder.create_button = (Button) convertView
				.findViewById(R.id.create_button);
		holder.copy_button = (TextView) convertView
				.findViewById(R.id.copy_button);
		convertView.setTag(holder);

		LinearLayout.LayoutParams linearLayout1 = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		holder.cell_content.setLayoutParams(linearLayout1);
		LinearLayout.LayoutParams linearLayout2 = new LayoutParams(mRightWidth,
				LayoutParams.MATCH_PARENT);
		holder.item_right.setLayoutParams(linearLayout2);
		holder.content.setText((CharSequence) data.get(position));
		holder.create_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.clickCreateButton(v, position);
				}
			}
		});
		holder.copy_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.clickCopyButton(v, position);
				}
			}
		});
		return convertView;
	}

	static class ViewHolder {
		RelativeLayout cell_content;
		RelativeLayout item_right;
		TextView content;
		Button create_button;
		TextView copy_button;
		Boolean needTouch = false;
	}

	public void setOnRightItemClickListener(onRightItemClickListener listener) {
		mListener = listener;
	}

	public interface onRightItemClickListener {
		void clickCreateButton(View v, int position);

		void clickCopyButton(View v, int position);
	}
}
