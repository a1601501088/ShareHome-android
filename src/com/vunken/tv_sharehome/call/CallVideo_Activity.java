package com.vunken.tv_sharehome.call;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.call.VideoQosInfo;
import com.huawei.rcs.hme.HmeVideoTV;
import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.system.SysApi.PhoneUtils;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.service.CaaSSdkService;
import com.vunken.tv_sharehome.service.Const;
import com.vunken.tv_sharehome.utils.DBUtils;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.UiUtils;

public class CallVideo_Activity extends BaseActivity implements
		OnFocusChangeListener, OnKeyListener {
	private CallSession callSession;
	private boolean isMute = false;
	private String TAG = "CallVideo_Activity";
	private Timer timer;
	private int callTime;
	private Handler handler = new Handler();

	private SurfaceView sv_local_video, sv_remote_video;
	/**
	 * 如果重力感应变化角度过小，则不处理.
	 */

	private TextView callvideo_PhoneNumber, callvideo_status, callvideo_time;
	private Button callvideo_switch_audio, callvideo_mute, callvideo_cancel;

	@Override
	public void OnCreate() {
		registerReceivers();
		
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			CallApi.setPauseMode(1);
			// 拿到屏幕宽高
			Display dp = getWindowManager().getDefaultDisplay();
			screenWidth = dp.getWidth();
			screenHeight = dp.getHeight();
			/**
			 * 确认视频画面大小
			 */
			LocalBroadcastManager
					.getInstance(getApplicationContext())
					.registerReceiver(
							callViodoResolution,
							new IntentFilter(
									CallApi.EVENT_CALL_VIDEO_RESOLUTION_CHANGE));
		} else {
			HmeVideoTV.openRemoteAssistRender();
			// CallApi.createRemoteVideoView(getApplicationContext());
		}

		setContentView(R.layout.activity_callvideo);
		initCallSession();
		initData();
		initViews();
		initAlphaAnimation1();
		initAlphaAnimation2();
		// 定时器
		initTimerOut();
		initListener();

		startCallTimeTask();
	}

	private String callNumber;

	private void initData() {
	
		if (callSession!=null&&callSession.getPeer()!=null) {
			callNumber = callSession.getPeer().getNumber();
			if (callNumber.startsWith(Config.CALL_BEFORE)) {
				callNumber = callNumber.substring(8, callNumber.length());
			}
		}
		

	}

	// 設置按鈕是否可點擊
	private void callvideoClicked(boolean b) {
		callvideo_switch_audio.setClickable(b);
		callvideo_mute.setClickable(b);
		callvideo_cancel.setClickable(b);
	}

	private void initTimerOut() {
		observable = Observable.timer(10, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
		subscriber = new Subscriber<Long>() {
			@Override
			public void onCompleted() {

				rl_call_type_layout.clearAnimation();
				rl_call_type_layout.startAnimation(alphaAnimation1To0);
				callvideoClicked(false);
				clickedNum = 0;// 重置clicked计数
				this.unsubscribe();
			}

			@Override
			public void onError(Throwable arg0) {
				this.unsubscribe();
			}

			@Override
			public void onNext(Long arg0) {
			}
		};
		subscribe = observable.subscribe(subscriber);
	}

	private void initListener() {
		mAnimation = AnimationUtils.loadAnimation(this, R.anim.anim);

		callvideo_switch_audio.setOnFocusChangeListener(this);
		callvideo_cancel.setOnFocusChangeListener(this);
		callvideo_mute.setOnFocusChangeListener(this);

		callvideo_switch_audio.setOnKeyListener(this);
		callvideo_cancel.setOnKeyListener(this);
		callvideo_mute.setOnKeyListener(this);

	}

	private BroadcastReceiver callViodoResolution = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int width = intent.getIntExtra(
					CallApi.PARAM_CALL_VIDEO_RESOLUTION_WIDTH, 0);
			int height = intent.getIntExtra(
					CallApi.PARAM_CALL_VIDEO_RESOLUTION_HEIGHT, 0);
			Log.e("scale", "old_width:" + width + ";old_height:" + height);
			// 计算缩放比例
			double scale = 1;
			scale = (double) screenHeight * 1.0000 / height * 1.0000;
			Log.e(TAG, "width:" + scale * width + "-----height:" + screenHeight);
			Log.e("scale", "scale" + scale);
		int real_width =	(int) ((scale * width) + 0.5);
		if (scale<1.5) {
			real_width+= UiUtils.dip2px(40);
		}
			remote_params.width = real_width;
			sv_remote_video.requestLayout();
		}
	};
	private int clickedNum = 0;

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			clickedNum++;
			if (clickedNum > 1) {
				callvideoClicked(true);
			}
			if (subscribe != null) {
				// rl_call_type_layout.clearAnimation();
				Animation animation = rl_call_type_layout.getAnimation();
				if (alphaAnimation1To0.equals(animation)) {
					rl_call_type_layout.clearAnimation();
					rl_call_type_layout.startAnimation(alphaAnimation0To1);
				}
				subscribe.unsubscribe();
				subscribe = null;
				observable = null;
				subscriber = null;
			}
			initTimerOut();

		}
		return false;
	}

	private void initAlphaAnimation1() {
		alphaAnimation0To1 = new AlphaAnimation(0f, 1f);
		alphaAnimation0To1.setDuration(1000);
		alphaAnimation0To1.setFillAfter(true);

	}

	private void initAlphaAnimation2() {
		alphaAnimation1To0 = new AlphaAnimation(1f, 0f);
		alphaAnimation1To0.setDuration(1000);
		alphaAnimation1To0.setFillAfter(true);

	}

	private void initCallSession() {
		callSession = CallApi.getForegroudCallSession();
		if (null == callSession) {
			finish();
			return;
		}

	}

	protected boolean m_isSmallVideoCreate_MPEG = false;

	protected boolean m_isBigVideoCreate_MPEG = false;
	protected final Callback surfaceCb = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder surfaceHolder) {
			LogApi.d(Const.TAG_CALL, "surfaceCreated:");
			if (sv_local_video.getHolder() == surfaceHolder) {
				m_isBigVideoCreate_MPEG = true;
			} else if (sv_remote_video.getHolder() == surfaceHolder) {
				m_isSmallVideoCreate_MPEG = true;
			}
			if (m_isSmallVideoCreate_MPEG && m_isBigVideoCreate_MPEG) {
				showMpegView();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			LogApi.d("Const.TAG_CALL",
					"surfaceDestroyed deleteLocalVideoSurface");
			if (sv_local_video.getHolder() == arg0) {
				m_isSmallVideoCreate_MPEG = false;
			} else if (sv_remote_video.getHolder() == arg0) {
				m_isBigVideoCreate_MPEG = false;
			}
		}
	};

	protected void showMpegView() {
		if (callSession == null || sv_local_video == null
				|| sv_remote_video == null) {
			return;
		}
		if (m_isSmallVideoCreate_MPEG && m_isBigVideoCreate_MPEG
				&& callSession.getStatus() == CallSession.STATUS_CONNECTED
				&& callSession.getType() == CallSession.TYPE_VIDEO) {
			int result1 = CallApi.createLocalVideoSurface(sv_local_video
					.getHolder().getSurface());
			int result2 = CallApi.createRemoteVideoSurface(sv_remote_video
					.getHolder().getSurface());
			callSession.showVideoWindow();
		}
	}

	private void initViews() {
		sv_remote_video = (SurfaceView) findViewById(R.id.sv_small_video);
		remote_params = sv_remote_video.getLayoutParams();
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			sv_local_video = (SurfaceView) findViewById(R.id.sv_localvideo);
			sv_local_video.setBackgroundDrawable(null);
			sv_remote_video.getHolder().addCallback(surfaceCb);
			sv_local_video.getHolder().addCallback(surfaceCb);
			sv_local_video.getHolder().setFormat(PixelFormat.TRANSLUCENT);
			sv_local_video.setZOrderOnTop(true);
		} else {
			sv_remote_video.setBackgroundDrawable(null);
		}

		callvideo_PhoneNumber = (TextView) findViewById(R.id.callvideo_PhoneNumber);
		//Log.e("tag", "视频:" + callSession.getPeer().getNumber());
		callvideo_PhoneNumber.setText(callNumber.substring(1));
		callvideo_status = (TextView) findViewById(R.id.callvideo_status);
		callvideo_status.setText("通话中");
		callvideo_time = (TextView) findViewById(R.id.callvideo_time);
		callvideo_switch_audio = (Button) findViewById(R.id.callvideo_switch_audio);
		callvideo_mute = (Button) findViewById(R.id.callvideo_mute);
		callvideo_cancel = (Button) findViewById(R.id.callvideo_cancel);

		// 接听 、挂断、切换、布局
		rl_call_type_layout = (RelativeLayout) findViewById(R.id.rl_call_type_layout);

		SetOnClickListener(callvideo_switch_audio, callvideo_mute,
				callvideo_cancel);
		AudioManager audioManamger = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		boolean speakerState = audioManamger.isSpeakerphoneOn();

	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.callvideo_switch_audio:
			if (callSession != null) {
				callSession.removeVideo();
			}
			break;
		case R.id.callvideo_mute:
			isMute = !isMute;
			if (isMute) {
				callSession.mute();
			} else {
				callSession.unMute();
			}
			break;
		case R.id.callvideo_cancel:
			callSession.terminate();

			break;

		default:
			break;
		}
	}

	private Rect getFullScreenRect() {
		Rect rect = new Rect();
		rect.left = 0;
		rect.top = 0;
		rect.right = 1280;
		rect.bottom = 720;
		return rect;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			IntentFilter intent = new IntentFilter();
			intent.addAction(Config.ACTION_USB_CAMERA_PLUG_IN_OUT);
			registerReceiver(mCameraPlugReciver_STB_A40, intent);
		} else {
			callSession.showVideoWindow();
			CaaSSdkService.setRemoteRenderPos(getFullScreenRect(),
					CallApi.VIDEO_LAYER_BOTTOM);

			IntentFilter it = new IntentFilter();
			it.addAction(Const.CAMERA_PLUG);
			registerReceiver(mCameraPlugReciver, it);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			unregisterReceiver(mCameraPlugReciver_STB_A40);
		} else {
			unregisterReceiver(mCameraPlugReciver);
		}

	}

	private BroadcastReceiver mCameraPlugReciver_STB_A40 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (null == bundle) {
				LogApi.d(Const.TAG_UI,
						"Enter ACTION_USB_CAMERA_PLUG_IN_OUT bundle is null");
				return;
			}
			int state = bundle.getInt(Config.USB_CAMERA_STATE);

			LogApi.d(Const.TAG_UI, "demo videotalk mCameraPlugReciver " + state);
			if (callSession == null)
				return;
			if (0 == state) {
					callSession.closeLocalVideo();
			} else {
					int iRet = callSession.openLocalVideo();
			}

		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unRegisterReceivers();
		stopCallTimeTask();
		if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
			CaaSSdkService.setLocalRenderPos(getFullScreenRect(),
					CallApi.VIDEO_LAYER_BOTTOM);// HmeVideoTV.VEDIO_RENDER_LAYER_BOTTOM
		} else {
			sv_local_video = null;
			RxBus.getInstance().post(Config.RXBUS_REFRESH_LOCALVIEW);
		}

		if (subscribe != null) {
			subscribe.unsubscribe();
		}
		alphaAnimation0To1 = null;
		alphaAnimation1To0 = null;
		// 通知刷新联系人
		// RxBus.getInstance().post(100);
		if (HuaweiSDKApplication.getApplication().activities.size() ==0) {
			 UiUtils.closeLocalView();
		}

	}

	private BroadcastReceiver mCameraPlugReciver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Rect rectLocal = new Rect();

			int iState = intent.getIntExtra("state", -1);
			LogApi.d(Const.TAG_UI, "camera stat change:" + iState);

			if (1 == iState) {
				
				rectLocal.left = 0;
				rectLocal.top = 0;
				rectLocal.right = 320;
				rectLocal.bottom = 180;

				CaaSSdkService.setLocalRenderPos(rectLocal,
						CallApi.VIDEO_LAYER_TOP);
				CaaSSdkService.openLocalView();
				CaaSSdkService.showLocalVideoRender(true);
			} else {
				CaaSSdkService.closeLocalView();
			}

		}
	};

	/* finish the call, if call status has become idle. */
	private BroadcastReceiver callStatusChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			int newStatus = intent.getIntExtra(CallApi.PARAM_NEW_STATUS,
					CallSession.STATUS_IDLE);
			switch (newStatus) {
			case CallSession.STATUS_IDLE:
				/**
				 * 接通后挂断电话
				 */
				String call_time = callvideo_time.getText().toString();// 通话时长
				DBUtils.getInstance(context).insertCallRecorder(
						callNumber + "", Config.CALLRECORDER_TYPE_RECEIVED,
						call_time);

				// 通知刷新通话记录
				RxBus.getInstance().post(100);
				finish();
				break;
			case CallSession.STATUS_CONNECTED:// 接通
				callSession.showVideoWindow();
				/*Bundle bundle = intent.getExtras();
				int status = bundle.getInt(CallApi.PARAM_CALL_NET_STATUS);
				if (status == 1) {
					Rect rectLocal = new Rect();
					rectLocal.left = 0;
					rectLocal.top = 0;
					rectLocal.right = 320;
					rectLocal.bottom = 180;
					CaaSSdkService.setLocalRenderPos(rectLocal,
							CallApi.VIDEO_LAYER_TOP);
					CaaSSdkService.showRemoteVideoRender(true);
				}*/

				break;
			default:
				break;
			}
		}
	};

	/* display the video stream which arrived from remote. */
	private BroadcastReceiver remoteNetStatusChangeReciverr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			Bundle bundle = intent.getExtras();
			int status = bundle.getInt(CallApi.PARAM_CALL_NET_STATUS);
			if (HuaweiSDKApplication.deviceName.equals(Config.STB_A40)) {
				// TODO
			} else {
				if (status == 1) {
					Rect rectLocal = new Rect();
					rectLocal.left = 0;
					rectLocal.top = 0;
					rectLocal.right = 320;
					rectLocal.bottom = 180;
					CaaSSdkService.setLocalRenderPos(rectLocal,
							CallApi.VIDEO_LAYER_TOP);
					CaaSSdkService.showRemoteVideoRender(true);
				
					CaaSSdkService.setRemoteRenderPos(getFullScreenRect(),
							CallApi.VIDEO_LAYER_BOTTOM);
				}
			}

		}
	};
	private BroadcastReceiver callTypeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			int newType = intent.getIntExtra(CallApi.PARAM_NEW_TYPE, -1);
			if (newType == CallSession.TYPE_AUDIO) {

				Intent newIntent = new Intent(mcontext,
						CallAudio_Activity.class);
				startActivity(newIntent);
				finish();
			}
		}
	};

	private BroadcastReceiver callQosReportReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CallSession session = (CallSession) intent
					.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
			if (!callSession.equals(session)) {
				return;
			}
			VideoQosInfo videoQos = session.getVideoQos();
			//视频质量
			int rate =videoQos.getSendFramRate();
			//通话双方的双向时延
			int delay = videoQos.getDelay();
			//通话过程音频包由于网络不稳定存在的抖动严重程度
			int jitter = videoQos.getJitter();
			//通话过程中丢包率大小，如丢包率为 5%，获取到的数值就是 5；网络较差情况下，最常见的问题就是丢包过大。
			int lostRatio = videoQos.getLostRatio();
			
			Logger.d("CallVideo_Activity", "通话过程中视视频质量==>", "视频质量:"+rate+";时延:"+delay+";抖动:"
					+jitter+";接收的丢包率:"+lostRatio);
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

	private Animation mAnimation;
	private Observable<Long> observable;
	private Subscriber<Long> subscriber;
	private RelativeLayout rl_call_type_layout;
	private Subscription subscribe;
	private AlphaAnimation alphaAnimation0To1;
	private AlphaAnimation alphaAnimation1To0;
	private int screenWidth;
	private int screenHeight;
	private LayoutParams remote_params;

	private void registerReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callStatusChangedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_STATUS_CHANGED));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(
						remoteNetStatusChangeReciverr,
						new IntentFilter(
								CallApi.EVENT_CALL_VIDEO_NET_STATUS_CHANGE));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callTypeChangedReceiver,
						new IntentFilter(CallApi.EVENT_CALL_TYPE_CHANGED));

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(callQosReportReceiver,
						new IntentFilter(CallApi.EVENT_CALL_QOS_REPORT));

	}

	private void unRegisterReceivers() {
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callStatusChangedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(remoteNetStatusChangeReciverr);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callTypeChangedReceiver);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(callQosReportReceiver);

		if (HuaweiSDKApplication.deviceName.equals(Config.SP_NAME)) {
			LocalBroadcastManager.getInstance(getApplicationContext())
					.unregisterReceiver(callViodoResolution);
		}
	}

	/* this task will be started when user enters video talking. */
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
						callvideo_time.setText(PhoneUtils
								.getCallDurationTime(callTime));
					}
				});
			}
		}, 1000, 1000);
	}

	private void stopCallTimeTask() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub

	}

}
