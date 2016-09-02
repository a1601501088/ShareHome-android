package com.vunken.tv_sharehome.serv;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.CommonUtil;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.UiUtils;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 用于30分钟登录一次，减少账号掉线情况
 * 
 * @author Administrator
 * 
 */
public class LoginTimer {

	private Subscription subscribe;
	private Subscription subscribe2;

	public void startTimer(final Context context) {
		subscribe = Observable.interval(30, TimeUnit.MINUTES)
				.filter(new Func1<Long, Boolean>() {

					@Override
					public Boolean call(Long arg0) {
						return arg0 != 0;
					}
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<Long>() {

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable arg0) {
						Logger.d("LoginTimer", "30分钟定时登录类",
								arg0.getMessage() != null ? arg0.getMessage()
										: "");
					}

					@Override
					public void onNext(Long arg0) {
						int size = HuaweiSDKApplication
						.getApplication().activities.size();
							if (size==0) {
								Logger.d("LoginTimer", "30分钟定时登录类", "登录:" + arg0);
								APIUtils.getInstance().Login(context);
							}else if (size==1){
								Activity activity = HuaweiSDKApplication
								.getApplication().activities.get(0);
								String simpleName = activity.getClass().getSimpleName();
								if ("HomeActivity".equals(simpleName)) {
									Logger.d("LoginTimer", "30分钟定时登录类", "登录:" + arg0);
									APIUtils.getInstance().Login(context);
								}
							}
						

					}
				});
	}

	public void endTimer() {
		if (subscribe != null) {
			subscribe.unsubscribe();
		}
	}

	private boolean lastPingStatus = true;// 记录ping返回的上一次状态

	/**
	 * 10s ping一下,看网络是否通
	 * 
	 * @param context
	 */
	public void startPing(final Context context) {
		subscribe2 = Observable.interval(30, TimeUnit.SECONDS)
				.filter(new Func1<Long, Boolean>() {

					@Override
					public Boolean call(Long arg0) {
						return arg0 != 0;
					}
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<Long>() {

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable arg0) {
						Logger.d("LoginTimer", "30s ping异常-->", arg0
								.getMessage() != null ? arg0.getMessage() : "");
					}

					@Override
					public void onNext(Long arg0) {
						Logger.d("LoginTimer", "30s ping-->", "" + arg0);
						boolean ping = CommonUtil.ping(context);
						Logger.d("LoginTimer", "30s ping 网络是否通-->", "" + ping);
						// 表示网络重新连接了
						if (!lastPingStatus && ping) {
							// 表示不在登录页面
								// 登录
								APIUtils.getInstance().Login(context);
						}
						lastPingStatus = ping;
						if (!ping) {
							UiUtils.showToast(context, "当前网络不可用");
						}
					}
				});
	}

	/**
	 * 结束ping
	 */
	public void endPing() {
		if (subscribe2 != null) {
			subscribe2.unsubscribe();
		}
	}

}
