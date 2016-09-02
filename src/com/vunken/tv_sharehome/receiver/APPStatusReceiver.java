package com.vunken.tv_sharehome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.vunken.tv_sharehome.activity.LoginActivity;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.SPUtils;

public class APPStatusReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Uri uri = intent.getData();
		if("android.intent.action.PACKAGE_ADDED".equals(action)){
		}
		if("android.intent.action.PACKAGE_REPLACED".equals(action)){
			if ("package:com.vunken.tv_sharehome".equals(uri.toString())) {
				String passWrod = SPUtils.getInstance(context).getPassWrod();
				String userName = SPUtils.getInstance(context).getUserName();
				if (!TextUtils.isEmpty(passWrod)&&!TextUtils.isEmpty(userName)) {
					APIUtils.getInstance().Login(context);
				}else {
					Intent it = new Intent(context,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(it);
				}
			}
		}
		if("android.intent.action.PACKAGE_REMOVED".equals(action)){
		}
	}

}
