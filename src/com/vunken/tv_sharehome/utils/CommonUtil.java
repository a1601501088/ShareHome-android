package com.vunken.tv_sharehome.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;

public class CommonUtil {

	/**
	 * 判断当前网络是否连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断服务是否运行
	 * 
	 * @param context
	 * @param clazz
	 *            要判断的服务的class
	 * @return
	 */
	public static boolean isServiceRunning(Context context,
			Class<? extends Service> clazz) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningServiceInfo> services = manager.getRunningServices(100);
		for (int i = 0; i < services.size(); i++) {
			String className = services.get(i).service.getClassName();
			if (className.equals(clazz.getName())) {
				return true;
			}
		}
		return false;
	}

	public static final boolean ping(Context context) {
		if (isNetConnected(context)) {
//			int lastIndexOf = Config.SERVICE_URI.lastIndexOf(":");
//	        int indexOf = Config.SERVICE_URI.indexOf("://");
	    
			String result = null;
			try {
//				String ip = Config.SERVICE_URI.substring(indexOf+3,lastIndexOf);// ping 的地址，可以换成任何一种可靠的外网
				String ip = Config.SIP;
				Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + ip);// ping网址3次
			
				// ping的状态
				int status = p.waitFor();
				if (status == 0) {
					result = "success";
					return true;
				} else {
					result = "failed";
				}
			} catch (IOException e) {
				result = "IOException";
			} catch (InterruptedException e) {
				result = "InterruptedException";
			} finally {
				Logger.d("CommUtils", "检查当前网络是否ping通-->", result);
			}
		}else {
			Logger.d("CommUtils", "检查当前网络是否ping通-->", "没有连接网络");
		}
		return false;
		
	}

}