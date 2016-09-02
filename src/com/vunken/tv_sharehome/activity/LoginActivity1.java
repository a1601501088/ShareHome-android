package com.vunken.tv_sharehome.activity;

import okhttp3.Call;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.domain.ContactVo;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.Logger;

public class LoginActivity1 extends BaseActivity {
	private String TAG = "LoginActivity1";
	private Subscription subscribe;
	private SharedPreferences sp;
	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_login);
		sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
		String login_name = sp.getString(Config.LOGIN_USER_NAME, "");
		//APIUtils.getInstance().Login(this);
		final long startTime = System.currentTimeMillis();
		String url = Config.SERVICE_URI + "/contact/queryContactAll.do";
		JSONObject json = new JSONObject();
		try {
			json.put("userName", login_name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/**
		 * 网络请求获取所有联系人
		 */
		Logger.d(TAG, "tv:获取所有联系人_new入参", json.toString());
		OkHttpUtils.post(url).params("json", json.toString())
				.execute(new JsonCallBack<ContactVo>() {
					@Override
					public void onError(Call call, Response response,
							Exception e) {
						super.onError(call, response, e);
						startActivity();
					}
					@Override
					public void onResponse(ContactVo vo) {
						Logger.d(TAG, "tv:获取所有联系人_new回参", vo.toString());
						Observable.just(vo)
								.filter(new Func1<ContactVo, Boolean>() {
									@Override
									public Boolean call(ContactVo vo) {
										return "200".equals(vo.code);
									}
								}).map(new Func1<ContactVo, Long>() {
									@Override
									public Long call(ContactVo vo) {
										/**
										 * 如何手机端删除联系人，则tv端也删除
										 */
									/*	DbCore.getDaoSession()
										.getContactDao().deleteAll();
									
										sp.edit().putBoolean(Config.FIRST_ENTRY_APPLICATION, false).commit();*/
										/**
										 * 把网络请求的数据插入到数据库
										 */
										DbCore.getDaoSession()
												.getContactDao()
												.insertOrReplaceInTx(
														vo.contacts);
										/**
										 * 如何插入数据库期间时间小于1秒，则sleep到1秒
										 */
										long lastTime = System.currentTimeMillis();
										Log.e(TAG, "插入联系人时间:"+(lastTime - startTime));
										if (lastTime - startTime < 1000) {
											SystemClock
													.sleep(1000 - (lastTime - startTime));
										}
										return lastTime;
									}
								})
								.subscribeOn(Schedulers.io())
								.observeOn(AndroidSchedulers.mainThread()) 
								.subscribe(new Subscriber<Long>() {
									@Override
									public void onCompleted() {
										startActivity();
										this.unsubscribe();
									}
									@Override
									public void onError(Throwable arg0) {
										startActivity();
										this.unsubscribe();
									}
									@Override
									public void onNext(Long lastTime) {
									}
								});

					}
				});
		
		
	}

	public void startActivity() {
		startActivity(new Intent(LoginActivity1.this, HomeActivity.class));
		finish();
	}

	@Override
	public void OnClick(View v) {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (subscribe != null) {
			subscribe.unsubscribe();

		}
	}
}
