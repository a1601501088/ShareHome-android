package com.vunken.tv_sharehome.activity;

import okhttp3.Call;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
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
import com.vunken.tv_sharehome.utils.CommonUtil;
import com.vunken.tv_sharehome.utils.NetMessage;
import com.vunken.tv_sharehome.utils.UiUtils;

public class RegisterActivity2 extends BaseActivity {

	private Button btn_register;

	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_register2);
		initDatas();
		initViews();

		initVeiwDatas();
		initListener();

	}

	private void initVeiwDatas() {
		tv_regster_title.setText(type == 2 ? "设置新密码" : "设置密码");
		btn_register.setText(type == 2?"确定" : "立即注册");
	}

	/**
	 * 信息验证
	 * 
	 * @return
	 */
	private boolean registVailta() {

		String password = et_password.getText().toString();
		if (password.length() >= 6) {
			/**
			 * 密码格式正确
			 */
			if (regEx(password)) {
				String password2 = et_password2.getText().toString();
				if (password.equals(password2)) {

					return true;
				} else {
					tv_password2_hint.setVisibility(View.VISIBLE);
				}
			}
			/**
			 * 密码格式错误
			 */
			else {
				tv_password_hint.setText("密码格式错误");
				tv_password_hint.setVisibility(View.VISIBLE);
			}

		} else {
			tv_password_hint.setText("密码长度小于6位");
			tv_password_hint.setVisibility(View.VISIBLE);
		}

		return false;
	}

	/**
	 * 密码大于两种类型
	 * 
	 * @param pass
	 * @return true 表示密码符合
	 */
	private boolean regEx(String pass) {
		String regEx1 = "^[`~!@#$%^&*()+=|{}':;',.<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]{1,}$";
		String regEx2 = "^[0-9]{1,}$";
		String regEx3 = "^[a-z]{1,}$";
		String regEx4 = "^[A-Z]{1,}$";
		int i = 0;
		if (pass.matches(regEx1))
			i++;
		if (pass.matches(regEx2))
			i++;
		if (pass.matches(regEx3))
			i++;
		if (pass.matches(regEx4))
			i++;
		return i == 0;
	}

	private void initListener() {
		/**
		 * 注册按钮
		 */
		btn_register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean vailta = registVailta();
				if (vailta) {
					try {
						registerOrUpdatePass(moblie, smsCode);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
		/**
		 * 密码不能少于6位
		 */
		et_password.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					registVailta();
				} else {
					tv_password_hint.setVisibility(View.INVISIBLE);
				}

			}
		});

		/**
		 * 判断两次密码是否一至
		 */
		et_password2.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					registVailta();
				} else {
					tv_password2_hint.setVisibility(View.INVISIBLE);
				}

			}
		});

	}

	/**
	 * 是否显示提示进度框
	 * 
	 * @param b
	 */
	private ProgressDialog dialog;

	private void showProgressDialog() {
			dialog = new ProgressDialog(this);
			dialog.setMessage("请稍等...");
			dialog.show();
		
	}

	/**
	 * 请求网络 注册 与修改密码
	 * 
	 * @throws JSONException
	 */
	protected void registerOrUpdatePass(String moblie, String smsCode)
			throws JSONException {
		/**
		 * 显示"请稍后框"
		 */
		showProgressDialog();
		
		// 修改密码
		if (type == 2) {
			updatePass(moblie, smsCode);
		}
		// 注册
		else if (type == 1) {
			register(moblie, smsCode);

		} else {
			UiUtils.showToast(mcontext, "动作类型错误");
		}
	}

	/**
	 * 
	 * @param moblie
	 *            手机号
	 * @param smsCode
	 *            短信验证码
	 * @throws JSONException 
	 */
	private void updatePass(String moblie, String smsCode) throws JSONException {
		String url = Config.SERVICE_URI + "/sendMsg/updatePass.do";
		JSONObject json = new JSONObject();
			json.put("smsCode", smsCode);
			json.put("userType", "8");
			json.put("username", moblie);
			json.put("password", et_password.getText().toString().trim());
	
		OkHttpUtils.post(url).params("json", json.toString())
				.execute(new JsonCallBack<String>() {
					@Override
					public void onError(Call call, Response response,
							Exception e) {
						super.onError(call, response, e);
						if (dialog!=null) {
							dialog.dismiss(); 
						}
						UiUtils.showToast(mcontext, "修改密码失败");
					}
					@Override
					public void onResponse(String t) {
						if (dialog!=null) {
							dialog.dismiss();
						}
						NetMessage msg = new NetMessage(t);
						String code = msg.getCode();
						if ("200".equals(code)) {
							String msg2 = "你的密码修改成功,请用8加手机号登录";
							showSuccessDialog(msg2);
						} else {
							String message = msg.getMessage();
							Log.e("修改密码", message);
							UiUtils.showToast(mcontext, "修改密码失败");
						}
					}
				});
	}

	/**
	 * 注册
	 * 
	 * @param moblie
	 * @param smsCode
	 * @throws JSONException
	 */
	private void register(String moblie, String smsCode) throws JSONException {
		
		String url = Config.SERVICE_URI + "/sendMsg/signup.do";
		if (!CommonUtil.isNetConnected(this)) {
			UiUtils.showToast(mcontext, "网络错误,请检查你的网络");
		} else {
			JSONObject json = new JSONObject();
			json.put("smsCode", smsCode);
			json.put("userType", "8");
			json.put("username", moblie);
			json.put("password", et_password.getText().toString().trim());
			OkHttpUtils.post(url).params("json", json.toString())
					.execute(new JsonCallBack<String>() {
						@Override
						public void onError(Call call, Response response,
								Exception e) {
							super.onError(call, response, e);
							if (dialog!=null) {
								dialog.dismiss(); 
							}
							UiUtils.showToast(mcontext, "注册失败");
						}
						@Override
						public void onResponse(String t) {
							if (dialog!=null) {
								dialog.dismiss(); 
							}
							try {
								JSONObject jsonObject = new JSONObject(t);
								String code = jsonObject.getString("code");
								String data = jsonObject.getString("data");
								
								if ("200".equals(code)) {
									String msg = data;
									showSuccessDialog(msg);
								}
								else {
									String imsCode = "";
									boolean imsCode_has = jsonObject.has("imsCode");
									if (imsCode_has) {
										imsCode = jsonObject.getString("imsCode");
									}
									if (imsCode.contains("网元命令执行失败")) {
										UiUtils.showToast(mcontext, "用户已经存在");
									}else {
										UiUtils.showToast(mcontext, "注册失败");
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}
	}

	/**
	 * 弹出注册成功提示框，告诉用户 账号
	 *  msg : 提示信息
	 */
	private void showSuccessDialog(String msg) {
		
		
		AlertDialog.Builder builder = new Builder(mcontext, android.R.style.Theme_Holo_Light_Dialog);
		builder.setTitle("提示");
		builder.setMessage(msg);
		
		builder.setPositiveButton("我知道了",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						{
							RxBusBean rxBusBean = new RxBusBean();
							rxBusBean.setKey(Config.RXBUS_REGISTER_SUCCESS);
							rxBusBean.setUsername("8" + moblie);
							rxBusBean.setPass("" + et_password.getText().toString());
							sendRxBus(rxBusBean);
							dialog.dismiss();
							finish();
						}
					}
				});
		
		AlertDialog 	 mBackPressedDialog = builder.create();
//		mBackPressedDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//		mBackPressedDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mBackPressedDialog.setCancelable(false);
		mBackPressedDialog.show();
		
	}

	/**
	 * 把数据发送到登录页面
	 * 
	 * @param o
	 */
	private void sendRxBus(Object o) {
		RxBus.getInstance().post(o);
	}

	private void initDatas() {
		Intent data = getIntent();
		type = data.getIntExtra(Config.KEY_VALIDATE, -1);
		moblie = data.getStringExtra("moblie");
		smsCode = data.getStringExtra("smsCode");
	}

	private TextView tv_password_hint, tv_password2_hint;

	private void initViews() {
		
		btn_register = (Button) findViewById(R.id.btn_register);
		et_password = (EditText) findViewById(R.id.et_password);
		et_password2 = (EditText) findViewById(R.id.et_password2);

		tv_password_hint = (TextView) findViewById(R.id.tv_password_hint);
		tv_password2_hint = (TextView) findViewById(R.id.tv_password2_hint);
		tv_regster_title = (TextView) findViewById(R.id.tv_regster_title);
	}

	@Override
	public void OnClick(View v) {
	}

	private EditText et_password;
	private EditText et_password2;
	private TextView tv_regster_title;
	private int type;
	private String moblie;
	private String smsCode;
}
