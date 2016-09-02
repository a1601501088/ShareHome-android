package com.vunken.tv_sharehome.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.login.LoginCfg;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.base.BaseFragmentActivity;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.domain.ContactNumBean;
import com.vunken.tv_sharehome.fragment.AttnFragment;
import com.vunken.tv_sharehome.fragment.CallLogFragment;
import com.vunken.tv_sharehome.fragment.ContactFragment;
import com.vunken.tv_sharehome.fragment.SettingFragment;
import com.vunken.tv_sharehome.fragmentFactory.HomeFragmentFactory;
import com.vunken.tv_sharehome.greendao.dao.bean.CallRecorders;
import com.vunken.tv_sharehome.greendao.dao.bean.Contact;
import com.vunken.tv_sharehome.greendao.dao.bean.WhiteContanct;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.serv.LoginConnectStatus;
import com.vunken.tv_sharehome.service.CaaSSdkService;
import com.vunken.tv_sharehome.service.Const;
import com.vunken.tv_sharehome.service.NetConnectService;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.Camera;
import com.vunken.tv_sharehome.utils.DBUtils;
import com.vunken.tv_sharehome.utils.SPUtils;
import com.vunken.tv_sharehome.utils.UiUtils;

public class HomeActivity extends BaseFragmentActivity implements
		OnFocusChangeListener {

	private final String TAG = "HomeActivity";
	public Button home_contact, dial, more;
	private SurfaceView m_svLocalVideo;
	private Rect rectLocal;
	private Handler mHandler;
	private Runnable mLogoutRunnable = new Runnable() {
		@Override
		public void run() {
			finishSelf();
		}
	};

	@Override
	protected void onCreate(Bundle bundle) {
		Config.login_count = 1;
		super.onCreate(bundle);
		setContentView(R.layout.activity_home);
		Log.e("HomeActivity", "only--onCreate");
		camera = new Camera();
		
		initLocalView();
		initConfig();

		// 获取联系人
		initContactAndCallRecorders();
		initRxBus();
		initView();
		init();
		initContentView();
		initListener();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e("HomeActivity", "only--onStop");
		// 中兴
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			camera.unregistBroadcastReceiver(mcontext);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				handler.sendEmptyMessageDelayed(0x121, 2000);
			}
		}).start();

	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int size = HuaweiSDKApplication.getApplication().activities.size();
			if (size < 2) {
				CallApi.closeLocalView();
				finish();
			}
			
		};
	};

	private void initLocalView() {
		rectLocal = new Rect();

		rectLocal.left = 0;
		rectLocal.top = 0;
		rectLocal.right = 1280;
		rectLocal.bottom = 720;
		CaaSSdkService.setLocalRenderPos(rectLocal, CallApi.VIDEO_LAYER_BOTTOM);
		CallApi.openLocalView();
	}

	/**
	 * 判断应用是否第一次进入
	 */
	private void initConfig() {
	
		sp = this.getSharedPreferences(Config.SP_NAME, 0);
		// sp.edit().putBoolean(Config.FIRST_ENTRY_APPLICATION, true).commit();
		boolean first = sp.getBoolean(Config.FIRST_ENTRY_APPLICATION, false);
		/**
		 * 表示是第一次进入应用
		 */
		if (!first) {

			addSelfContact2List();

			/**
			 * 把状态置为不是第一次进入
			 */
			sp.edit().putBoolean(Config.FIRST_ENTRY_APPLICATION, true).commit();

		}

	}

	/**
	 * 添加手机端号码为白名单和联系人中 918390878104
	 */
	private void addSelfContact2List() {
		/**
		 * 构建联系人bean
		 */
		Contact contact = new Contact();
		String userName = sp.getString(Config.LOGIN_USER_NAME, "");
		contact.setUserName(userName);
		if (!TextUtils.isEmpty(userName)) {
			if (userName.startsWith("8")) {
				userName = userName.substring(1);
			}

			WhiteContanct whiteContanct = DBUtils.getInstance(this)
					.getWhiteContanctByPhone(userName);
			/**
			 * 如果白名间数据库没有找到该联系人才添加
			 */
			if (null == whiteContanct) {
				/**
				 * 添加到白名单
				 */
				DBUtils.getInstance(this).insertWhiteContanct("我的手机", userName,
						new Date(), "");
			}

			Contact contactByPhone = DBUtils.getInstance(this)
					.getContactByPhone(userName);
			/**
			 * 如果联系人数据库没有找到该联系人才添加
			 */
			if (null == contactByPhone) {
				/**
				 * 添加到联系人
				 */
				// contact.setUserId(0l);
				contact.setContactName("我的手机");
				contact.setHomePhone(userName);
				DbCore.getDaoSession().getContactDao().insert(contact);
			}

		}
	}

	private List<Fragment> fragments;
	private ContactFragment mContactFragment;
	private CallLogFragment callLogFragment;
	private AttnFragment attnFragment;
	private SettingFragment settingFragment;

	private void initFragment() {
		mContactFragment = (ContactFragment) HomeFragmentFactory
				.createFragment(0);
		callLogFragment = (CallLogFragment) HomeFragmentFactory
				.createFragment(1);
		attnFragment = (AttnFragment) HomeFragmentFactory.createFragment(2);
		settingFragment = (SettingFragment) HomeFragmentFactory
				.createFragment(3);
	}

	private void initListener() {
		home_call_log.setOnFocusChangeListener(this);
		dial.setOnFocusChangeListener(this);
		home_contact.setOnFocusChangeListener(this);
		more.setOnFocusChangeListener(this);

		// onkeyListener
		dial.setOnKeyListener(keyListener);
		more.setOnKeyListener(keyListener);
		home_contact.setOnKeyListener(keyListener);
		home_call_log.setOnKeyListener(keyListener);

		SetOnClickListener(home_contact, home_call_log, dial, more);

	}

	// private List<ContactBean> lists = new ArrayList<ContactBean>();
	// public List<ContactBean> contactBeans = new ArrayList<ContactBean>();

	public List<Contact> contacts;
	public List<CallRecorders> callRecorders;

	private void initContactAndCallRecorders() {

		/**
		 * 初始化联系人
		 */
		contacts = DBUtils.getInstance(mcontext).getContactList();
		/**
		 * 初始化通话记录
		 */
		callRecorders = DBUtils.getInstance(mcontext).getCallRecorderList();

	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		finish();
	}

	private void initRxBus() {

		subscribe3 = RxBus.getInstance().toObservable(ContactNumBean.class)
				.filter(new Func1<ContactNumBean, Boolean>() {

					@Override
					public Boolean call(ContactNumBean contactNumBean) {
						return contactNumBean.getTag() == 200;
					}
				}).subscribe(new Action1<ContactNumBean>() {
					@Override
					public void call(ContactNumBean contactNumBean) {
						// home_contact.requestFocus();

						dial.setCompoundDrawables(null, drawable_call_nomar,
								null, null);
						if (mContactFragment == null) {
							mContactFragment = new ContactFragment();
						}
						// setFragment(mContactFragment);
						setFragment(0);
						((ContactFragment) fragments.get(0)).setFlag(true,
								contactNumBean.getMoblie());
						// mContactFragment.setFlag(true,contactNumBean.getMoblie());
						// mContactFragment = null;
					}
				});

		subscribe = RxBus.getInstance().toObservable(Integer.class)
				.filter(new Func1<Integer, Boolean>() {

					@Override
					public Boolean call(Integer arg0) {
						return arg0 == Config.RXBUS_REFRESH_LOCALVIEW;
					}
				}).subscribe(new Subscriber<Integer>() {

					@Override
					public void onCompleted() {
						// CallApi.openLocalView();
					}

					@Override
					public void onError(Throwable arg0) {

					}

					@Override
					public void onNext(Integer arg0) {
						CaaSSdkService.setLocalRenderPos(rectLocal,
								CallApi.VIDEO_LAYER_BOTTOM);
						// CaaSSdkService.openLocalView();
					}
				});

	}

	private void initContentView() {
		m_svLocalVideo = (SurfaceView) findViewById(R.id.sv_localvideo);

		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			m_svLocalVideo.setVisibility(View.VISIBLE);
			m_svLocalVideo.getHolder().addCallback(surfaceCb);
		} else {

			m_svLocalVideo.setBackgroundDrawable(null);
			CaaSSdkService.setLocalRenderPos(rectLocal,
					CallApi.VIDEO_LAYER_BOTTOM);

		}

	}

	/**
	 * Comment for <code>surfaceCb</code><br>
	 * surfaceCb回调函数，第三方芯片适配方案使用
	 */
	protected boolean bSurfaceCreated;
	private final Callback surfaceCb = new Callback() {

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void surfaceCreated(SurfaceHolder surfaceHolder) {
			if (m_svLocalVideo != null
					&& m_svLocalVideo.getHolder() == surfaceHolder) {
				bSurfaceCreated = true;
				int result = CallApi.createLocalPreviewSurface(m_svLocalVideo
						.getHolder().getSurface());

				result = CallApi.openLocalView();

			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
			if (m_svLocalVideo != null
					&& m_svLocalVideo.getHolder() == surfaceHolder) {
				bSurfaceCreated = false;
			}
		}
	};

	private Drawable getDrawableIcon(int drawableId) {
		Drawable drawable = getResources().getDrawable(drawableId);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());

		return drawable;
	}

	private void init() {

		drawable_contact_one = getDrawableIcon(R.drawable.navigation_contact_selector);
		drawable_contact_two = getDrawableIcon(R.drawable.ic_navigation_contact_selected);

		drawable_call_log_one = getDrawableIcon(R.drawable.call_log_selector);
		drawable_call_log_two = getDrawableIcon(R.drawable.ic_call_log_press);
		drawable_call_one = getDrawableIcon(R.drawable.call_selector);
		drawable_call_two = getDrawableIcon(R.drawable.call_press);
		drawable_call_nomar = getDrawableIcon(R.drawable.call_normal);

		// 两个setting 图片
		drawable_setting_one = getDrawableIcon(R.drawable.setting_selector);
		drawable_setting_two = getDrawableIcon(R.drawable.setting_press);

		color_back = getResources().getColor(R.color.viewfinder_mask);
		color_not_back = getResources().getColor(R.color.transparent);

		home_contact = (Button) findViewById(R.id.home_contact);// 联系人
		dial = (Button) findViewById(R.id.home_dial);
		dial.requestFocus();
		more = (Button) findViewById(R.id.home_more);
		relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

		// -----默认打开拨号盘页面
		attnFragment = new AttnFragment();
		// setFragment(attnFragment);
		setFragment(2);
		relativeLayout.setVisibility(View.VISIBLE);
		relativeLayout.setBackgroundColor(getResources().getColor(
				R.color.viewfinder_mask));
		// ----------------

	}

	private OnKeyListener keyListener = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (v.getId()) {
				case R.id.home_dial:
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						more.requestFocus();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
							&& isShowBack) {
						dial.setCompoundDrawables(null, drawable_call_two,
								null, null);
					}
					break;
				case R.id.home_more:
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						home_contact.requestFocus();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						dial.requestFocus();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
							&& isShowBack) {
						more.setCompoundDrawables(null, drawable_setting_two,
								null, null);
					}
					break;
				case R.id.home_contact:
					if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						more.requestFocus();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
							&& isShowBack) {
						home_contact.setCompoundDrawables(null,
								drawable_contact_two, null, null);
					}
					break;
				case R.id.home_call_log:
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && isShowBack) {
						if (callLogFragment != null
								&& callLogFragment.listview != null
								&& callLogFragment.listview.getAdapter() != null) {
							int count = callLogFragment.listview.getCount();
							if (count == 0) {
								return true;
							} else {
								home_call_log.setCompoundDrawables(null,
										drawable_call_log_two, null, null);
								callLogFragment.listview.setSelection(0);

							}
						} else {
							return true;
						}
					}
					break;
				}
			}
			return false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		Log.e("HomeActivity", "only--onStart");
		// 中兴
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			camera.registBroadcastReceiver(mcontext);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Config.login_count = 0;
		if (subscribe3 != null)
			subscribe3.unsubscribe();
		if (subscribe != null)
			subscribe.unsubscribe();
		Log.e("HomeActivity", "only--onDestroy");
	}

	@Override
	protected void onResume() {
		super.onResume();
		isDes = false;
		Log.e("HomeActivity", "only--onResume");
	

	}

	@Override
	protected void onPause() {
		super.onPause();
	
		Log.e("HomeActivity", "only--onPause");
	}

	protected void finishSelf() {
		stopTimer();

		LoginCfg loginCfg = new LoginCfg();
		loginCfg.isAutoLogin = true;
		LoginApi.setCurrentUserLoginCfg(loginCfg);
		SharedPreferences preferences = getSharedPreferences("demo",
				MODE_PRIVATE);

		SharedPreferences.Editor editor = preferences.edit();

		editor.putBoolean("auto_login_flag", true); // value to store

		editor.commit();
		finish();
	}

	private void stopTimer() {
		if (null != mHandler) {
			mHandler.removeCallbacks(mLogoutRunnable);
		}
	}

	

	

	public void initView() {
		home_call_log = (Button) findViewById(R.id.home_call_log);

		frameLayout1 = (FrameLayout) findViewById(R.id.frameLayout1);

	}

	private boolean isShowBack = true;

	@Override
	public void onClick(View v) {
		if (!isShowBack) {
			switch (v.getId()) {
			case R.id.home_contact:
				setFragment(0);
				break;
			case R.id.home_call_log:
				setFragment(1);
				break;
			case R.id.home_dial:
				setFragment(2);
				break;
			case R.id.home_more:
				setFragment(3);
				break;
			}
			frameLayout1.setVisibility(View.VISIBLE);
			relativeLayout.setBackgroundColor(color_back);

			isShowBack = true;
		} else {
			showBack();
			isShowBack = false;
		}
	}

	private void showBack() {
		frameLayout1.setVisibility(View.INVISIBLE);
		relativeLayout.setBackgroundColor(color_not_back);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {

			int id = v.getId();
			switch (id) {
			case R.id.home_contact:
				if (isShowBack) {
					setFragment(0);
				}
				home_contact.setCompoundDrawables(null, drawable_contact_one,
						null, null);
				break;
			case R.id.home_call_log:
				if (isShowBack) {
					setFragment(1);
				}
				home_call_log.setCompoundDrawables(null, drawable_call_log_one,
						null, null);
				break;
			case R.id.home_dial:

				if (isShowBack) {
					setFragment(2);
				}
				dial.setCompoundDrawables(null, drawable_call_one, null, null);
				break;

			case R.id.home_more:

				if (isShowBack) {
					setFragment(3);
				}
				more.setCompoundDrawables(null, drawable_setting_one, null,
						null);
				break;

			}
		}

	}

	private void setFragment(int index) {
		FragmentTransaction beginTransaction = getFragmentManager()
				.beginTransaction();
		if (fragments == null) {
			fragments = new ArrayList<Fragment>();
			initFragment();
			fragments.add(mContactFragment);
			fragments.add(callLogFragment);
			fragments.add(attnFragment);
			fragments.add(settingFragment);
			for (int i = 0; i < fragments.size(); i++) {
				if (!fragments.get(i).isAdded()) {
					beginTransaction.add(R.id.frameLayout1, fragments.get(i),
							"f" + i);
				}
			}
		}

		for (int i = 0; i < fragments.size(); i++) {
			if (i == index) {
				Fragment fragment = fragments.get(i);
				if (fragment != null && fragment.isHidden()) {
					beginTransaction.show(fragments.get(i));
				}
			} else {
				Fragment fragment = fragments.get(i);
				if (fragment != null && !fragment.isHidden()) {
					beginTransaction.hide(fragments.get(i));
				}
			}
		}
		// beginTransaction.commit();
		try {
			if (!isDes) {
				beginTransaction.commitAllowingStateLoss();
			}

		} catch (Exception e) {
		}

	}

	private long exitTime = 0;

	private RelativeLayout relativeLayout;

	public Button home_call_log;

	// private Subscription subscribe;

	private Drawable drawable_setting_one;
	private int color_back;
	private int color_not_back;
	private Drawable drawable_setting_two;
	private Drawable drawable_contact_one;
	public Drawable drawable_contact_two;
	private Drawable drawable_call_log_one;
	private Drawable drawable_call_log_two;
	private Drawable drawable_call_one;
	private Drawable drawable_call_two, drawable_call_nomar;
	private Subscription subscribe3;
	private FrameLayout frameLayout1;
	private Subscription subscribe;
	private SharedPreferences sp;
	private Camera camera;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_HOME) {// 这里写你要在监听到home键之后做的事情。
			Log.e("code", "home----------------------");

		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			showToast("再按一次退出程序");
			exitTime = System.currentTimeMillis();
		} else {
			/**
			 * 在ShareHomeService 同步联系人
			 */
			/*Intent intent = new Intent(this, NetConnectService.class);
			intent.putExtra("asynContact", Config.ASYN_CONTACT);
			startService(intent);*/
			CallApi.closeLocalView();
			this.finish();
		}
	}

	@Override
	public void OnPagerSelected(int arg0) {

	}

	private boolean isDes;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		// super.onSaveInstanceState(outState);
		isDes = true;
	}

}
