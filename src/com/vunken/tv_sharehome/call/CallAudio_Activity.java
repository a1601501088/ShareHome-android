package com.vunken.tv_sharehome.call;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.system.SysApi.PhoneUtils;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.utils.DBUtils;

public class CallAudio_Activity extends BaseActivity {
	private CallSession videoShareCallsession = null;
	private boolean isVideoShareCaller = false;
	public static String PARAM_SESSION_ID = "PARAM_SESSION_ID";
	public static String PARAM_IS_CALLER = "PARAM_IS_CALLER";
	private AlertDialog alertDialog;
	private CallSession callSession;
	private boolean isMute = false;
	private Timer timer;
	private int callTime;
	private Handler handler = new Handler();

	private TextView call_phoneNumber, callout_status, callout_time;
	private ImageView callout_icon;
	private Button callout_switch_video, callout_mute,
			callout_cancel, call_handsfree;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_callaudio);
		initCallSession();
		initData();
		initViews();

		registerReceivers();/* 注册广播接收器。 */

		startCallTimeTask();/* 计算通话时间。 */
	}
 private String callNumber;
	private void initData() {
		callNumber = callSession.getPeer().getNumber();
		if (callNumber.startsWith(Config.CALL_BEFORE)) {
			callNumber = callNumber.substring(8);
		}
		
	}

	public void initCallSession() {
		callSession = CallApi.getForegroudCallSession();
		if (null == callSession) {
			LogApi.d("V2OIP", "没有发现通话");
			finish();
			return;
		}
	}

	private void initViews() {
		call_phoneNumber = (TextView) findViewById(R.id.call_phoneNumber);
		Log.e("tag", "语音:"+callSession.getPeer().getNumber());
		
		call_phoneNumber.setText(callNumber.substring(1));

		callout_status = (TextView) findViewById(R.id.callout_status);

		callout_time = (TextView) findViewById(R.id.callout_time);

		callout_icon = (ImageView) findViewById(R.id.callout_icon);

		callout_switch_video = (Button) findViewById(R.id.callout_switch_video);
		callout_mute = (Button) findViewById(R.id.callout_mute);
		callout_cancel = (Button) findViewById(R.id.callout_cancel);
		AudioManager audioManamger = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		boolean speakerState = audioManamger.isSpeakerphoneOn();
		callout_status.setText("通话中");
		SetOnClickListener(callout_switch_video, 
				callout_mute, callout_cancel, call_handsfree);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.callout_switch_video:
			if (callSession.isAbleToAddVideo()) {
				callSession.addVideo();
				Toast.makeText(getApplicationContext(), "发送邀请,等待对方接受……",
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.callout_mute:
			isMute = !isMute;
			if (isMute) {
				callout_status.setText("静音");
				callSession.mute();
			} else {
				callout_status.setText("通话中");
				callSession.unMute();
			}
			break;
		case R.id.callout_cancel:
			callSession.terminate();
			showToast("通话结束");
			break;

		default:
			break;
		}

	}

	private void registerReceivers() {
		/*
		 * 注册一个广播接收器，用于处理呼叫邀请事件。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callInvitationReciever,
						new IntentFilter(CallApi.EVENT_CALL_INVITATION));
		/*
		 * 注册一个广播接收器，用于检测电话状态。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callStatusChangedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_STATUS_CHANGED));
		/*
		 * 注册广播接收器，用于检测呼叫类型被邀请的。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(
						callTypeChangeInvitationReceiver,
						new IntentFilter(
								CallApi.EVENT_CALL_TYPE_CHANGED_INVITATION));
		/*
		 * 注册一个广播接收器，用于检测呼叫类型。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callTypeChangedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_TYPE_CHANGED));
		/*
		 * 注册广播接收器，用于检测呼叫类型是否已更改为拒绝。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(
						callTypeChangeRejectedReceiver,
						new IntentFilter(
								CallApi.EVENT_CALL_TYPE_CHANGED_REJECTED));
		/*
		 * 注册广播接收器，为了应对服务质量报告事件。
		 */
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callQosReportReceiver,
						new IntentFilter(CallApi.EVENT_CALL_QOS_REPORT));

	}

	private void unRegisterReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callInvitationReciever);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callStatusChangedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callTypeChangeInvitationReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callTypeChangedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callTypeChangeRejectedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callQosReportReceiver);
	}

	/* 收到一个视频分享电话。 */
	private BroadcastReceiver callInvitationReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			videoShareCallsession = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (null == videoShareCallsession) {
				return;
			}
			/* only received by callee. */
			if (CallSession.TYPE_VIDEO_SHARE == videoShareCallsession.getType()) {
				isVideoShareCaller = false;
				Toast.makeText(mcontext, "A Video Share Invitation Incoming",
						Toast.LENGTH_LONG).show();
			}
		}
	};
	/* 电话状态改变 */
	private BroadcastReceiver callStatusChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			/* 视频通话的状态不同于另一个呼叫. */
			if (session == videoShareCallsession) {
				int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
						CallSession.STATUS_IDLE);

				switch (newStatus) {
				case CallSession.STATUS_ALERTING:
					Toast.makeText(getApplicationContext(), "视频共享会话提醒",
							Toast.LENGTH_LONG).show();
					break;
				case CallSession.STATUS_CONNECTED: // 接受方
					Intent newIntent = new Intent(mcontext,
							CallVideo_Activity.class);
					newIntent
							.putExtra(PARAM_SESSION_ID, session.getSessionId());
					newIntent.putExtra(PARAM_IS_CALLER, isVideoShareCaller);
					startActivityForResult(newIntent, 0);
					break;
				case CallSession.STATUS_IDLE: // 被拒绝的
					if (isVideoShareCaller) {
						Toast.makeText(getApplicationContext(), "视频共享会话终止",
								Toast.LENGTH_LONG).show();
					}
					break;
				default:
					break;
				}

				return;
			}

			if (!callSession.equals(session)) {
				return;
			}
			int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
					CallSession.STATUS_IDLE);
			switch (newStatus) {
			case CallSession.STATUS_HOLD:
				break;
			case CallSession.STATUS_HELD:
				break;
			case CallSession.STATUS_CONNECTED:
				break;
			case CallSession.STATUS_IDLE:
				
				/**
				 * 接通后挂断电话
				 */
				String call_time = callout_time.getText().toString();// 通话时长
				DBUtils.getInstance(context).insertCallRecorder(
						callNumber + "", Config.CALLRECORDER_TYPE_RECEIVED,
						call_time);
				
				// 通知刷新通话记录
				RxBus.getInstance().post(100);
				finish();
				break;
			default:
				break;
			}
		}
	};

	/* 接到视频电话。 */
	private BroadcastReceiver callTypeChangeInvitationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			Builder dl = new AlertDialog.Builder(mcontext);
			dl.setTitle("提示");
			// dl.setMessage(R.string.fail);
			dl.setMessage("对方是邀请一个视频电话，接受或不接受？");
			dl.setPositiveButton("接受", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					callSession.acceptAddVideo();
					alertDialog.dismiss();
					alertDialog = null;
				}
			});
			dl.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					callSession.rejectAddVideo();
					alertDialog.dismiss();
					alertDialog = null;
				}
			});

			alertDialog = dl.create();
			alertDialog.show();
		}

	};

	/* 对视频呼叫的邀请已被接受。 */
	private BroadcastReceiver callTypeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			int newType = intent.getIntExtra(CallApi.PARAM_NEW_TYPE, -1);
			if (newType == CallSession.TYPE_VIDEO) {
				Intent newIntent = new Intent(mcontext,
						CallVideo_Activity.class);
				startActivity(newIntent);
				finish();
			}
		}
	};

	/* 邀请视频通话已被拒绝 */
	private BroadcastReceiver callTypeChangeRejectedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			Toast.makeText(getApplicationContext(), "你的邀请被拒绝",
					Toast.LENGTH_LONG).show();
		}
	};

	/* 处理服务质量报告事件。 */
	private BroadcastReceiver callQosReportReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			int quality = intent.getIntExtra(CallApi.PARAM_CALL_QOS,
					CallApi.QOS_QUALITY_NORMAL);
			switch (quality) {
			case CallApi.QOS_QUALITY_GOOD:
				break;

			case CallApi.QOS_QUALITY_NORMAL:
				break;

			case CallApi.QOS_QUALITY_BAD:
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		/* unregister broadcast receivers. */
		unRegisterReceivers();
		if (null != alertDialog) {
			alertDialog.dismiss();
			alertDialog = null;
		}
		/*
		 * end call time task.You should stop the time task when this activity
		 * is destroy.
		 */
		stopCallTimeTask();
	}

	private void stopCallTimeTask() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void startCallTimeTask() {
		timer = new Timer();
		callTime = (int) ((System.currentTimeMillis() - callSession
				.getOccurDate()) / 1000);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				callTime++;
				handler.post(new Runnable() {
					@Override
					public void run() {
						callout_time.setText(PhoneUtils
								.getCallDurationTime(callTime));
					}
				});
			}
		}, 1000, 1000);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	}
}
