package com.vunken.tv_sharehome.activity;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.login.LoginApi;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.service.Const;

public class SDK_LogActivity extends BaseActivity {
	ProgressDialog dialog;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_sdklog);
		Log.d("DEMO", "tttttTTTTTTTTTTTTTTTT");
		LogApi.d("DEMO", "ACT_DemoLog onCreate");
		LogApi.e("DEMO", "ACT_DemoLog onCreate");
		LogApi.i("DEMO", "ACT_DemoLog onCreate");
			init();
		IntentFilter requestFilter = new IntentFilter(
				LogApi.EVENT_LOG_UPLOAD_REQUEST);
		IntentFilter resultFilter = new IntentFilter(
				LogApi.EVENT_LOG_UPLOAD_RESULT);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
				resultFilter);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
				requestFilter);
//		LocalBroadcastManager.getInstance(this).registerReceiver(unLoginStatusChangedReceiver, new IntentFilter(LoginApi.EVENT_LOGIN_STATUS_CHANGED));
	}
	
	private void init() {
		sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);

	}

	// 注销
	public void logout(View v) {
		sp.edit().clear().commit();
		showProgressDialog();
		subscribe = Observable.timer(2, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				//.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Long>() {

					@Override
					public void call(Long arg0) {
						// LoginApi.logout();
						if (mProgressDialog != null) {
							mProgressDialog.dismiss();
						
							try {
								LoginApi.logout();
							} catch (Exception e) {
								
							}finally{
								startActivity(new Intent(SDK_LogActivity.this,
										LoginActivity.class));
								finish();
							}
							RxBus.getInstance().post(201);
						}
					}
				});

	}

	private void showProgressDialog() {
		mProgressDialog = new ProgressDialog(SDK_LogActivity.this);
		mProgressDialog.show();
		mProgressDialog.setMessage("正在注销....");
	}

	@Override
	public void OnClick(View v) {
		// TODO Auto-generated method stub

	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogApi.d("DEMO", "log receive broadcast");
			if (dialog != null) {
				dialog.dismiss();
			}
			if (LogApi.EVENT_LOG_UPLOAD_REQUEST.equalsIgnoreCase(intent
					.getAction())) {
				Toast.makeText(mcontext, "接收请求上传日志", Toast.LENGTH_SHORT).show();
			} else if (LogApi.EVENT_LOG_UPLOAD_RESULT.equalsIgnoreCase(intent
					.getAction())) {
				Toast.makeText(
						mcontext,
						"上传日志结果:"
								+ intent.getIntExtra(
										LogApi.PARAM_LOG_UPLOAD_RESULT, -1),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mcontext, "unknow broastcast in demolog",
						Toast.LENGTH_SHORT).show();
			}
		}
	};
	private SharedPreferences sp;
	private ProgressDialog mProgressDialog;
	private Subscription subscribe;

	public void onClick_uploadLog(View view) {
		LogApi.d("DEMO", "invoke onClick_uploadLog");
		LogApi.uploadLog(1024 * 512, Const.hmeLogPath);
		dialog = new ProgressDialog(this);
		dialog.setTitle("upload log");
		dialog.setMessage("Please wait while uploading...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
	}

	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		if (subscribe != null)
			subscribe.unsubscribe();
		
		
	};
}
