package com.vunken.tv_sharehome.utils;

import java.lang.reflect.Field;
import java.util.List;

import android.app.Fragment;
import android.content.Context;

import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.login.LoginCfg;
import com.huawei.rcs.login.UserInfo;
import com.vunken.tv_sharehome.Config;

public final class APIUtils {
	private static APIUtils instance;

	private APIUtils() {
	}

	public static APIUtils getInstance() {
		if (instance == null) {
			instance = new APIUtils();
		}
		return instance;
	}

	/**
	 * 获取联系人
	 * 
	 * @return
	 */
	public static List<ContactSummary> getContactList() {
		return ContactApi.getContactSummaryList(ContactApi.LIST_FILTER_ALL);
	}

	public void Login(final Context context) {
		String userName = SPUtils.getInstance(context).getUserName();
		String pass = SPUtils.getInstance(context).getPassWrod();
		login(userName, pass);
	}

	private void login(String userName, String pass) {
		// 查询用户的配置数据
		LoginCfg loginCfg = LoginApi.getLoginCfg(Config.ACCOUNT_BEFORE + "8" +userName);
		if (loginCfg == null) {
			loginCfg = new LoginCfg();
			loginCfg.isAutoLogin = true;
			loginCfg.isVerified = true;
			loginCfg.isRememberPassword = true;
		}
		UserInfo userInfo = new UserInfo();
		userInfo.username = Config.ACCOUNT_BEFORE + "8" + userName;
		userInfo.password = pass;
		LoginApi.login(userInfo, loginCfg);

	}
	


}
