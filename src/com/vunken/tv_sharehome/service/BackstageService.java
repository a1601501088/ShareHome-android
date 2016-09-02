package com.vunken.tv_sharehome.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class BackstageService extends Service {
	
	private final static String BACKSTAGE_ACTION = "com.vunken.tv_sharehome.service.BackstageService";
	 /**
     * 定时唤醒的时间间隔，5分钟
     */
    private final static int ALARM_INTERVAL = 6 * 60 * 1000;
    private final static int WAKE_REQUEST_CODE = 6666;

    private final static int GRAY_SERVICE_ID = -1001;
	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (Build.VERSION.SDK_INT < 18) {
			startForeground(GRAY_SERVICE_ID, new Notification());// API < 18
																	// ，此方法能有效隐藏Notification上的图标
		} else {
			Intent innerIntent = new Intent(this, GrayInnerService.class);
			startService(innerIntent);
			startForeground(GRAY_SERVICE_ID, new Notification());
		}
		//发送唤醒广播来促使挂掉的UI进程重新启动起来
/*        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent();
        alarmIntent.setAction(WakeReceiver.GRAY_WAKE_ACTION);
        PendingIntent operation = PendingIntent.getBroadcast(this, WAKE_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), ALARM_INTERVAL, operation);*/

		return START_STICKY;
	}

	/**
	 * 给 API >= 18 的平台上用的灰色保活手段
	 */
	public static class GrayInnerService extends Service {

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			startForeground(GRAY_SERVICE_ID, new Notification());
			stopForeground(true);
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}

		@Override
		public IBinder onBind(Intent intent) {
			  throw new UnsupportedOperationException("Not yet implemented");
		}

	}

	

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
