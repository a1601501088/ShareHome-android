package com.vunken.tv_sharehome.activity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.huawei.rcs.caasomp.CaasOmpCfg;
import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.login.LoginCfg;
import com.huawei.rcs.login.UserInfo;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.Encrypt3DES;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.NetMessage;
import com.vunken.tv_sharehome.utils.SPUtils;
import com.vunken.tv_sharehome.utils.UiUtils;

public class LoginActivity extends BaseActivity {
	
	private String tag = "LoginActivity";
	private ProgressDialog mypDialog;// 弹窗
	
	private boolean bIsAutoLogin = true;

	// 电话区号 中国地区为 +86
	private String key = "l7xUXj0ipu4wTE1BOKdCVQg6tCoa";// 登录密钥
	/**
	 * 查询用户的配置数据
	 */
	private LoginCfg mLoginCfg = null;
	private UserInfo mLastUserInfo;// 华为SDK存储的用户信息

	@Override
	public void OnCreate() {
		getWindow().addFlags(  WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); 
		setContentView(R.layout.activity_login2);
		// 登录状态的广播
		LocalBroadcastManager.getInstance(this).registerReceiver(
				LoginStatusChangedReceiver,
				new IntentFilter(LoginApi.EVENT_LOGIN_STATUS_CHANGED));
		initViews();
		getData();
		AutoLogin();
		initData();

		initListener();

		// initData();
		// AutoLogin();
	}

	private void initViews() {
		
		btn_get_smsCode = (Button) findViewById(R.id.btn_get_smsCode);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		et_mobile = (EditText) findViewById(R.id.et_mobile);
		et_code = (EditText) findViewById(R.id.et_code);
		tv_mobile_hint = (TextView) findViewById(R.id.tv_mobile_hint);
		tv_code_hint = (TextView) findViewById(R.id.tv_code_hint);

	}

	/**
	 * 获取短信验证码按钮
	 */
	private void getSmsCode() {
		btn_get_smsCode.setClickable(false);
		Observable.interval(0, 1, TimeUnit.SECONDS)
				.filter(new Func1<Long, Boolean>() {
					@Override
					public Boolean call(Long aLong) {
						btn_get_smsCode.setClickable(false);
						return aLong <= 60;
					}
				}).map(new Func1<Long, Long>() {
					@Override
					public Long call(Long aLong) {
						if (aLong == 0) {
							sendRequsetGetSmsCode();
						}
						return -(aLong - 60);
					}

				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<Long>() {
					@Override
					public void onCompleted() {
					}

					@Override
					public void onError(Throwable e) {
						this.unsubscribe();
						btn_get_smsCode.setClickable(true);
					}

					@SuppressLint("ResourceAsColor")
					@Override
					public void onNext(Long aLong) {
						if (aLong != 0) {
							btn_get_smsCode.setText(aLong + "秒后重新获取");
						} else {
							this.unsubscribe();
							btn_get_smsCode.setClickable(true);
							btn_get_smsCode.setText("重新发送验证码");
						}
					}
				});

	}

	private boolean smsCode_success;

	private void initListener() {
		btn_get_smsCode.setOnClickListener(this);
		btn_confirm.setOnClickListener(this);

		/**
		 * 隐藏验证码错误提示
		 */
		et_code.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					tv_code_hint.setVisibility(View.INVISIBLE);
				} else {
					if (registVailta()) {

						/**
						 * 请求网络判断验证码是否正确
						 */
						String url = Config.SERVICE_URI
								+ "/sendMsg/vaildateSmsCode.do";
						JSONObject json = new JSONObject();
						try {
							json.put("userName", et_mobile.getText().toString());
							json.put("smsCode", et_code.getText().toString());
						} catch (JSONException e) {
							e.printStackTrace();
						}
						OkHttpUtils.post(url).params("json", json.toString())
								.execute(new JsonCallBack<String>() {
									@Override
									public void onResponse(String t) {
										NetMessage netMessage = new NetMessage(
												t);
										String code = netMessage.getCode();
										if ("200".equals(code)) {
											tv_code_hint
													.setVisibility(View.INVISIBLE);
											smsCode_success = true;
										} else {
											tv_code_hint
													.setVisibility(View.VISIBLE);
											smsCode_success = false;
										}
									}
								});
					}
				}
			}
		});
		et_mobile.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					tv_mobile_hint.setVisibility(View.INVISIBLE);
				}else {
					registVailta();
				}
				
			}
		});
	}

	private void getData() {
		mLastUserInfo = LoginApi.getUserInfo(LoginApi.getLastUserName());
		if (mLastUserInfo==null||mLastUserInfo.username==null) {
			return;
		}
		// 查询用户的配置数据
		mLoginCfg = LoginApi.getLoginCfg(mLastUserInfo.username);
	}

	private void initData() {
		if (mLastUserInfo != null) {
			if (!TextUtils.isEmpty(mLastUserInfo.countryCode)) {// 自动写入帐号
				countryCode = mLastUserInfo.countryCode;
				String username = mLastUserInfo.username;
				if (!TextUtils.isEmpty(username)
						&& username.length() > Config.ACCOUNT_BEFORE.length()) {
					username = username.substring(Config.ACCOUNT_BEFORE
							.length() + 1);
					// et_mobile.setText(username);
				}
			}
		} else {
			Log.e(TAG, "没有帐号数据");
		}

	}


	/**
	 * 自动登录
	 * */
	private void AutoLogin() {
		if (bIsAutoLogin && null!=mLoginCfg&& mLoginCfg.isVerified) {
			CaasOmpCfg.setString(CaasOmpCfg.EN_OMP_CFG_USER_NAME,
					mLastUserInfo.username);
			CaasOmpCfg.setString(CaasOmpCfg.EN_OMP_CFG_PASSWORD,
					mLastUserInfo.password);
			
		} 
		autoLogin();
		
	}

	/**
	 * 查看sp中是否有账号信息，有的话进行登录
	 */
	private void autoLogin() {
		String sp_username = SPUtils.getInstance(mcontext).getUserName();
		String sp_password = SPUtils.getInstance(mcontext).getPassWrod();
		if (!TextUtils.isEmpty(sp_username) && !TextUtils.isEmpty(sp_password)) {
			// 自动登录
			startLoginActivity1();
			//APIUtils.getInstance().Login(mcontext);
			finish();
		} else {
			et_mobile.requestFocus();
		}

	}

	private void startLoginActivity1() {
		startActivity(new Intent(mcontext, LoginActivity1.class));
	}

	private void login(UserInfo userInfo, LoginCfg loginCfg) {
		LoginApi.login(userInfo, loginCfg);
	}

	/**
	 * 登录广播
	 * */
	private BroadcastReceiver LoginStatusChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int old_status = intent.getIntExtra(LoginApi.PARAM_OLD_STATUS, -1);
			int new_status = intent.getIntExtra(LoginApi.PARAM_NEW_STATUS, -1);
			int reason = intent.getIntExtra(LoginApi.PARAM_REASON, -1);
			Log.e("登录", "old_status:" + old_status + ";new_status" + new_status
					+ ";reason" + reason);
			switch (new_status) {
			case LoginApi.STATUS_CONNECTED:// 连接成功
				if (Config.login_count == 0) {
			
						showDialog("登录成功");
						userLoginLog();
						// 保存密码 与其账号
						if (is_serLogin_success) {
							SPUtils.getInstance(mcontext)
									.setPassWrod(encodePass)
									.setUserName(
											et_mobile.getText().toString()
													.trim());
							is_serLogin_success = false;
						}
						intent = new Intent(mcontext, LoginActivity1.class);
						intent.putExtra("is_video_call", false);
						startActivity(intent);
					

					ClearDialog(true);

				}

				break;
			case LoginApi.STATUS_CONNECTING:// 正在连接中
				if (reason == LoginApi.REASON_NET_UNAVAILABLE) {
					showDialog("正在连接中。。。");
				}
			
				
				break;
			case LoginApi.STATUS_DISCONNECTED:// 连接不上服务器
				Log.e(TAG, mapReasonStringtoReasonCode(reason));
				showDialog("登录失败");
				ClearDialog();

				break;
			case LoginApi.STATUS_DISCONNECTING:// 断开连接中
				showDialog("断开连接中...");
				ClearDialog();
				break;
			case LoginApi.STATUS_IDLE:// 闲置
				showDialog("请稍等...");
				ClearDialog();
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 登录失败的原因
	 * */
	private String mapReasonStringtoReasonCode(int reason) {

		String reasonStr = null;
		switch (reason) {
		case LoginApi.REASON_AUTH_FAILED:// 鉴权失败，用户名或密码错误
			reasonStr = "auth failed";
			showDialog("登录失败，用户名或密码错误");
			ClearDialog();
			/**
			 * 清空sp配置信息
			 */
			SPUtils.getInstance(mcontext).clear();
			/**
			 * 清除本地通话记录数据库
			 */
			DbCore.getDaoSession().getCallRecordersDao().deleteAll();
			/**
			 * 清除本地白名单数据库
			 */
			DbCore.getDaoSession().getWhiteContanctDao().deleteAll();
			// startActivity(new Intent(this,LoginActivity.class));
			break;
		case LoginApi.REASON_CONNCET_ERR:// 连接错误
			reasonStr = "connect error";
			APIUtils.getInstance().Login(mcontext);
			break;
		case LoginApi.REASON_NET_UNAVAILABLE:// 没有网络
			reasonStr = "no network";
			showToast("当前网络不可用");
			break;
		case LoginApi.REASON_NULL:// 空
			reasonStr = "none";
			APIUtils.getInstance().Login(mcontext);
			break;
		case LoginApi.REASON_SERVER_BUSY:// 服务器繁忙
			reasonStr = "server busy";
			showToast("服务器繁忙，请稍后再试！！");
			APIUtils.getInstance().Login(mcontext);
			break;
		case LoginApi.REASON_SRV_FORCE_LOGOUT:// 强行注销
			reasonStr = "force logout";
			showToast("账号异地登录，被服务器强制下线");

			break;
		case LoginApi.REASON_USER_CANCEL:// 用户取消了
			reasonStr = "user canceled";
			break;
		case LoginApi.REASON_WRONG_LOCAL_TIME:// 当地时间错了
			reasonStr = "wrong local time";
			APIUtils.getInstance().Login(mcontext);
			break;
		case LoginApi.REASON_ACCESSTOKEN_INVALID:// 无效的访问令牌
			reasonStr = "invalid access token";
			APIUtils.getInstance().Login(mcontext);
			break;
		case LoginApi.REASON_ACCESSTOKEN_EXPIRED:// 访问令牌过期
			reasonStr = "access token expired";
			APIUtils.getInstance().Login(mcontext);
			break;
		case LoginApi.REASON_APPKEY_INVALID:// 无效的application 密钥
			reasonStr = "invalid application key";
			APIUtils.getInstance().Login(mcontext);
			break;
		case LoginApi.REASON_UNKNOWN:// 未知的
		default:
			APIUtils.getInstance().Login(mcontext);
			break;
		}
		return reasonStr;
	}

	/**
	 * 使用华为sdk登录
	 * 
	 * @param username
	 * @param pass
	 */
	private void Login(String username, String pass) {

		UserInfo userInfo = new UserInfo();

		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pass)) {
			return;
		}

		userInfo.username = Config.ACCOUNT_BEFORE + "8" + username;
		userInfo.password = pass;

		Log.e("username", userInfo.username);
		Log.e("password", userInfo.password);

		mLoginCfg = new LoginCfg();
		mLoginCfg.isAutoLogin = true;
		mLoginCfg.isVerified = true;
		mLoginCfg.isRememberPassword = true;
		login(userInfo, mLoginCfg);

	}

	/**
	 * 登录记录日志，用于后台登录
	 */
	private void userLoginLog() {
		String url = Config.SERVICE_URI + "/loginLog.do";
		String[] version = getVersionNameAndCode().split("=");
		JSONObject json = new JSONObject();
		try {
			json.put("userName", "8"+et_mobile.getText().toString());
			json.put("userType", "8");
			json.put("appVesionName", version[0]);// 版本名
			json.put("appVesionCode", version[1]);// 版本号
			json.put("appMotifyTime", Config.MOTIFY_TIME);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		OkHttpUtils.post(url).params("json", json.toString())
				.execute(new JsonCallBack<String>() {
					@Override
					public void onResponse(String t) {
						NetMessage netMessage = new NetMessage(t);
						String code = netMessage.getCode();
						if ("200".equals(code)) {
							Log.e("LoginActivity", "日志记录成功");
						} else {
							Log.e("LoginActivity", "日志记录失败");
						}
					}
				});
	}

	private long exitTime = 0;

	private Button btn_get_smsCode;
	private EditText et_mobile;
	private EditText et_code;
	private Button btn_confirm;
	private TextView tv_mobile_hint;
	private TextView tv_code_hint;

	// 获取版本名称
	private String getVersionNameAndCode() {
		try {
			PackageInfo packageInfo = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			// 获取版本code
			int versionCode = packageInfo.versionCode;
			String versionName = packageInfo.versionName;
			return versionName + "=" + versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			showToast("再按一次退出程序");
			exitTime = System.currentTimeMillis();
		} else {
			this.finish();
			System.exit(0);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(LoginStatusChangedReceiver);
		if (mypDialog != null) {

			mypDialog.cancel();
		}
	}

	/**
	 * dialog提示
	 * 
	 * @param string
	 */
	public void showDialog(String string) {
		if (mypDialog == null) {
			mypDialog = new ProgressDialog(this);
			// 实例化
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// 设置进度条风格，风格为圆形，旋转的
			// mypDialog.setTitle("");

			// 设置ProgressDialog 提示信息
			mypDialog.setIcon(R.drawable.ic_delete_c_press);
			// 设置ProgressDialog 标题图标

			mypDialog.setIndeterminate(false);
			// 设置ProgressDialog 的进度条是否不明确
			mypDialog.setCancelable(true);
		}
		// 设置消息
		mypDialog.setMessage(string);
		// 设置ProgressDialog 是否可以按退回按键取消
		mypDialog.show();
		// 让ProgressDialog显示
	}

	/**
	 * 隐藏dialog
	 */
	public void dialogCancel() {
		if (mypDialog != null) {
			mypDialog.cancel();
		}
	}

	/**
	 * 1.5秒后隐藏dialog
	 */
	public void ClearDialog(final boolean isFinsh) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if (mypDialog != null) {

					mypDialog.cancel();
				}
				if (isFinsh) {
					finish();
				}
			}
		};

		timer.schedule(task, 1500);
	}

	public void ClearDialog() {
		ClearDialog(false);
	}

	@Override
	public void OnClick(View v) {
		switch (v.getId()) {
		// 发送验证码按钮
		case R.id.btn_get_smsCode:
			getSmsCode();
			break;
		// 确定按钮(登录)
		case R.id.btn_confirm:

			serviceLogin();
			break;
		}

	}

	private String encodePass;
	private boolean is_serLogin_success;// 调用服务器接口登录返回200的标识

	/**
	 * 调用服务器接口登录
	 */
	private void serviceLogin() {
		
		if (!registVailta()) {
			return;
		}
		if (!smsCode_success) {
			return;
		}
		showDialog("登录中...");
		String url = Config.SERVICE_URI + "/sendMsg/login.do";
		JSONObject json = new JSONObject();
		try {
			json.put("username", et_mobile.getText().toString().trim());
			json.put("smsCode", et_code.getText().toString().trim());
			json.put("userType", "8");
		} catch (JSONException e1) {
			return;
		}
		OkHttpUtils.post(url).params("json", json.toString())
				.execute(new JsonCallBack<String>() {

					@Override
					public void onResponse(String t) {
						Logger.d(tag, "登录", t);
						NetMessage message = new NetMessage(t);
						String code = message.getCode();
						if (200==Integer.parseInt(code)) {
							encodePass = message.getData("encodePass");
							is_serLogin_success = true;
							if (!TextUtils.isEmpty(encodePass)) {
								try {
									// 解密码后的密码
									String pass = Encrypt3DES.getInstance()
											.decrypt(encodePass);
								
									// 调用华为的sdk登录
									Login(et_mobile.getText().toString().trim(),
											pass);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

						} else {
							showDialog("登录失败");
						
						}
						ClearDialog();
					}

					@Override
					public void onError(Call call, Response response,
							Exception e) {
						super.onError(call, response, e);
						if (!isNetConnected(mcontext)) {
							showDialog("网络出错");
							ClearDialog();
						}
						if (e != null) {
							Log.e(tag, e.getMessage());

						}

					}
				});
	}

	/**
	 * 请求网络 获取短信验证码
	 */
	protected void sendRequsetGetSmsCode() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("mobile", et_mobile.getText().toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String url = Config.SERVICE_URI + "/sendMsg/getCode.do";
		OkHttpUtils.post(url).params("json", jsonObject.toString())
				.execute(new JsonCallBack<String>() {
					@Override
					public void onResponse(String t) {
						NetMessage netMessage = new NetMessage(t);
						if ("200".equals(netMessage.getCode())) {
							et_code.requestFocus();
							UiUtils.showToast(mcontext, "验证码发送成功，请注意查收");
							
						} else {
							UiUtils.showToast(mcontext, "验证码发送失败");
						}
					}
				});
	}

	/**
	 * 信息验证
	 * 
	 * @return
	 */
	private boolean registVailta() {
		String mobile = et_mobile.getText().toString();
		if (isMobile(mobile)) {
			return true;
		} else {
			tv_mobile_hint.setVisibility(View.VISIBLE);
		}
		return false;
	}
}
