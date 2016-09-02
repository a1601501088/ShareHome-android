package com.vunken.tv_sharehome.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.log.LogApi;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.service.Const;

public class Camera {
	

	public Camera() {
	}

	private BroadcastReceiver mCameraPlugReciver_STB_A40 = new BroadcastReceiver() {
		private String tag = "Camera";

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (null == bundle) {
				LogApi.d(Const.TAG_UI,
						"Enter ACTION_USB_CAMERA_PLUG_IN_OUT bundle is null");
				return;
			}
			int state = bundle.getInt(Config.USB_CAMERA_STATE);
			LogApi.d(Const.TAG_UI, "demo videotalk mCameraPlugReciver " + state);
			Logger.d(tag, "中兴摄像头热插拔:", "state:" + state);
			/*
			 * if (callSession == null) return;
			 */
			if (0 == state) {
				//callSession.closeLocalVideo();
				 CallApi.closeLocalView();
			} else {
				//int iRet = callSession.openLocalVideo();
				 CallApi.openLocalView();
			}

		}
	};
	

	



	public void registBroadcastReceiver(Context context) {
		//setSur();
		IntentFilter intent = new IntentFilter();
		intent.addAction(Config.ACTION_USB_CAMERA_PLUG_IN_OUT);
		context.registerReceiver(mCameraPlugReciver_STB_A40, intent);
	}

	public void unregistBroadcastReceiver(Context context) {
		context.unregisterReceiver(mCameraPlugReciver_STB_A40);
	}

}
