package com.dongji.market.helper;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dongji.market.R;
import com.dongji.market.activity.ApkDetailActivity;
import com.dongji.market.activity.Change_Pwd_Activity;
import com.dongji.market.activity.ChannelListActivity;
import com.dongji.market.activity.Login_Activity;
import com.dongji.market.activity.MainActivity;
import com.dongji.market.activity.Search_Activity;
import com.dongji.market.activity.Search_Result_Activity;
import com.dongji.market.activity.Setting_Activity;
import com.dongji.market.activity.SoftwareManageActivity;
import com.dongji.market.adapter.SearchHistoryAdapter;
import com.dongji.market.application.AppMarket;
import com.dongji.market.database.MarketDatabase.SearchHistory;
import com.dongji.market.pojo.ApkItem;
import com.dongji.market.pojo.DownloadEntity;
import com.dongji.market.pojo.LoginParams;
import com.dongji.market.protocol.DataManager;
import com.dongji.market.receiver.CommonReceiver;
import com.dongji.market.service.DownloadService;
import com.dongji.market.widget.CustomSearchView;
import com.dongji.market.widget.CustomSearchView.OnItemClickListener;
import com.dongji.market.widget.CustomSearchView.OnKeyDownListener;
import com.dongji.market.widget.CustomSearchView.OnTextChangeListener;
import com.dongji.market.widget.ShareDialog;

/**
 * Title部分公共类
 * 
 * @author RanQing
 * 
 */
public class TitleUtil implements OnClickListener, AConstDefine {

	private Activity cxt;
	private View titleView;
	private Bundle bundle;
	private ImageView mSettingButton, mSearchButton, mSWeManageButton, mShareButton;
	private Button mBackButton;
	private TextView mPageNameTextView;
	public CustomSearchView mSearchEdit;
	private FrameLayout mTopLogoLayout;
	private ImageView mSortPageShrinkIcon;
	private PopupWindow mSettingPopup, mSharePopup, mSortPopup;
	private Button mClearKeywordBtn;
	private String pageName;

	private TextView tvCount;
	private ProgressBar manager_progress;

	public SearchHistory history;
	private List<String> data;
	public SearchHistoryAdapter historyAdapter;
	private LoginParams loginParams;
	private CommonReceiver commonRecv;

	private TextView mPopSettingDiv, mPopChgPwd, mPopLogin;
	private TextView mPopDownloadMost, mPopGradeTop, mPopRiseFastest;
	private SaveSettingListener saveListener;
	public static final int SEARCH_PAGE_FLAG = 100;
	public static final int SOFTMNG_PAGE_FLAG = 101;
	public static final int LOGIN_PAGE_FLAG = 102;
	public static final int QUITE_LOGIN_PAGE_FLAG = 103;
	public static final int CHANGE_PWD_PAGE_FLAG = 104;
	public static final int SMS_SHARE = 105;
	public static final int TENCENT_SHARE = 106;
	public static final int SINA_SHARE = 107;

	private FlowBroadcastReceiver flowBroadcastReceiver;

	private MyHandler mHandler;

	private OnSortChangeListener listener;
	private OnToolBarBlankClickListener mOnToolBarBlankClickListener;
	public boolean isSharing = false;
	private ImageView topLogo;
	private FrameLayout softManagerLay1;
	private FrameLayout softManagerLay2;
	private boolean isSearchActivity = false;
	private RelativeLayout searchLayout;
	private LinearLayout mClearLayout;

	/**
	 * 
	 * @param cxt
	 *            activity
	 * @param titleView
	 *            view
	 * @param pageName
	 * @param params
	 * @param mOnToolBarBlankClickListener
	 */
	public TitleUtil(Activity cxt, View titleView, String pageName, Bundle params, OnToolBarBlankClickListener mOnToolBarBlankClickListener) {
		this.bundle = params;
		this.cxt = cxt;
		this.titleView = titleView;
		this.pageName = pageName;
		this.mOnToolBarBlankClickListener = mOnToolBarBlankClickListener;
		initViews();
		initHandler();
		registerAllReceiver();
	}

	public TitleUtil(Activity cxt, View titleView, int pageNameId, Bundle params, OnToolBarBlankClickListener mOnToolBarBlankClickListener) {
		this(cxt, titleView, cxt.getResources().getString(pageNameId), params, mOnToolBarBlankClickListener);
	}

	public TitleUtil(Activity cxt, View titleView, int pageNameId, SaveSettingListener saveListener, Bundle params, OnToolBarBlankClickListener mOnToolBarBlankClickListener) {
		this(cxt, titleView, pageNameId, params, mOnToolBarBlankClickListener);
		this.saveListener = saveListener;
	}

	public TitleUtil(Activity cxt, View titleView, String pageName, Bundle bundle, OnSortChangeListener listener, OnToolBarBlankClickListener mOnToolBarBlankClickListener) {
		this(cxt, titleView, pageName, bundle, null);
		this.listener = listener;
		this.mOnToolBarBlankClickListener = mOnToolBarBlankClickListener;
	}

	/**
	 * 初始化actionbar
	 */
	private void initViews() {
		if (cxt.getClass().equals(Search_Activity.class)) {
			isSearchActivity = true;
		}
		initLayout();
		if (cxt.getClass().equals(ApkDetailActivity.class) || cxt.getClass().equals(MainActivity.class)) {// 首页及详情页分享
			mShareButton.setVisibility(View.VISIBLE);
			mShareButton.setOnClickListener(this);
		} else {
			mShareButton.setVisibility(View.GONE);
		}
		if (!isSearchActivity) {// 非搜索页有页面名称
			mPageNameTextView.setText(pageName);
			if (cxt.getClass().equals(ChannelListActivity.class)) {// 分类页有排序图标
				mSortPageShrinkIcon.setVisibility(View.VISIBLE);
				mSortPageShrinkIcon.setOnClickListener(this);
				mPageNameTextView.setOnClickListener(this);
			}
			View mLinearLayout = titleView.findViewById(R.id.outsidelayout);
			mLinearLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnToolBarBlankClickListener != null) {
						mOnToolBarBlankClickListener.onClick();
					}
				}
			});
		} else {// 搜索页
			if (!cxt.isFinishing()) {
				mClearLayout.setOnClickListener(this);
				history = new SearchHistory(cxt);
				data = history.getAll();
				historyAdapter = new SearchHistoryAdapter(cxt, data, history);
				mSearchEdit.setAdapter(historyAdapter);
				mSearchEdit.setOnTextChangeListener(new OnTextChangeListener() {

					public void afterTextChanged(Editable s) {
						if (TextUtils.isEmpty(s.toString())) {
							historyAdapter.updateData(history.getAll());
						} else {
							mSearchEdit.dismissDropDown();
						}
					}
				});
				mSearchEdit.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							if (TextUtils.isEmpty(mSearchEdit.getText().toString().trim())) {
								historyAdapter.notifyDataSetChanged();
							}
						}
						return false;
					}
				});
				mSearchEdit.setOnKeyDownListener(new OnKeyDownListener() {

					@Override
					public boolean onKeyDown(int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_ENTER) {
							searchInvoke(mSearchEdit.getText().toString().trim());
						}
						return false;
					}
				});
				mSearchEdit.setDropDownOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(String keyword) {
						searchInvoke(keyword);
					}
				});
				mClearKeywordBtn.setOnClickListener(this);
			}
		}
		if (cxt.getClass().equals(MainActivity.class)) {// 主页面会改变logo图标,并隐藏返回按钮
			mPageNameTextView.setText(R.string.app_name);
			mPageNameTextView.setVisibility(View.VISIBLE);
		}
		manager_progress.setMax(100);
		mBackButton.setOnClickListener(this);
		mTopLogoLayout.setOnClickListener(this);
		mSettingButton.setOnClickListener(this);
		mSearchButton.setOnClickListener(this);
		mSWeManageButton.setOnClickListener(this);
	}

	private void initLayout() {
		mTopLogoLayout = (FrameLayout) titleView.findViewById(R.id.top_logo_layout);
		mBackButton = (Button) titleView.findViewById(R.id.backButton);
		topLogo = (ImageView) titleView.findViewById(R.id.top_logo);
		mSearchButton = (ImageView) titleView.findViewById(R.id.searchButton);
		mShareButton = (ImageView) titleView.findViewById(R.id.shareButton);
		softManagerLay1 = (FrameLayout) titleView.findViewById(R.id.softmanager_layout1);
		mSWeManageButton = (ImageView) titleView.findViewById(R.id.softmanagerbutton);
		manager_progress = (ProgressBar) titleView.findViewById(R.id.manager_progress);
		softManagerLay2 = (FrameLayout) titleView.findViewById(R.id.softmanager_layout2);
		tvCount = (TextView) titleView.findViewById(R.id.tvCount);
		mSettingButton = (ImageView) titleView.findViewById(R.id.settingButton);
		if (isSearchActivity) {
			searchLayout = (RelativeLayout) titleView.findViewById(R.id.search_layout);
			mSearchEdit = (CustomSearchView) titleView.findViewById(R.id.searchEdittext);
			mClearLayout = (LinearLayout) titleView.findViewById(R.id.clearLayout);
			mClearKeywordBtn = (Button) titleView.findViewById(R.id.clearKeyword);
		} else {
			mPageNameTextView = (TextView) titleView.findViewById(R.id.page_name);
			mSortPageShrinkIcon = (ImageView) titleView.findViewById(R.id.shrink_icon);
		}

		if (DJMarketUtils.isPhone(cxt) || DJMarketUtils.getPhysicalSize(cxt) < 6) {
			return;
		}

		int screenHeight = DJMarketUtils.getScreenSize(cxt).heightPixels;
		int screenWidth = DJMarketUtils.getScreenSize(cxt).widthPixels;
		LinearLayout.LayoutParams linearParams;
		FrameLayout.LayoutParams frameParams;
		RelativeLayout.LayoutParams relativeParams;
		int actualHeight;
		int actualWidth;
		int leftMargin;
		int rightMargin;
		if (isSearchActivity) {

			actualHeight = (int) (screenHeight * 0.038);
			leftMargin = (int) (screenWidth * 0.01);
			rightMargin = (int) (screenWidth * 0.016);
			linearParams = (LayoutParams) searchLayout.getLayoutParams();
			linearParams.height = actualHeight;
			linearParams.leftMargin = leftMargin;
			linearParams.rightMargin = rightMargin;
			searchLayout.setLayoutParams(linearParams);

			actualWidth = (int) (screenWidth * 0.04);
			relativeParams = (RelativeLayout.LayoutParams) mClearLayout.getLayoutParams();
			relativeParams.width = actualWidth;
			mClearLayout.setLayoutParams(relativeParams);

			actualHeight = (int) (screenHeight * 0.016);
			actualWidth = actualHeight;
			rightMargin = (int) (screenWidth * 0.003);
			linearParams = (LayoutParams) mClearKeywordBtn.getLayoutParams();
			linearParams.height = actualHeight;
			linearParams.width = actualWidth;
			linearParams.rightMargin = rightMargin;
			mClearKeywordBtn.setLayoutParams(linearParams);

		} else {

			actualHeight = (int) (screenHeight * 0.02);
			actualWidth = actualHeight;
			leftMargin = (int) (screenWidth * 0.008);
			linearParams = (LayoutParams) mSortPageShrinkIcon.getLayoutParams();
			linearParams.width = actualWidth;
			linearParams.height = actualHeight;
			linearParams.leftMargin = leftMargin;
			mSortPageShrinkIcon.setLayoutParams(linearParams);
		}

		actualWidth = (int) (screenWidth * 0.1);
		linearParams = (LayoutParams) mTopLogoLayout.getLayoutParams();
		linearParams.width = actualWidth;
		mTopLogoLayout.setLayoutParams(linearParams);

		actualHeight = (int) (screenHeight * 0.013);
		actualWidth = (int) (actualHeight * 0.4);
		leftMargin = (int) (screenWidth * 0.008);
		frameParams = (FrameLayout.LayoutParams) mBackButton.getLayoutParams();
		frameParams.width = actualWidth;
		frameParams.height = actualHeight;
		frameParams.leftMargin = leftMargin;
		mBackButton.setLayoutParams(frameParams);

		actualHeight = (int) (screenHeight * 0.042);
		actualWidth = actualHeight;
		leftMargin = (int) (screenWidth * 0.022);
		frameParams = (FrameLayout.LayoutParams) topLogo.getLayoutParams();
		frameParams.width = actualWidth;
		frameParams.height = actualHeight;
		frameParams.leftMargin = leftMargin;
		topLogo.setLayoutParams(frameParams);

		actualHeight = (int) (screenHeight * 0.03);
		actualWidth = actualHeight;
		linearParams = (LayoutParams) mSearchButton.getLayoutParams();
		linearParams.width = actualWidth;
		linearParams.height = actualHeight;
		mSearchButton.setLayoutParams(linearParams);

		actualHeight = (int) (screenHeight * 0.03);
		actualWidth = (int) (actualHeight * 0.692);
		leftMargin = (int) (screenWidth * 0.036);
		linearParams = (LayoutParams) mShareButton.getLayoutParams();
		linearParams.width = actualWidth;
		linearParams.height = actualHeight;
		linearParams.leftMargin = leftMargin;
		mShareButton.setLayoutParams(linearParams);

		actualHeight = (int) (screenHeight * 0.033);
		actualWidth = (int) (actualHeight * 0.97);
		leftMargin = (int) (screenWidth * 0.036);
		rightMargin = (int) (screenWidth * 0.025);
		linearParams = (LayoutParams) softManagerLay1.getLayoutParams();
		linearParams.width = actualWidth;
		linearParams.height = actualHeight;
		linearParams.leftMargin = leftMargin;
		linearParams.rightMargin = rightMargin;
		softManagerLay1.setLayoutParams(linearParams);

		actualHeight = (int) (screenHeight * 0.028);
		actualWidth = actualHeight;
		frameParams = (FrameLayout.LayoutParams) softManagerLay2.getLayoutParams();
		frameParams.width = actualWidth;
		frameParams.height = actualHeight;
		softManagerLay2.setLayoutParams(frameParams);

		actualHeight = (int) (screenHeight * 0.002);
		actualWidth = (int) (actualHeight * 11.3);
		frameParams = (FrameLayout.LayoutParams) manager_progress.getLayoutParams();
		frameParams.width = actualWidth;
		frameParams.height = actualHeight;
		manager_progress.setLayoutParams(frameParams);

		actualHeight = (int) (screenHeight * 0.013);
		actualWidth = actualHeight;
		frameParams = (FrameLayout.LayoutParams) tvCount.getLayoutParams();
		frameParams.width = actualWidth;
		frameParams.height = actualHeight;
		tvCount.setLayoutParams(frameParams);

		actualHeight = (int) (screenHeight * 0.03);
		actualWidth = actualHeight;
		rightMargin = (int) (screenWidth * 0.02);
		linearParams = (LayoutParams) mSettingButton.getLayoutParams();
		linearParams.width = actualWidth;
		linearParams.height = actualHeight;
		linearParams.rightMargin = rightMargin;
		mSettingButton.setLayoutParams(linearParams);
	}

	/**
	 * 初始化Handler
	 */
	private void initHandler() {
		// 分享Handler
		HandlerThread thread = new HandlerThread("handler");
		thread.start();
		mHandler = new MyHandler(thread.getLooper());

		// 刷新Handler
		HandlerThread mHandlerThread = new HandlerThread("T");
		mHandlerThread.start();
		mRefreshTitleHandler = new DownloadRefreshHandler(mHandlerThread.getLooper());
		mRefreshTitleHandler.sendEmptyMessageDelayed(EVENT_REFRESH_DOWNLOAD, 3000L);// 3秒后发送下载刷新消息
	}

	/**
	 * 注册所有广播接收器（接收流量用完广播，普通广播（回到首页））
	 */
	private void registerAllReceiver() {
		flowBroadcastReceiver = new FlowBroadcastReceiver(cxt);
		flowBroadcastReceiver.registerMyReceiver();

		if (!(cxt instanceof MainActivity)) {
			commonRecv = new CommonReceiver();
			cxt.registerReceiver(commonRecv, new IntentFilter(AConstDefine.GO_HOME_BROADCAST));
		}
	}

	/**
	 * 取消注册广播接收器
	 * 
	 * @param cxt
	 */
	public void unregisterMyReceiver(Activity cxt) {
		if (null != flowBroadcastReceiver) {
			flowBroadcastReceiver.unregisterMyReceiver();// 注销流量用完广播接收器
		}
		if (null != commonRecv && !(cxt instanceof MainActivity)) {
			cxt.unregisterReceiver(commonRecv);// 注销普通广播接收器
		}
	}

	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.backButton:
		case R.id.top_logo_layout:
			if (cxt.getClass().equals(Setting_Activity.class)) {
				saveListener.exitVerify(true, -1);
			} else if (!cxt.getClass().equals(MainActivity.class)) {
				cxt.sendBroadcast(new Intent(AConstDefine.GO_HOME_BROADCAST));
			}
			break;
		case R.id.searchButton:
			if (cxt.getClass().equals(Setting_Activity.class)) {
				saveListener.exitVerify(false, SEARCH_PAGE_FLAG);
			} else if (cxt.getClass().equals(ApkDetailActivity.class)) {
				intent.setClass(cxt, Search_Activity.class);
				cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
				cxt.startActivity(intent);
				cxt.finish();
			} else if (cxt.getClass().equals(Search_Result_Activity.class)) {
				cxt.finish();
			} else if (!cxt.getClass().equals(Search_Activity.class)) {// 当前不为搜索界面
				intent.setClass(cxt, Search_Activity.class);
				cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
				cxt.startActivity(intent);
			} else {
				searchInvoke(mSearchEdit.getText().toString().trim());
			}
			break;
		case R.id.softmanagerbutton:
			if (cxt.getClass().equals(Setting_Activity.class)) {
				saveListener.exitVerify(false, SOFTMNG_PAGE_FLAG);
			} else if (!cxt.getClass().equals(SoftwareManageActivity.class)) {// 当前不为软件管理界面
				intent.setClass(cxt, SoftwareManageActivity.class);
				if (bundle != null) {
					intent.putExtras(bundle);
				}
				cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
				cxt.startActivity(intent);
				if (cxt.getClass().equals(ApkDetailActivity.class)) {
					cxt.finish();
				}
			}
			break;
		case R.id.settingButton:
			showOrDismissSettingPopupWindow();
			break;
		case R.id.popup_setting:
			if (!cxt.getClass().equals(Setting_Activity.class)) {// 当前不为设置界面
				intent.setClass(cxt, Setting_Activity.class);
				cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
				cxt.startActivity(intent);
				if (cxt.getClass().equals(ApkDetailActivity.class)) {
					cxt.finish();
				}
			}
			mSettingPopup.dismiss();
			break;
		case R.id.popup_login:
			if (DJMarketUtils.isLogin(cxt)) {
				/*
				 * ...... 此为退出登录逻辑,待补充,假设退出完成
				 */
				if (loginParams == null) {
					loginParams = ((AppMarket) cxt.getApplication()).getLoginParams();
				}
				loginParams.setSessionId(null);
				loginParams.setUserName(null);
				DJMarketUtils.showToast(cxt, R.string.login_out);
				if (cxt.getClass().equals(Change_Pwd_Activity.class)) {
					cxt.finish();
				}
			} else {
				if (cxt.getClass().equals(Setting_Activity.class)) {
					saveListener.exitVerify(false, LOGIN_PAGE_FLAG);
				} else if (!cxt.getClass().equals(Login_Activity.class)) {// 当前不为登录界面
					intent.setClass(cxt, Login_Activity.class);
					cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
					cxt.startActivity(intent);
					if (cxt.getClass().equals(ApkDetailActivity.class)) {
						cxt.finish();
					}
				} else if (cxt.getClass().equals(Login_Activity.class)) {
				}
			}
			mSettingPopup.dismiss();
			break;
		case R.id.popup_change_pwd:
			if (cxt.getClass().equals(Setting_Activity.class)) {
				saveListener.exitVerify(false, CHANGE_PWD_PAGE_FLAG);
			} else if (!cxt.getClass().equals(Change_Pwd_Activity.class)) {// 当前不为更改密码界面
				intent.setClass(cxt, Change_Pwd_Activity.class);
				cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
				cxt.startActivity(intent);
			}
			mSettingPopup.dismiss();
			break;
		case R.id.clearLayout:
		case R.id.clearKeyword:
			if (!TextUtils.isEmpty(mSearchEdit.getText().toString())) {
				mSearchEdit.setText("");
			}
			break;
		case R.id.shareButton:
			if (!cxt.isFinishing()) {
				boolean isApkDetailPage = false;
				if (cxt.getClass().equals(ApkDetailActivity.class)) {
					isApkDetailPage = true;
				}
				ShareDialog shareDialog = new ShareDialog(cxt, bundle, isApkDetailPage);
				shareDialog.show();
			}
			break;
		case R.id.popup_sms_share:
			if (!isSharing) {
				isSharing = true;
				mHandler.sendEmptyMessage(SMS_SHARE);
			}
			mSharePopup.dismiss();
			break;
		case R.id.shrink_icon:
		case R.id.page_name:
			if (cxt.getClass().equals(ChannelListActivity.class)) {
				showOrDismissSortPopupWindow();
			}
			break;
		case R.id.popup_download_most:
			if (this.listener != null) {
				this.listener.onSortChanged(OnSortChangeListener.SORT_BY_DOWNLOAD);
			}
			dismissPopupWindow();
			break;
		case R.id.popup_grade_top:
			if (this.listener != null) {
				this.listener.onSortChanged(OnSortChangeListener.SORT_BY_SCORE);
			}
			dismissPopupWindow();
			break;
		case R.id.popup_rise_fastest:
			if (this.listener != null) {
				this.listener.onSortChanged(OnSortChangeListener.SORT_BY_RISE);
			}
			dismissPopupWindow();
			break;
		default:
			break;
		}
	}

	/**
	 * 取消搜索Pop
	 */
	public void dismissSearchPop() {
		if (mSearchEdit != null) {
			mSearchEdit.dismissFocus();
		}
	}

	/**
	 * 搜索调用
	 * 
	 * @param keyword
	 */
	private void searchInvoke(String keyword) {
		if (!TextUtils.isEmpty(keyword)) {
			history.add(keyword);
			Intent intent = new Intent();
			intent.putExtra("search_keyword", keyword);
			intent.setClass(cxt, Search_Result_Activity.class);
			cxt.startActivity(intent);
		} else {
			DJMarketUtils.showToast(cxt, cxt.getResources().getString(R.string.input_keyword_please));
		}
	}

	class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SMS_SHARE:
				executeSmsShare();
				break;
			}
		}

	};

	public void toOtherPage(int pageFlag) {
		Intent intent = new Intent();
		switch (pageFlag) {
		case SEARCH_PAGE_FLAG:
			intent.setClass(cxt, Search_Activity.class);
			cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
			cxt.startActivity(intent);
			break;
		case SOFTMNG_PAGE_FLAG:
			intent.setClass(cxt, SoftwareManageActivity.class);
			if (bundle != null) {
				intent.putExtras(bundle);
			}
			cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
			cxt.startActivity(intent);
			break;
		case LOGIN_PAGE_FLAG:
			intent.setClass(cxt, Login_Activity.class);
			cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
			cxt.startActivity(intent);
			break;
		case CHANGE_PWD_PAGE_FLAG:
			intent.setClass(cxt, Change_Pwd_Activity.class);
			cxt.overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
			cxt.startActivity(intent);
			break;
		default:
			break;
		}
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	/**
	 * 短信分享实现
	 */
	private void executeSmsShare() {
		try {

			ApkItem apkItem = bundle.getParcelable("apkItem");
			Uri smsUri = Uri.parse("smsto:");
			Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
			intent.putExtra("sms_body", cxt.getResources().getString(R.string.share_text1) + apkItem.appName + cxt.getResources().getString(R.string.share_text2) + DataManager.newInstance().getShortUrlByLongUrl(apkItem.apkUrl));
			cxt.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isSharing = false;
		}
	}

	private void initSortPopupWindow() {
		LayoutInflater mInflater = LayoutInflater.from(cxt);
		View mLayout = mInflater.inflate(R.layout.layout_popup_sort, null);
		mLayout.setFocusableInTouchMode(true);
		mLayout.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (event.getAction()) {
				case KeyEvent.ACTION_DOWN:
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						dismissPopupWindow();
					} else if (keyCode == KeyEvent.KEYCODE_MENU) {
						dismissPopupWindow();
					}
					break;

				default:
					break;
				}
				return false;
			}
		});
		mPopDownloadMost = (TextView) mLayout.findViewById(R.id.popup_download_most);
		mPopGradeTop = (TextView) mLayout.findViewById(R.id.popup_grade_top);
		mPopRiseFastest = (TextView) mLayout.findViewById(R.id.popup_rise_fastest);
		mSortPopup = new PopupWindow(mLayout, mSortPageShrinkIcon.getWidth() + DJMarketUtils.dip2px(cxt, 80), DJMarketUtils.dip2px(cxt, 139), true);
		mSortPopup.setBackgroundDrawable(cxt.getResources().getDrawable(R.drawable.sort_popup_bg));
		mSortPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		mSortPopup.setOutsideTouchable(true);
		mSortPopup.setTouchable(true);
		mSortPopup.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				mSortPageShrinkIcon.setImageResource(R.drawable.arrows_down);
			}
		});
		mPopDownloadMost.setOnClickListener(this);
		mPopGradeTop.setOnClickListener(this);
		mPopRiseFastest.setOnClickListener(this);
	}

	private void showOrDismissSortPopupWindow() {
		if (mSortPopup == null) {
			initSortPopupWindow();
		}
		if (cxt != null && !cxt.isFinishing()) {
			if (mSortPopup.isShowing()) {
				mSortPageShrinkIcon.setImageResource(R.drawable.arrows_down);
				mSortPopup.dismiss();
			} else {
				mSortPageShrinkIcon.setImageResource(R.drawable.arrows_up);
				mSortPopup.showAsDropDown(titleView, mTopLogoLayout.getWidth(), 3);
			}
		}
	}

	private void initSettingPopupWindow() {
		LayoutInflater mInflater = LayoutInflater.from(cxt);
		View mLayout = mInflater.inflate(R.layout.layout_popup_setting, null);
		mLayout.setFocusableInTouchMode(true);
		mLayout.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (event.getAction()) {
				case KeyEvent.ACTION_DOWN:
					if (keyCode == KeyEvent.KEYCODE_MENU) {
						dismissPopupWindow();
					} else if (keyCode == KeyEvent.KEYCODE_BACK) {
						dismissPopupWindow();
					}
					break;
				}
				return false;
			}
		});
		TextView mPopSetting = (TextView) mLayout.findViewById(R.id.popup_setting);
		mPopLogin = (TextView) mLayout.findViewById(R.id.popup_login);
		mPopSettingDiv = (TextView) mLayout.findViewById(R.id.popup_chg_pwd_divider);
		mPopChgPwd = (TextView) mLayout.findViewById(R.id.popup_change_pwd);
		mSettingPopup = new PopupWindow(mLayout, mSettingButton.getWidth() * 5, (DJMarketUtils.dip2px(cxt, 100)), true);
		mSettingPopup.setBackgroundDrawable(cxt.getResources().getDrawable(R.drawable.setting_pop_bg));
		mSettingPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		mSettingPopup.setOutsideTouchable(true);
		mSettingPopup.setTouchable(true);
		mSettingPopup.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					mSettingPopup.dismiss();
					return true;
				}
				return false;
			}
		});
		mPopSetting.setOnClickListener(this);
		mPopLogin.setOnClickListener(this);
		mPopChgPwd.setOnClickListener(this);
	}

	public void showOrDismissSettingPopupWindow() {
		if (mSettingPopup == null) {
			initSettingPopupWindow();
		}
		updateSettingPop();
		if (cxt != null && !cxt.isFinishing()) {
			if (mSettingPopup.isShowing()) {
				mSettingPopup.dismiss();
			} else {
				mSettingPopup.showAsDropDown(titleView, DJMarketUtils.getScreenSize(cxt).widthPixels, 3);
			}
		}
	}

	/**
	 * 更新设置pop
	 */
	private void updateSettingPop() {
		if (DJMarketUtils.isLogin(cxt)) {
			mPopLogin.setText(R.string.login_out);
			mPopSettingDiv.setVisibility(View.VISIBLE);
			mPopChgPwd.setVisibility(View.VISIBLE);
			mSettingPopup.setHeight(DJMarketUtils.dip2px(cxt, 148));
		} else if (!DJMarketUtils.isLogin(cxt) && mPopSettingDiv.getVisibility() == View.VISIBLE) {
			mPopLogin.setText(R.string.login);
			mPopSettingDiv.setVisibility(View.GONE);
			mPopChgPwd.setVisibility(View.GONE);
			mSettingPopup.setHeight(DJMarketUtils.dip2px(cxt, 100));
		}
	}

	/**
	 * 关闭设置pop
	 */
	private void dismissPopupWindow() {
		if (cxt != null && !cxt.isFinishing()) {
			if (mSettingPopup != null && mSettingPopup.isShowing()) {
				mSettingPopup.dismiss();
			} else if (mSharePopup != null && mSharePopup.isShowing()) {
				mSharePopup.dismiss();
			} else if (mSortPopup != null && mSortPopup.isShowing()) {
				mSortPopup.dismiss();
				mSortPageShrinkIcon.setImageResource(R.drawable.arrows_down);
			}
		}
	}

	/**************** kevin logic ********************/
	private DownloadRefreshHandler mRefreshTitleHandler;
	private static final int EVENT_REFRESH_DOWNLOAD = 1;

	private class DownloadRefreshHandler extends Handler {
		DownloadRefreshHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REFRESH_DOWNLOAD:// 刷新下载
				if (cxt != null && DownloadService.mDownloadService != null) {
					List<DownloadEntity> downloadList = DownloadService.mDownloadService.getAllDownloadList();
					long downloadLength = 0;
					long downloadCur = 0;
					int updateCount = 0;
					for (int i = 0; i < downloadList.size(); i++) {
						DownloadEntity entity = downloadList.get(i);
						if (entity.downloadType == AConstDefine.TYPE_OF_DOWNLOAD) {// 需下载的类型
							downloadLength += entity.fileLength;// 下载的的总长度
							downloadCur += entity.currentPosition;// 已经下载的数量
						} else if (entity.downloadType == AConstDefine.TYPE_OF_UPDATE) {// 需更新的类型
							if (entity.getStatus() != AConstDefine.STATUS_OF_INITIAL && entity.getStatus() != AConstDefine.STATUS_OF_IGNORE) {
								downloadLength += entity.fileLength;
								downloadCur += entity.currentPosition;
							} else {// 下载状态为初始化状态或忽略更新状态
								updateCount++;// 更新数量自增
							}
						}
					}
					final int updateCount2 = updateCount;
					final long downloadLength2 = downloadLength;
					final long downloadCur2 = downloadCur;
					cxt.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (downloadLength2 > 0) {
								mSWeManageButton.setBackgroundResource(R.drawable.manager_download_selector);
								manager_progress.setVisibility(View.VISIBLE);
								double progress = downloadCur2 * 100.0 / downloadLength2;
								manager_progress.setProgress((int) progress);
								if (updateCount2 > 0) {
									tvCount.setVisibility(View.VISIBLE);
									tvCount.setText(String.valueOf(updateCount2));
								} else {
									tvCount.setVisibility(View.GONE);
								}
							} else {
								manager_progress.setVisibility(View.GONE);
								if (updateCount2 > 0) {
									tvCount.setVisibility(View.VISIBLE);
									tvCount.setText(String.valueOf(updateCount2));
									mSWeManageButton.setBackgroundResource(R.drawable.manager_update_selector);
								} else {
									tvCount.setVisibility(View.GONE);
									mSWeManageButton.setBackgroundResource(R.drawable.manager_none_selector);
								}
							}
						}
					});
				}
				mRefreshTitleHandler.sendEmptyMessageDelayed(EVENT_REFRESH_DOWNLOAD, 2000L);
				break;
			}
		}
	}

	/**
	 * 删除请求刷新下载的消息
	 */
	public void removeRefreshHandler() {
		if (mRefreshTitleHandler != null && mRefreshTitleHandler.hasMessages(EVENT_REFRESH_DOWNLOAD)) {
			mRefreshTitleHandler.removeMessages(EVENT_REFRESH_DOWNLOAD);
		}
	}

	/**
	 * 发送请求刷新下载的消息
	 */
	public void sendRefreshHandler() {
		if (mRefreshTitleHandler != null && !mRefreshTitleHandler.hasMessages(EVENT_REFRESH_DOWNLOAD)) {
			mRefreshTitleHandler.sendEmptyMessage(EVENT_REFRESH_DOWNLOAD);
		}
	}

	/**
	 * 排序改变监听
	 * 
	 * @author yvon
	 * 
	 */
	public interface OnSortChangeListener {
		static final int SORT_BY_DOWNLOAD = 1; // 按时间排序
		static final int SORT_BY_SCORE = 2; // 按评分排序
		static final int SORT_BY_RISE = 3; // 按下载次数排序

		void onSortChanged(int sort);
	}

	/**
	 * 工具条空白点击监听
	 * 
	 * @author yvon
	 * 
	 */
	public interface OnToolBarBlankClickListener {
		void onClick();
	}

	/**
	 * 保存设置监听接口
	 * 
	 * @author yvon
	 * 
	 */
	public static interface SaveSettingListener {
		public void exitVerify(boolean isFinish, int pageFlag);
	}

}
