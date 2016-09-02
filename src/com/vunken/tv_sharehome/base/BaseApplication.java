package com.vunken.tv_sharehome.base;

import java.util.LinkedList;
import java.util.List;

import com.vunken.tv_sharehome.MainActivity;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class BaseApplication extends Application {
	private static BaseApplication application;
	private static List<Activity> activities;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	public static Context getApplication() {
		return application;
	}

	public static List<Activity> getActivities() {
		if (activities == null) {
			activities = new LinkedList<Activity>();
		}
		return activities;
	}

	// 不跳转至登录页面,直接退出应用
	public static void exitApp() {
		if (activities != null) {
			for (int i = 0; i < activities.size(); i++) {
				Activity activity = activities.get(i);
				activity.finish();
			}
			activities.clear();
			activities = null;
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
			activities = null;

			context.startActivity(new Intent(context, MainActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

		}

	}

	/**
     * 获得当前进程的名字
     *
     * @param context
     * @return 进程号
     */
    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
