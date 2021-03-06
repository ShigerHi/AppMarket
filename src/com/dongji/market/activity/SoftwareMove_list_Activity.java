package com.dongji.market.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.adapter.SoftwareMoveAdapter;
import com.dongji.market.helper.AConstDefine;
import com.dongji.market.helper.FileLoadTask;
import com.dongji.market.pojo.InstalledAppInfo;
import com.dongji.market.widget.ScrollListView;

/**
 * 软件搬家页
 * 
 * @author yvon
 * 
 */
public class SoftwareMove_list_Activity extends Activity implements OnClickListener {

	// TODO 这里与云备份那边的标志相冲突。最好写在一起
	public static final int FLAG_SDCARD = 7;
	public static final int FLAG_PHONECARD = 8;

	private static final int EVENT_REQUEST_SOFTWARE_LIST = 0;

	private SoftwareMoveAdapter softwareMoveAdapter;
	private LinearLayout llBottomBtn;
	private RadioButton rbPhonecard, rbSdcard;
	private MyHandler mHandler;

	private ScrollListView mListView;
	private TextView tvNoAppTips;

	private FileLoadTask task;
	private View mLoadingView;
	private int flag = FLAG_PHONECARD;

	private MyMoveBroadcastReceiver myMoveBroadcastReceiver;
	private int locStep;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_softwaremovelist);

		mListView = (ScrollListView) findViewById(R.id.list);
		tvNoAppTips = (TextView) findViewById(R.id.tvNoAppTips);
		mLoadingView = findViewById(R.id.loadinglayout);

		initBottomButton();
		if (isCanMove(SoftwareMove_list_Activity.this)) {
			initHandler();

			startLoad();

			registerAllReceiver();
		} else {
			mLoadingView.setVisibility(View.GONE);
			llBottomBtn.setVisibility(View.GONE);
			tvNoAppTips.setText(R.string.phonenotsupportmove);
			tvNoAppTips.setVisibility(View.VISIBLE);
		}
	}

	private void initBottomButton() {
		llBottomBtn = (LinearLayout) findViewById(R.id.llBottomBtn);
		rbPhonecard = (RadioButton) findViewById(R.id.rbPhonecard);
		rbSdcard = (RadioButton) findViewById(R.id.rbSdcard);
		rbPhonecard.setOnClickListener(this);
		rbSdcard.setOnClickListener(this);
		rbPhonecard.setChecked(true);
	}

	public static boolean isCanMove(Context context) {
		if ((Build.VERSION.SDK_INT > 7) && (Build.VERSION.SDK_INT < 11)) {
			return true;
		}
		try {
			return !((Boolean) Environment.class.getMethod("isExternalStorageEmulated", null).invoke(null, null)).booleanValue();
		} catch (Exception e) {
			System.out.println("sdcardmove........exception.........");
			e.printStackTrace();
		}
		return false;
	}

	private void initHandler() {
		HandlerThread handlerThread = new HandlerThread("handler");
		handlerThread.start();
		mHandler = new MyHandler(handlerThread.getLooper());
	}

	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REQUEST_SOFTWARE_LIST:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mLoadingView.setVisibility(View.VISIBLE);
						tvNoAppTips.setVisibility(View.GONE);
						mListView.setVisibility(View.GONE);
						softwareMoveAdapter = new SoftwareMoveAdapter(SoftwareMove_list_Activity.this, new ArrayList<InstalledAppInfo>());
						mListView.setAdapter(softwareMoveAdapter);
						task = new FileLoadTask(SoftwareMove_list_Activity.this, softwareMoveAdapter, mHandler, flag);// 本地图片异步加载
						task.execute();
					}
				});
				break;
			case FileLoadTask.EVENT_LOADED:
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mLoadingView.setVisibility(View.GONE);
						if (softwareMoveAdapter.getCount() == 0) {
							mListView.setVisibility(View.GONE);
							if (flag == FLAG_PHONECARD) {
								tvNoAppTips.setText(R.string.noMoveToSDCardApp);
							} else {
								tvNoAppTips.setText(R.string.noMoveToPhoneCardApp);
							}
							tvNoAppTips.setVisibility(View.VISIBLE);
						} else {
							mListView.setVisibility(View.VISIBLE);
							tvNoAppTips.setVisibility(View.GONE);
						}
					}
				});
				break;
			}
		}
	}

	private void startLoad() {
		mHandler.sendEmptyMessage(EVENT_REQUEST_SOFTWARE_LIST);
	}

	private void registerAllReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE);
		filter.addAction(AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL);
		filter.addDataScheme("package");
		registerReceiver(new MyBroadcastReceiver(), filter);

		filter = new IntentFilter();
		filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
		filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
		myMoveBroadcastReceiver = new MyMoveBroadcastReceiver();
		registerReceiver(myMoveBroadcastReceiver, filter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rbPhonecard:
			flag = FLAG_PHONECARD;
			mHandler.sendEmptyMessage(EVENT_REQUEST_SOFTWARE_LIST);
			break;
		case R.id.rbSdcard:
			flag = FLAG_SDCARD;
			mHandler.sendEmptyMessage(EVENT_REQUEST_SOFTWARE_LIST);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return getParent().onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return getParent().onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return getParent().onMenuOpened(featureId, menu);
	}

	@Override
	protected void onRestart() {
		if (softwareMoveAdapter != null) {
			softwareMoveAdapter.notifyDataSetChanged();
		}
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		if (null != myMoveBroadcastReceiver) {
			unregisterReceiver(myMoveBroadcastReceiver);
		}
		super.onDestroy();
	}

	class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (AConstDefine.BROADCAST_SYS_ACTION_APPREMOVE.equals(intent.getAction())) {
				mHandler.sendEmptyMessage(EVENT_REQUEST_SOFTWARE_LIST);
			} else if (AConstDefine.BROADCAST_SYS_ACTION_APPINSTALL.equals(intent.getAction())) {
				mHandler.sendEmptyMessage(EVENT_REQUEST_SOFTWARE_LIST);
			}
		}

	}

	private class MyMoveBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE")) {
				mHandler.sendEmptyMessage(EVENT_REQUEST_SOFTWARE_LIST);
			} else if (intent.getAction().equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
				startLoad();
			}
		}
	}

	void onToolBarClick() {
		if (mListView != null) {
			locStep = (int) Math.ceil(mListView.getFirstVisiblePosition() / AConstDefine.AUTO_SCRLL_TIMES);
			mListView.post(scrollToTop);
		}
	}

	Runnable scrollToTop = new Runnable() {

		@Override
		public void run() {
			if (mListView.getFirstVisiblePosition() > 0) {
				if (mListView.getFirstVisiblePosition() < AConstDefine.AUTO_SCRLL_TIMES) {
					mListView.setSelection(mListView.getFirstVisiblePosition() - 1);
				} else {
					mListView.setSelection(Math.max(mListView.getFirstVisiblePosition() - locStep, 0));
				}
				mListView.post(this);
			}
			return;
		}
	};
}
