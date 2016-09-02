package com.vunken.tv_sharehome.base;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.huawei.rcs.RCSApplication;
import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.call.MediaApi;
import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.hme.HmeAudioTV;
import com.huawei.rcs.hme.HmeVideo;
import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.system.DmVersionInfo;
import com.huawei.rcs.system.SysApi;
import com.huawei.rcs.tls.DefaultTlsHelper;
import com.huawei.rcs.upgrade.UpgradeApi;
import com.huawei.usp.UspCfg;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.MainActivity;
import com.vunken.tv_sharehome.activity.LoginActivity;
import com.vunken.tv_sharehome.call.CallIn_Activity;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.serv.LoginConnectStatus;
import com.vunken.tv_sharehome.serv.LoginTimer;
import com.vunken.tv_sharehome.service.BackstageService;
import com.vunken.tv_sharehome.service.CaaSSdkService;
import com.vunken.tv_sharehome.service.Const;
import com.vunken.tv_sharehome.service.NetConnectService;
import com.vunken.tv_sharehome.service.ShareHomeService;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.CommonUtil;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.UiUtils;

/**
 * 华为SDK RCSApplication 用于初始化SDK
 * */
public class HuaweiSDKApplication extends RCSApplication {
	public static HuaweiSDKApplication application;
	private String  tag =  "HuaweiSDKApplication";
	private LoginTimer loginTimer = new LoginTimer();
	 private LoginConnectStatus connectStatus;
	/**
	 * 初始化 UI 需要的组件
	 * */
	@Override
	public void onCreate() {
		super.onCreate();
		application = this;
		Logger.getInstance(this).start();
		DbCore.init(this, "sharehome2.db");
		loginTimer.startTimer(this);
		//登录状态监听
		connectStatus = new LoginConnectStatus(this);
		 connectStatus.registerReceiver();
		//loginTimer.startPing(this);
		// ----异常
		// MyUncaughtExceptionHandler handler = new
		// MyUncaughtExceptionHandler(this);
		// Thread.setDefaultUncaughtExceptionHandler(handler);
		// -----
		// LogApi.d(Const.TAG_UI, "The hmelogpath is " + Const.hmeLogPath);
		// ContactApi.init(this);// 联系人组件初始化
		File targetDir = new File(Const.hmeLogPath);
		if (!targetDir.exists()) {
			if (!targetDir.mkdirs()) {
				LogApi.e(Const.TAG_UI, "mkdir failed: " + Const.hmeLogPath);
			}
		}
		MediaApi.setConfigString(UspCfg.JEN_UMME_CFG_HME_LOGPATH,
				Const.hmeLogPath + "/");
		SysApi.loadTls(new DefaultTlsHelper());

		UpgradeApi.init(getApplicationContext());// 初始化更新API

		// 判断设备类型
		setDeviceType();

		HmeAudioTV.setup(this);// 初始化语音
		if (deviceName.equals("STB_A40")) {
			HmeVideo.setVideoMode(HmeVideo.VIDEO_MODE_VT);// 设置视频模式，机顶盒模式
		} else {
			HmeVideo.setVideoMode(HmeVideo.VIDEO_MODE_STB);// 设置视频模式，机顶盒模式
		}
		// HmeVideo.
		HmeVideo.setup(this);// 初始化视频

		CallApi.init(getApplicationContext());// 初始化CallApi

		DmVersionInfo versionInfo = new DmVersionInfo(
				"v1.1.30.8",// 版本信息
				SysApi.VALUE_MAJOR_TYPE_PLATFORM_STB,
				SysApi.VALUE_MAJOR_TYPE_OS_ANDROID,
				SysApi.VALUE_MAJOR_TYPE_APP_RCS, "00");

		SysApi.setDMVersion(versionInfo);

		// 设置配设备类型
		CallApi.setConfig(CallApi.CONFIG_MAJOR_DEVICE_NAME,
				CallApi.CONFIG_MINOR_TYPE_DEFAULT, deviceName);
		CaaSSdkService.setVideoLevel(0);

	//中兴
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			
		}
		//华为
		else {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Const.CAMERA_PLUG);
			registerReceiver(mCameraPlugReciver, filter);
		}
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callInvitationReceiver,
						new IntentFilter(CallApi.EVENT_CALL_INVITATION));
		initIPorSport();
		Intent intent = new Intent(ShareHomeService.SERVICE_NAME);
		intent.setPackage("com.vunken.tv_sharehome");
		startService(intent);

		/*
		 * Config.intent = new Intent(this,NetConnectService.class);
		 * this.startService(Config.intent);
		 */
		/*IntentFilter filter1 = new IntentFilter(Intent.ACTION_TIME_TICK);
		receiver = new MyBroadcastReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
				filter1);*/

		Intent it = new Intent(this, BackstageService.class);
		this.startService(it);
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {

				boolean isServiceRunning = CommonUtil.isServiceRunning(context,
						BackstageService.class);
				if (!isServiceRunning) {
					Intent it = new Intent(context, BackstageService.class);
					context.startService(it);
				}

			}

		}
	}

	public static HuaweiSDKApplication getApplication() {
		return application;
	}

	private void initIPorSport() {

		LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_IP,
				LoginApi.CONFIG_MINOR_TYPE_DEFAULT, Config.SIP);
		LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_PORT,
				LoginApi.CONFIG_MINOR_TYPE_DEFAULT, Config.SPORT);

	}

	public void setDeviceType() {
		String sDevice = android.os.Build.DEVICE;// 获取设备信息
		String sModel = android.os.Build.MODEL;// 获取模型信息
		LogApi.d(Const.TAG_UI, "device=" + sDevice + "--sModel=" + sModel);
		if (sDevice.contains("Hi3716CV200")) {
			Const.DEVICE_TYPE = Const.TYPE_3719C;
		} else if (sDevice.contains("Hi3719CV100")) {
			Const.DEVICE_TYPE = Const.TYPE_3719C;
		} else if (sDevice.contains("Hi3719MV100")) {
			Const.DEVICE_TYPE = Const.TYPE_3719M;
		} else if (sDevice.contains("Hi3798MV100")) {
			Const.DEVICE_TYPE = Const.TYPE_3798M;
		} else {
			LogApi.e(Const.TAG_UI, "the device is Other!");
			Const.DEVICE_TYPE = 100;// am
		}

		if (Const.TYPE_3798M == Const.DEVICE_TYPE) {
			deviceName = "STB_3798M";
		} else if (Const.DEVICE_TYPE == 100) {
			deviceName = Config.STB_A40;// am
		} else {
			deviceName = "STB_3719C";
		}
	}

	public static String deviceName = null;
	/**
	 * 电话邀请接收广播
	 * */
	BroadcastReceiver callInvitationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);

			if (session.getType() == CallSession.TYPE_VIDEO_SHARE) {
				return;
			}
			if (session.getType() == CallSession.TYPE_AUDIO) {
				Log.e("tag", "语音");
			} else if (session.getType() == CallSession.TYPE_VIDEO) {
				Log.e("tag", "视频");
			}

			// 跳转到接听页面
			Intent newIntent = new Intent(context, CallIn_Activity.class);
			newIntent.putExtra("session_id", session.getSessionId());
			Log.e("session.getSessionId()", session.getSessionId() + "");
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(newIntent);
		}
	};
	/**
	 * 摄像头插入 接收广播
	 */
	private BroadcastReceiver mCameraPlugReciver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Rect rectLocal = new Rect();

			int iState = intent.getIntExtra("state", -1);
				System.out.println("iState-->"+iState);
			if (1 == iState) {
				rectLocal.left = 0;
				rectLocal.top = 0;
				rectLocal.right = 1280;
				rectLocal.bottom = 720;
				CaaSSdkService.setLocalRenderPos(rectLocal,
						CallApi.VIDEO_LAYER_BOTTOM);
				// 打开摄像头
				UiUtils.openLocalView();
				// 显示摄像头
				CallApi.setVisible(CallApi.VIDEO_TYPE_LOCAL, true);
			} else {
				UiUtils.closeLocalView();
			}

		}
	};

	@Override
	public void onTerminate() {
		super.onTerminate();
		// 注销广播
		/*
		 * LocalBroadcastManager.getInstance(getApplicationContext())
		 * .unregisterReceiver(callInvitationReceiver);
		 */
		Logger.getInstance(this).stop();
		
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		loginTimer.endTimer();
		//loginTimer.endPing();
		connectStatus.unregisterReceiver();
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			
		} else {
			
			unregisterReceiver(mCameraPlugReciver);
		}
	}

	public static List<Activity> activities = new LinkedList<Activity>();
	private MyBroadcastReceiver receiver;

	// 不跳转至登录页面,直接退出应用
	public static void exitApp() {
		if (activities != null) {
			for (int i = 0; i < activities.size(); i++) {
				Activity activity = activities.get(i);
				activity.finish();
			}
			activities.clear();
			// 杀死当前的进程
			// android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);

		}

	}
	// 跳转至登录页面
		public static void exitAppStartLoginActivity(Context context) {
			if (activities != null) {
				for (int i = 0; i < activities.size(); i++) {
					Activity activity = activities.get(i);
					activity.finish();
				}
				activities.clear();
			

				context.startActivity(new Intent(context, LoginActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

			}

		}
}
