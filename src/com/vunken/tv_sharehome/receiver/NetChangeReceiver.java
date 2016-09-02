package com.vunken.tv_sharehome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huawei.rcs.login.LoginApi;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.service.NetConnectService;
import com.vunken.tv_sharehome.utils.CommonUtil;

/**
 * C_only：Administrator on 2016/2/2 18:10
 */
public class NetChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		boolean netConnected = CommonUtil.isNetConnected(context);
		if (netConnected) {
			
			if (Config.net_connect_true == 0) {
				Config.net_connect_true = 1;
				Intent it = new Intent(context, NetConnectService.class);
				it.putExtra("asynContact", Config.INTENT_LOGIN_VALUE);
				context.startService(it);

			}

		} else {
			Config.net_connect_true = 0;
			LoginApi.logout();
		}

	}
	/**
	 * 为帐号输入框写入帐号
	 */
	/*
	 * private UserInfo mLastUserInfo;// 华为SDK存储的用户信息 private SharedPreferences
	 * sp; private void initLoginData(Context context) { sp =
	 * context.getSharedPreferences(Config.SP_NAME, context.MODE_PRIVATE);
	 * String userName = sp.getString(Config.LOGIN_USER_NAME, ""); if
	 * (!TextUtils.isEmpty(userName)) {
	 * 
	 * initLoginInfo(userName); } else { Log.e("tag", "没有账号");
	 * //startActivityToLoginActivity(); }
	 * 
	 * } private void initLoginInfo(String userName) {
	 * 
	 * String pass = sp.getString(Config.LOGIN_PASSWORD, ""); if
	 * (!TextUtils.isEmpty(pass)) { login(userName, pass); }else {
	 * //startActivityToLoginActivity(); }
	 * 
	 * } private void login(String userName, String pass) { // 查询用户的配置数据
	 * LoginCfg loginCfg = LoginApi.getLoginCfg(userName); if (loginCfg == null)
	 * { loginCfg = new LoginCfg(); loginCfg.isAutoLogin = true;
	 * loginCfg.isVerified = false; loginCfg.isRememberPassword = true; }
	 * UserInfo userInfo = new UserInfo(); userInfo.username =
	 * Config.ACCOUNT_BEFORE + userName; userInfo.password = pass;
	 * 
	 * LoginApi.login(userInfo, loginCfg);
	 * 
	 * }
	 */
}
