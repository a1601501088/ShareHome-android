package com.vunken.tv_sharehome.activity;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.base.BaseActivity;
import com.vunken.tv_sharehome.domain.RxBusBean;
import com.vunken.tv_sharehome.utils.NetMessage;
import com.vunken.tv_sharehome.utils.UiUtils;

public class RegisterActivity extends BaseActivity {

	private Button btn_get_smsCode, btn_next;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_register);
		initRxBus();
		initDatas();
		initViews();
		initViewDatas();
		initListener();

	}

	@Override
	protected void onResume() {
		super.onResume();
		rootView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		rootView.setVisibility(View.INVISIBLE);
	}

	private void initRxBus() {
		subscribe = RxBus.getInstance().toObservable(RxBusBean.class)
				.filter(new Func1<RxBusBean, Boolean>() {
					@Override
					public Boolean call(RxBusBean arg0) {
						return arg0.getKey() == Config.RXBUS_REGISTER_SUCCESS;
					}
				}).subscribe(new Action1<RxBusBean>() {
					@Override
					public void call(RxBusBean arg0) {
						finish();
					}
				});
	}

	private void initViewDatas() {
		tv_regster_title.setText(type == 2 ? "忘记密码" : "用户注册");
		UiUtils.setMaxLength(et_mobile, 12);
		UiUtils.setMaxLength(et_code, 6);

	}

	/**
	 * 信息验证
	 * 
	 * @return
	 */
	private boolean registVailta() {
		String mobile = et_mobile.getText().toString();
		if (mobile.matches("1[3|4|5|7|8|][0-9]{9}")) {
			return true;
		} else {
			tv_mobile_hint.setVisibility(View.VISIBLE);
		}
		return false;
	}

	private void initListener() {
		/**
		 * 验证信息是否正确
		 */
		btn_get_smsCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean vailta = registVailta();
				if (vailta) {
					getSmsCode();

				}
			}

		});
		/**
		 * 下一步
		 */
		btn_next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean registVailta = registVailta();
				if (registVailta && smsCode_success) {
					/**
					 * 带type 用来在RegisterActivity2页面判断 是注册还是修改密码
					 */
					Intent intent = new Intent(mcontext,
							RegisterActivity2.class);
					intent.putExtra("moblie", et_mobile.getText().toString());
					intent.putExtra("smsCode", et_code.getText().toString());
					intent.putExtra(Config.KEY_VALIDATE, type);
					startActivity(intent);
				}

			}
		});

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

		/**
		 * 手机号输入框
		 */
		et_mobile.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					tv_mobile_hint.setVisibility(View.INVISIBLE);
				} else {
					registVailta();
				}

			}
		});
	}

	/**
	 * 记录验证码是否正确
	 */
	private boolean smsCode_success = true;

	private void initDatas() {
		Intent data = getIntent();
		type = data.getIntExtra(Config.KEY_VALIDATE, -1);
	}

	private TextView tv_code_hint, tv_mobile_hint;

	private void initViews() {
		rootView = findViewById(R.id.rootView);
		btn_get_smsCode = (Button) findViewById(R.id.btn_get_smsCode);
		btn_next = (Button) findViewById(R.id.btn_next);
		et_code = (EditText) findViewById(R.id.et_code);
		et_mobile = (EditText) findViewById(R.id.et_mobile);
		tv_code_hint = (TextView) findViewById(R.id.tv_code_hint);
		tv_mobile_hint = (TextView) findViewById(R.id.tv_mobile_hint);
		tv_regster_title = (TextView) findViewById(R.id.tv_regster_title);

	}

	@Override
	public void OnClick(View v) {
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

	private String url = Config.SERVICE_URI + "/sendMsg/getCode.do";
	private EditText et_code;
	private EditText et_mobile;
	private int type;
	private TextView tv_regster_title;
	private Subscription subscribe;
	private View rootView;

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
		OkHttpUtils.post(url).params("json", jsonObject.toString())
				.execute(new JsonCallBack<String>() {
					@Override
					public void onResponse(String t) {

						// {"code":"200","message":"success"}
						NetMessage netMessage = new NetMessage(t);
					}
				});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (subscribe != null) {
			subscribe.unsubscribe();

		}
	}
}
