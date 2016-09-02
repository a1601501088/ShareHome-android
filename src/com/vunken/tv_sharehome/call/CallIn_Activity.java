package com.vunken.tv_sharehome.call;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.greendao.dao.WhiteContanctDao;
import com.vunken.tv_sharehome.greendao.dao.WhiteContanctDao.Properties;
import com.vunken.tv_sharehome.greendao.dao.bean.WhiteContanct;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.utils.DBUtils;
import com.vunken.tv_sharehome.utils.UiUtils;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * 接电话
 * 
 */
public class CallIn_Activity extends BaseActivity implements
		OnFocusChangeListener {
	private CallSession callSession = null;
	private Button callins_answer, callouts_end, switch_call;
	private ImageView callins_icon;
	private TextView callins_phoneNumber;

	@Override
	protected void onStart() {
		super.onStart();
		Log.e("CallIn_Activity", "only--onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("CallIn_Activity", "only--onResume");
		UiUtils.openLocalView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e("CallIn_Activity", "only--onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e("CallIn_Activity", "only--onStop");
	}

	@Override
	public void OnCreate() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_callins);
		Log.e("CallIn_Activity", "only--OnCreate");
		getExtras();
		initViews();
		initListener();

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callStatusChangedReceive,
						new IntentFilter(CallApi.EVENT_CALL_STATUS_CHANGED));
	}

	private void initListener() {
		switch_call.setOnFocusChangeListener(this);
		callouts_end.setOnFocusChangeListener(this);
		callins_answer.setOnFocusChangeListener(this);
	}

	// 获取数据库的白名单号码并与白名单电话比较
	// 如果包含就直接接能，否則要用戶選擇接聽與否
	private void initWhiteListFromDB() {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				WhiteContanctDao whiteContanctDao = DbCore.getDaoSession()
						.getWhiteContanctDao();
				QueryBuilder<WhiteContanct> queryBuilder = whiteContanctDao
						.queryBuilder();
				List<WhiteContanct> list = queryBuilder.where(
						Properties.HomePhone.eq(split_moblie.substring(1)))
						.list();
				if (null != list && 0 != list.size()) {
					SystemClock.sleep(2000);
					return true;
				}
				return false;

			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					callIn();
				} else {
					initCallView();
				}
			}

		}.execute();

	}

	private void initViews() {
		callins_answer = (Button) findViewById(R.id.callins_answer);// 接听
		callins_answer.requestFocus();
		callouts_end = (Button) findViewById(R.id.callouts_end);// 拒接
		switch_call = (Button) findViewById(R.id.switch_call);// 切换

		callins_icon = (ImageView) findViewById(R.id.callins_icon);

		callins_phoneNumber = (TextView) findViewById(R.id.callins_phoneNumber);
		// Log.e("tag", "接电话:"+callSession.getPeer().getNumber());
		if (callSession.getPeer() == null) {
			return;
		} else if (TextUtils.isEmpty(callSession.getPeer().getNumber())) {
			return;
		}
		String callNumber = callSession.getPeer().getNumber().trim();
		Log.e(TAG, "接电话" + callNumber);

		split_moblie = callNumber.split(Config.CALL_BEFORE)[1];
		if (split_moblie.startsWith("8") || split_moblie.startsWith("9")) {
			callins_phoneNumber.setText(split_moblie.substring(1));// 去8或9
		} else {
			callins_phoneNumber.setText(split_moblie);
		}

		// 电话并jie duan
		// String moblie = callSession.getPeer().getNumber();

		initWhiteListFromDB();// 获取数据库的白名单号码并与白名单电话比较

	}

	// 如果不是白名單走這里
	private void initCallView() {

		SetOnClickListener(callins_answer, callouts_end, switch_call);
		if (callSession.getType() == callSession.TYPE_AUDIO) {
			boolean isVideoCall = (callSession.getType() == callSession.TYPE_AUDIO);
			switch_call.setVisibility(View.GONE);
		}
		if (callSession.getType() == callSession.TYPE_VIDEO) {
			boolean isVideoCall = (callSession.getType() == callSession.TYPE_VIDEO);
		}
	}

	private void getExtras() {
		long sessionId = getIntent().getLongExtra("session_id",
				CallSession.INVALID_ID);
		callSession = CallApi.getCallSessionById(sessionId);
		if (null == callSession) {
			finish();
			return;
		}

	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.callins_answer:
			callIn();// 接电话
			break;
		case R.id.callouts_end:
			callSession.terminate();
			break;
		case R.id.switch_call:
			if (callSession.getType() == CallSession.TYPE_AUDIO) {
				callSession.accept(CallSession.TYPE_VIDEO);
			} else if (callSession.getType() == CallSession.TYPE_VIDEO) {
				callSession.accept(CallSession.TYPE_AUDIO);
			}
			break;

		default:
			break;
		}

	}

	private void callIn() {

		if (callSession.getType() == CallSession.TYPE_AUDIO) {
			callSession.accept(CallSession.TYPE_AUDIO);
		} else if (callSession.getType() == CallSession.TYPE_VIDEO) {
			callSession.accept(CallSession.TYPE_VIDEO);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		/* Unregister broadcast receiver. */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callStatusChangedReceive);
		Log.e("CallIn_Activity", "only--onDestroy");
		UiUtils.openLocalView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		// super.onSaveInstanceState(outState);
	}
	
	/* Deal with call incoming event when call status was changed. */
	private BroadcastReceiver callStatusChangedReceive = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);

			if (!callSession.equals(session)) {
				return;
			}
			int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
					CallSession.STATUS_IDLE);
			//System.out.println("newStatus:" + newStatus);
			switch (newStatus) {
			case CallSession.STATUS_CONNECTED:// CallSession.STATUS_CONNECTED =
												// // 4
				intent = new Intent();
				/* Page layout will be changed, according to calling type. */
				if (session.getType() == CallSession.TYPE_AUDIO) {
					intent.setClass(mcontext, CallAudio_Activity.class);
				} else {
					intent.setClass(mcontext, CallVideo_Activity.class);
				}
				startActivity(intent);
				finish();
				break;
			
				
			case CallSession.STATUS_IDLE:// CallSession.STATUS_IDLE = 0
				
				DBUtils.getInstance(context).insertCallRecorder(
						split_moblie.toString(),
						Config.CALLRECORDER_TYPE_MISSED, "");

				// 通知刷新通话记录
				RxBus.getInstance().post(100);
				finish();
				break;
			default:
				break;
			}
		}
	};
	private String split_moblie;

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			// v.setAnimation(mAnimation);
		} else {
			// v.clearAnimation();
		}

	}

	@Override
	public void onBackPressed() {
	
	}
}
