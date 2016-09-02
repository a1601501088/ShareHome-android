package com.vunken.tv_sharehome.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.login.LoginApi;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.net.MissedCallNet;
import com.vunken.tv_sharehome.service.CaaSSdkService;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.DBUtils;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.SPUtils;
import com.vunken.tv_sharehome.utils.UiUtils;

/**
 * 拨电话
 **/
public class CallOut_Activity extends BaseActivity {
	private String tag = this.getClass().getSimpleName();
	private String PhoneNumber;
	private boolean isVideoCall;
	CallSession callSession = null;
	private TextView callout_Phonenumber;
	private Button callout_cancel;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_callout);
		/*
		 * 注册一个广播接收器，它将被称为一次呼叫状态改变.
		 */

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callStatusChangedReceive,
						new IntentFilter(CallApi.EVENT_CALL_STATUS_CHANGED));

		getExtras();
		initViews();
		initCall();
		Log.e("CallOut_Activity", "only--OnCreate");
		// insertCallRecorder();

	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.e("CallOut_Activity", "only--onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.e("CallOut_Activity", "only--onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e("CallOut_Activity", "only--onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();

		Log.e("CallOut_Activity", "only--onStop");
	}

	private void getExtras() {
		PhoneNumber = getIntent().getStringExtra("PhoneNumber");
		isVideoCall = getIntent().getBooleanExtra("is_video_call", false);
	}

	private String username = "";
	private String calledType = "9";//被叫号码类型
	private void initViews() {

		callout_Phonenumber = (TextView) findViewById(R.id.callout_Phonenumber);
		username = PhoneNumber.substring(9);
		 calledType = PhoneNumber.substring(8, 9);
		
		callout_Phonenumber.setText(username);
		callout_cancel = (Button) findViewById(R.id.callout_cancel);
		SetOnClickListener(callout_cancel);
	}

	private void call() {
		if (isVideoCall) {
			callSession = CallApi.initiateVideoCall(PhoneNumber);
		} else {
			callSession = CallApi.initiateAudioCall(PhoneNumber);
		}
	}

	private void initCall() {
		
		call();

		/* 当你收到一个错误代码的时候，你必须完成这个程序。 */
		if (callSession.getErrCode() != CallSession.ERRCODE_OK) {
			APIUtils.getInstance().Login(mcontext);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					call();
				}
			}, 3000);
		}

	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.callout_switch_video:

			break;
		case R.id.callout_mute:

			break;
		case R.id.callout_cancel:
			if (callSession.getErrCode() != CallSession.ERRCODE_OK) {
				finish();
				return;
			}
			callSession.terminate();
			break;
		/*
		 * case value: break;
		 */

		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		/* Unregister broadcast receiver which you register before. */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callStatusChangedReceive);

		Log.e("CallOut_Activity", "only--onDestroy");

	}

	/*
	 * 如果调用状态已成为连接,它将会转到电话说的布局。否则,调用请求也许被拒绝了。
	 */
	/**
	 * 记录STATUS_ALERTING次数据 如果些状态出现一次则发送短信
	 * 
	 */
	private int alerting_count = 0;
	private long current_time = 0;
	private long dy_time = 0;
	private BroadcastReceiver callStatusChangedReceive = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			/* Call session should be checked against. */
			if (!callSession.equals(session)) {
				return;
			}
			int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
					CallSession.STATUS_IDLE);
			
			switch (newStatus) {
			case CallSession.STATUS_CONNECTED:// CallSession.STATUS_CONNECTED =
				intent = new Intent();
				if (session.getType() == CallSession.TYPE_AUDIO) {
					intent.setClass(mcontext, CallAudio_Activity.class);
				} else {
					intent.setClass(mcontext, CallVideo_Activity.class);
				}
				startActivity(intent);
				finish();
				break;
			case CallSession.STATUS_OUTGOING:// 1
				alerting_count = 0;
				dy_time = 0;
				current_time = System.currentTimeMillis();
				break;
			case CallSession.STATUS_ALERTING:// 3
				alerting_count ++;
				if (alerting_count==1) {
					 dy_time = System.currentTimeMillis() - current_time;
					Logger.d(tag, "拨打电话1至3之间的时间隔", dy_time+"");
				}
				
				break;
			case CallSession.STATUS_IDLE:// CallSession.STATUS_IDLE = 0
				/**
				 * 插入通话记录
				 */
				DBUtils.getInstance(mcontext).insertCallRecorder(
						PhoneNumber.substring(8),
						Config.CALLRECORDER_TYPE_DIAL, "");

				// 通知刷新通话记录
				RxBus.getInstance().post(100);
				finish();
				Logger.d(tag, "是否发送未接电话提醒:", "alerting_count:"+alerting_count+";dy_time:"+dy_time);
				// 调用未接来电提醒接口 ,表示对方已登录但对方没有网络(对方无法接通)
				if (alerting_count==1&&dy_time>2000) {
					sendSms(1);
					Logger.d(tag, "only_status对方无法接通:", "only_status对方无法接通");
				}
				//表示对方不在线或对方不是想家用户
				else if (alerting_count==1&&dy_time<1000) {
					sendSms(2);
					Logger.d(tag, "only_status对方无法接通:", "only_status对方不在线");
				}
				String reason_text = intent.getStringExtra(CallApi.PARAM_SIP_REASON_TEXT);
				String status_code =	intent.getStringExtra(CallApi.PARAM_SIP_STATUS_CODE);
				String cause =intent.getStringExtra(CallApi.PARAM_SIP_CAUSE);
				Bundle extras = intent.getExtras();
				String string = extras.getString(CallApi.PARAM_SIP_REASON_TEXT);
				
				System.out.println("reason_text:"+reason_text+";status_code:"+status_code+";cause:"+cause+";string:"+string);
				System.out.println("newStatus-only:" + newStatus);
				
				break;

			default:
				break;
			}
		}

		
	};
	private void sendSms(final int status) {
		
		new MissedCallNet(SPUtils.getInstance(mcontext).getUserName(), username,  current_time+"", "8", calledType,new MissedCallNet.MissedCallNetCallback() {
			
			@Override
			public void onSuccess(String code, String messge) {
				UiUtils.showToast(status==1?"对方无法接通,已经使用短信方式通知对方":"对方不在线,已经使用短信方式通知对方");
			}
			
			@Override
			public void onFail(String code, String messge) {
				
			}
		});
	}
	@Override
	public void onBackPressed() {
	}

}
