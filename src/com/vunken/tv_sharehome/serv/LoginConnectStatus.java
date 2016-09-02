package com.vunken.tv_sharehome.serv;

import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.login.LoginApi;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.activity.HomeActivity;
import com.vunken.tv_sharehome.activity.LoginActivity;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.SPUtils;
import com.vunken.tv_sharehome.utils.UiUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 用于监听登录时的状态、 网络连接但无网的监听上报
 * @author Administrator 
 *
 */
public class LoginConnectStatus {
	private String tag = this.getClass().getSimpleName();
	
	
	private Context context;

	public LoginConnectStatus(Context context) {
		this.context = context;
		if (mLoginStatusChangedReceiver==null) {
			mLoginStatusChangedReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					int new_status = intent.getIntExtra(LoginApi.PARAM_NEW_STATUS, -1);
					int reason = intent.getIntExtra(LoginApi.PARAM_REASON, -1);
					LogApi.d("tag", "the status is " + new_status);

					switch (new_status) {
					case LoginApi.STATUS_DISCONNECTED:
						String reasonCode = mapReasonStringtoReasonCode(reason);
						Logger.d("LoginConnectStatus", "登录状态监听-->", reasonCode+"");
						
						break;
					default:
						break;
					}
				}
			};
		}
	
	}

	public void registerReceiver(){
		LocalBroadcastManager.getInstance(context).registerReceiver(
				mLoginStatusChangedReceiver,
				new IntentFilter(LoginApi.EVENT_LOGIN_STATUS_CHANGED));
	}
	
	public void unregisterReceiver(){
		LocalBroadcastManager.getInstance(context)
		.unregisterReceiver(mLoginStatusChangedReceiver);
	}

	
	private BroadcastReceiver mLoginStatusChangedReceiver;

	/**
	 * 登录失败的原因
	 * */
	private String mapReasonStringtoReasonCode(int reason) {
		String reasonStr = null;
		Logger.d(tag, "登录失败的原因码only-->", reason+"");
		switch (reason) {
		case LoginApi.REASON_AUTH_FAILED:// 鉴权失败，用户名或密码错误
			reasonStr = "auth failed";
			exitApp();
			
			break;
		case LoginApi.REASON_CONNCET_ERR:// 连接错误
			reasonStr = "connect error";
			APIUtils.getInstance().Login(context);
			break;
		case LoginApi.REASON_NET_UNAVAILABLE:// 没有网络
			reasonStr = "没有网络";
			UiUtils.showToast( context, "网络出现异常");
			
			break;
		case LoginApi.REASON_NULL:// 空
			APIUtils.getInstance().Login(context);
			break;
		case LoginApi.REASON_SERVER_BUSY:// 服务器繁忙
			reasonStr = "server busy";
			APIUtils.getInstance().Login(context);
			break;
		case LoginApi.REASON_SRV_FORCE_LOGOUT:// 强行注销
			reasonStr = "force logout";
			UiUtils.showToast( context, "账号异地登录，被服务器强制下线");
			exitApp();
			
			break;
		case LoginApi.REASON_USER_CANCEL:// 用户取消了
			reasonStr = "user canceled";
			break;
		case LoginApi.REASON_WRONG_LOCAL_TIME:// 当地时间错了
			reasonStr = "wrong local time";
			APIUtils.getInstance().Login(context);
			break;
		case LoginApi.REASON_ACCESSTOKEN_INVALID:// 无效的访问令牌
			reasonStr = "invalid access token";
			APIUtils.getInstance().Login(context);
			break;
		case LoginApi.REASON_ACCESSTOKEN_EXPIRED:// 访问令牌过期
			reasonStr = "access token expired";
			APIUtils.getInstance().Login(context);
			break;
		case LoginApi.REASON_APPKEY_INVALID:// 无效的application 密钥
			reasonStr = "invalid application key";
			APIUtils.getInstance().Login(context);
			break;
		case LoginApi.REASON_UNKNOWN:// 未知的
		default:
			reasonStr = "unknown";
			 APIUtils.getInstance().Login(context);
			break;
		}
		return reasonStr;
	}

	private void exitApp() {
		/**
		 * 清空sp配置信息
		 */
		SPUtils.getInstance(context).clear();
		/**
		 * 清除本地通话记录数据库
		 */
		DbCore.getDaoSession().getCallRecordersDao().deleteAll();
		/**
		 * 清除本地联系人数据库
		 */
		DbCore.getDaoSession().getContactDao().deleteAll();
		/**
		 * 清除本地白名单数据库
		 */
		DbCore.getDaoSession().getWhiteContanctDao().deleteAll();
		HuaweiSDKApplication.exitAppStartLoginActivity(context);
	}
		
}
