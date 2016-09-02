package com.vunken.tv_sharehome.fragment;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.huawei.rcs.login.LoginApi;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.activity.HomeActivity;
import com.vunken.tv_sharehome.adapter.BaseHolder;
import com.vunken.tv_sharehome.adapter.DefaultAdapter;
import com.vunken.tv_sharehome.adapter.WhiteHolder;
import com.vunken.tv_sharehome.base.BaseFragment;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.greendao.dao.bean.Contact;
import com.vunken.tv_sharehome.greendao.dao.bean.WhiteContanct;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.utils.DBUtils;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.NetMessage;
import com.vunken.tv_sharehome.utils.PhoneUtils;
import com.vunken.tv_sharehome.utils.SPUtils;
import com.vunken.tv_sharehome.utils.StringUtils;
import com.vunken.tv_sharehome.utils.UiUtils;
import com.vunken.tv_sharehome.utils.ZipCompressor;

public class SettingFragment extends BaseFragment implements
		OnFocusChangeListener, OnKeyListener, OnCheckedChangeListener {
	private String tag = "SettingFragment";
	private SharedPreferences sp;
	private ProgressDialog mProgressDialog;
	private Subscription subscribe1;
	private RelativeLayout rl_root;
	private View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
	 activity = getActivity();

		sp = getActivity().getSharedPreferences(Config.SP_NAME,
				getActivity().MODE_PRIVATE);
		rootView = inflater.inflate(R.layout.fragment_setting_root, null);
		initSettingHomeView();
		initShareFriendViews();
		initWhiteViews();
		initServiceList();// 未设计
		initLogResponse();// 问题反馈
		initDatas();
		initListener();
		return rootView;
	}

	private void initLogResponse() {
		rl_log_response = (RelativeLayout) rootView
				.findViewById(R.id.rl_log_response);
		rg_log_title = (RadioGroup) rl_log_response
				.findViewById(R.id.rg_log_title);
		et_log_content = (EditText) rl_log_response
				.findViewById(R.id.et_log_content);
		btn_submit_log = (Button) rl_log_response
				.findViewById(R.id.btn_submit_log);
		tv_logtime = (TextView) rl_log_response.findViewById(R.id.tv_logtime);

	}

	// 获取版本名称
	private String getVersionName() {
		try {
			PackageInfo packageInfo = getActivity().getPackageManager()
					.getPackageInfo(getActivity().getPackageName(), 0);
			// 获取版本code
			// int versionCode = packageInfo.versionCode;
			String versionName = packageInfo.versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void initDatas() {
		int white_color = getResources().getColor(R.color.white);
		et_white_contact_username.setHintTextColor(white_color);
		et_white_contact_account.setHintTextColor(white_color);

	}

	private void initServiceList() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity,
				R.style.MyDialogStyle);
		dialog = builder.create();
		View view = View.inflate(activity, R.layout.dialog_service_list, null);
		TextView tv_service_list = (TextView) view
				.findViewById(R.id.tv_service_list);
		String replace = tv_service_list.getText().toString()
				.replace("yyyy年MM月dd日", Config.MOTIFY_TIME);
		tv_service_list.setText(replace);
		tv_service_list.append("\n" + "版本号:" + getVersionName());
		btn_service_confirm = (Button) view
				.findViewById(R.id.btn_service_confirm);
		dialog.setView(view, 0, 0, 0, 0);

		// dialog.show();

	}

	private void initShareFriendViews() {
		rl_root_share_friend_layout = (RelativeLayout) rootView
				.findViewById(R.id.rl_root_share_friend_layout);
	}

	private boolean isShowCheckBox = false;// CheckBox是否显示状态

	private void initListener() {

		// 添加白名单联系人btn_add_white_contact_1
		btn_add_white_contact_1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rl_input_white_contact_list.setVisibility(View.VISIBLE);
				et_white_contact_username.requestFocus();
				showCheckBox(false);
			}
		});
		// 取消添加白名单联系人
		btn_white_contact_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btn_add_white_contact_1.requestFocus();
				rl_input_white_contact_list.setVisibility(View.INVISIBLE);
				/**
				 * 清空输入框内容
				 */
				et_white_contact_account.setText("");
				et_white_contact_username.setText("");
			}
		});
		// 打开删除联系人的CheckBox
		btn_delete_white_contact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showCheckBox(!isShowCheckBox);
				rl_input_white_contact_list.setVisibility(View.INVISIBLE);
			}
		});
		btn_service_list.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (dialog != null && !dialog.isShowing()) {
					dialog.show();
				}
			}
		});
		btn_service_confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
			}
		});
		/**
		 * 添加白名单的输入账号框添加内容改变监听，用来在输入账号时判断改账号是否在联系人中存在，存在就显示联系人姓名
		 */
		et_white_contact_account.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String account = et_white_contact_account.getText().toString();
				Contact contact = DBUtils.getInstance(activity)
						.getContactByPhone(account);
				/**
				 * 设置白名单姓名, 如果联系人数据库中没有此联系人,则为username框失去焦点时的内容
				 */
				String contactName = last_username;
				if (null != contact && null != contact.getContactName()) {
					contactName = contact.getContactName();
				}
				et_white_contact_username.setText(contactName);
			}
		});
		/**
		 * 上传日志
		 */
		btn_submit_log.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logSubmit();
			}

		});
		tv_logtime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setLogTime();
			}

		});
		// ---OnFocusChangeListener
		btn_share_friend.setOnFocusChangeListener(this);
		btn_show_white_list.setOnFocusChangeListener(this);
		btn_service_list.setOnFocusChangeListener(this);
		btn_logout.setOnFocusChangeListener(this);
		et_white_contact_username.setOnFocusChangeListener(this);
		btn_log_response.setOnFocusChangeListener(this);
		// ---OnKeyListener
		btn_share_friend.setOnKeyListener(keyListener1);
		btn_show_white_list.setOnKeyListener(keyListener1);
		btn_logout.setOnKeyListener(keyListener1);
		btn_service_list.setOnKeyListener(keyListener1);
		lv_white_list.setOnKeyListener(keyListener1);
		btn_add_white_contact_1.setOnKeyListener(keyListener1);
		btn_white_contact_cancel.setOnKeyListener(keyListener1);
		btn_white_contact_save.setOnKeyListener(keyListener1);
		et_white_contact_username.setOnKeyListener(keyListener1);
		et_white_contact_account.setOnKeyListener(keyListener1);
		btn_log_response.setOnKeyListener(keyListener1);
		rg_log_title.setOnCheckedChangeListener(this);

	}

	private OnKeyListener keyListener1 = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (v.getId()) {
				case R.id.btn_share_friend:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						((HomeActivity) getActivity()).more.requestFocus();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						btn_logout.requestFocus();
						return true;
					}
					break;
				case R.id.btn_show_white_list:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						((HomeActivity) getActivity()).more.requestFocus();
						return true;
					}
					break;
				case R.id.btn_service_list:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						((HomeActivity) getActivity()).more.requestFocus();
						return true;
					}
					break;
				case R.id.btn_logout:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						((HomeActivity) getActivity()).more.requestFocus();
						return true;
					}
					break;
				case R.id.btn_log_response:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						((HomeActivity) getActivity()).more.requestFocus();
						return true;
					}
					break;
				case R.id.lv_white_list:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						btn_show_white_list.requestFocus();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						int select_item = lv_white_list
								.getSelectedItemPosition();
						if (select_item == adapter.getCount() - 1) {
							lv_white_list.setSelection(0);
							return true;
						}
					}
					break;
				case R.id.btn_add_white_contact_1:
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						if (lv_white_list != null) {
							lv_white_list.setSelection(0);
						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						btn_show_white_list.requestFocus();
						return true;
					}
					break;
				case R.id.btn_white_contact_cancel:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						btn_add_white_contact_1.requestFocus();
						rl_input_white_contact_list
								.setVisibility(View.INVISIBLE);
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						btn_white_contact_save.requestFocus();
						return true;
					}
					break;
				case R.id.btn_white_contact_save:
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						btn_white_contact_cancel.requestFocus();
						return true;
					}
					break;
				case R.id.et_white_contact_username:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						if (et_white_contact_username.getSelectionStart() == 0) {
							String username = et_white_contact_username
									.getText().toString();

							if (TextUtils.isEmpty(username)) {
								btn_add_white_contact_1.requestFocus();
								rl_input_white_contact_list
										.setVisibility(View.INVISIBLE);

							}
							return true;
						}

					}
					break;
				case R.id.et_white_contact_account:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						if (et_white_contact_account.getSelectionStart() == 0) {
							String account = et_white_contact_account.getText()
									.toString();
							if (TextUtils.isEmpty(account)) {
								btn_add_white_contact_1.requestFocus();
								rl_input_white_contact_list
										.setVisibility(View.INVISIBLE);

							}
							return true;
						}
					}
					break;
				}
			}
			return false;
		}

	};

	// 显示与隐藏CheckBox
	private void showCheckBox(boolean b) {
		int count = lv_white_list.getCount();

		if (count > 0) {
			for (int i = 0; i < count; i++) {
				RelativeLayout rl_list_item_layout = (RelativeLayout) lv_white_list
						.getChildAt(i);
				if (rl_list_item_layout != null) {
					CheckBox cb_check = (CheckBox) rl_list_item_layout
							.findViewById(R.id.cb_check);
					cb_check.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
				}
			}
			// 隐藏时把checkBox的状态转为false
			if (b && last_position != -1) {
				View view = lv_white_list.getChildAt(last_position);
				if (view != null) {
					CheckBox cb_check = (CheckBox) view
							.findViewById(R.id.cb_check);
					cb_check.setChecked(false);
				}

			}
			isShowCheckBox = b;
		}

	}

	private void initSettingHomeView() {
		Button btn_account = (Button) rootView.findViewById(R.id.btn_account);
		btn_logout = (Button) rootView.findViewById(R.id.btn_logout);
		btn_log_response = (Button) rootView
				.findViewById(R.id.btn_log_response);
		btn_show_white_list = (Button) rootView
				.findViewById(R.id.btn_show_white_list);
		rl_root_white_view = (RelativeLayout) rootView
				.findViewById(R.id.rl_root_white_view);

		lv_white_list = (ListView) rootView.findViewById(R.id.lv_white_list);
		btn_service_list = (Button) rootView
				.findViewById(R.id.btn_service_list);
		btn_share_friend = (Button) rootView
				.findViewById(R.id.btn_share_friend);
		String account = sp.getString(Config.LOGIN_USER_NAME, "");
		if (TextUtils.isEmpty(account)) {
			account = "没有账号信息";
		}
		btn_account.setText(account);
		// btn_logout 退出登录
		btn_logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				logout();
			}
		});
		// btn_show_white_list 白名单列表
	}

	// 注销
	private void logout() {
		/**
		 * 清空sp配置信息
		 */
		SPUtils.getInstance(getActivity()).clear();
		/**
		 * 清除本地通话记录数据库
		 */
		DbCore.getDaoSession().getCallRecordersDao().deleteAll();
		/**
		 * 清除本地白名单数据库
		 */
		DbCore.getDaoSession().getWhiteContanctDao().deleteAll();
		/**
		 * 清除本地联系人数据库
		 */
		DbCore.getDaoSession().getContactDao().deleteAll();
		showProgressDialog();
		LoginApi.logout();
		subscribe1 = Observable.timer(2, TimeUnit.SECONDS).subscribe(
				new Action1<Long>() {
					@Override
					public void call(Long arg0) {
						if (mProgressDialog != null) {
							mProgressDialog.dismiss();

						}
						HuaweiSDKApplication.exitApp();
					}
				});
	}

	private ListView lv_white_list;
	private Button btn_show_white_list;
	private RelativeLayout rl_root_white_view;
	private RelativeLayout rl_root_share_friend_layout;
	private Button btn_share_friend;
	private RelativeLayout rl_input_white_contact_list;
	private Button btn_add_white_contact_1;
	private Button btn_white_contact_save;
	private EditText et_white_contact_account;
	private EditText et_white_contact_username;
	private Button btn_white_contact_cancel;
	private Button btn_delete_white_contact;
	private WhiteAdapter adapter;

	private void showProgressDialog() {
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.show();
		mProgressDialog.setMessage("正在注销....");
	}

	private int last_position = -1;

	private void initWhiteViews() {
		// 白名单页面添加联系人的布局
		rl_input_white_contact_list = (RelativeLayout) rootView
				.findViewById(R.id.rl_input_white_contact_list);
		// 打开添加联系人布局
		btn_add_white_contact_1 = (Button) rootView
				.findViewById(R.id.btn_add_white_contact_1);
		btn_delete_white_contact = (Button) rootView
				.findViewById(R.id.btn_delete_white_contact);
		// 添加联系人
		btn_white_contact_save = (Button) rootView
				.findViewById(R.id.btn_white_contact_save);
		et_white_contact_account = (EditText) rootView
				.findViewById(R.id.et_white_contact_account);
		et_white_contact_username = (EditText) rootView
				.findViewById(R.id.et_white_contact_username);

		tv_hint_label = (TextView) rootView.findViewById(R.id.tv_hint_label);
		// 取消添加联系人
		btn_white_contact_cancel = (Button) rootView
				.findViewById(R.id.btn_white_contact_cancel);
		// -------------------
		lv_white_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, View view,
					final int position, long id) {
				if (isShowCheckBox) {// 表示已经打开删除的CheckBox
					if (last_position != -1 && last_position != position) {
						RelativeLayout rl_item = (RelativeLayout) lv_white_list
								.getChildAt(last_position);
						if (rl_item != null) {
							CheckBox last_CheckBox = (CheckBox) rl_item
									.findViewById(R.id.cb_check);
							last_CheckBox.setChecked(false);
						}

					}

					CheckBox cb_check = (CheckBox) view
							.findViewById(R.id.cb_check);
					// UiUtils.showToast(activity, cb_check.isChecked() + "");
					if (cb_check.isChecked()) {
						// TODO 删除这一item
						showDeleteWhiteContactDialog(parent, position);
					} else {
						cb_check.setChecked(true);
					}
					last_position = position;
				}
			}

		});

		// 添加白名单联系人
		btn_white_contact_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (adapter == null) {
					adapter = new WhiteAdapter(null);
				}
				int count2 = adapter.getCount();
				if (count2 >= 6) {
					UiUtils.showToast(activity, "白单名最多为6个");
					return;
				} else {
					String account = et_white_contact_account.getText()
							.toString();
					for (int i = 0; i < count2; i++) {
						WhiteContanct whiteContanct = adapter.getItem(i);
						if (account.equals(whiteContanct.getHomePhone())) {
							UiUtils.showToast(activity, "号码已存在");
							return;
						}

					}
					// 保存联系人
					long count = saveWhiteContanct(account);
					Log.e("count", count + "");
					if (count != -1) {
						// UiUtils.showToast(UiUtils.getContext(), "联系人添加成功");
						et_white_contact_username.requestFocus();
						et_white_contact_username.setText("");
						et_white_contact_account.setText("");
						// rootView.setVisibility(View.VISIBLE);
						// 查找出所有白名单
						queryAllWhiteContanct();
					}
				}
			}
		});
		// queryAllWhiteContanct();
	}

	// 删除白名单联系人确认框
	private void showDeleteWhiteContactDialog(final AdapterView<?> parent,
			final int position) {
		new AlertDialog.Builder(activity).setTitle("提示")
				.setMessage("确定要删除此白名单联系人吗?")
				.setNegativeButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						WhiteContanct whiteContanct = (WhiteContanct) parent
								.getItemAtPosition(position);
						if (whiteContanct == null) {
							return;
						}
						final long whiteId = whiteContanct.getWhiteId();
						// UiUtils.showToast(activity, moblie);

						Observable
								.just(1)
								.map(new Func1<Integer, Boolean>() {
									@Override
									public Boolean call(Integer arg0) {
										return DBUtils.getInstance(activity)
												.deleteWhiteContact(whiteId);
									}

								}).subscribeOn(Schedulers.io())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(new Subscriber<Boolean>() {

									@Override
									public void onCompleted() {
										this.unsubscribe();
									}

									@Override
									public void onError(Throwable arg0) {
										this.unsubscribe();
									}

									@Override
									public void onNext(Boolean b) {
										if (b) {// 删除成功
											btn_delete_white_contact
													.requestFocus();
											last_position = -1;
											isShowCheckBox = false;
											queryAllWhiteContanct();
										} else {
											// 删除失败
										}
									}

								});

					}
				}).setPositiveButton("取消", null).show();
	}

	private void queryAllWhiteContanct() {
		Observable
				.just(1)
				.map(new Func1<Integer, List<WhiteContanct>>() {
					@Override
					public List<WhiteContanct> call(Integer arg0) {
						return DBUtils.getInstance(
								HuaweiSDKApplication.getApplication())
								.getAllWhiteContanct();
					}
				}).filter(new Func1<List<WhiteContanct>, Boolean>() {
					@Override
					public Boolean call(List<WhiteContanct> arg0) {
						return arg0 != null;
					}
				}).subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<List<WhiteContanct>>() {
					@Override
					public void onCompleted() {
						this.unsubscribe();
					}

					@Override
					public void onError(Throwable arg0) {
						this.unsubscribe();
					}

					@Override
					public void onNext(List<WhiteContanct> contancts) {
						if (contancts.size() == 0) {
							lv_white_list.setVisibility(View.INVISIBLE);
							tv_hint_label.setVisibility(View.VISIBLE);
							if (isShowCheckBox) {
								isShowCheckBox = false;
							}
						} else {
							setListAdapter(contancts);
							lv_white_list.setVisibility(View.VISIBLE);
							tv_hint_label.setVisibility(View.INVISIBLE);
						}
					}
				});
	}

	// 给listview设置数据
	private void setListAdapter(final List<WhiteContanct> contancts) {

		adapter = new WhiteAdapter(contancts);
		lv_white_list.setAdapter(adapter);

	}

	private WhiteHolder holder;
	private TextView tv_hint_label;
	private Button btn_service_list;
	private Button btn_logout;
	private Button btn_service_confirm;
	private AlertDialog dialog;

	private class WhiteAdapter extends DefaultAdapter<WhiteContanct> {
		protected WhiteAdapter(List<WhiteContanct> list) {
			super(list);
		}

		@Override
		protected BaseHolder getHolder() {
			SettingFragment.this.holder = new WhiteHolder();
			return holder;
		}

	}

	// 保存白名单联xiren
	private long saveWhiteContanct(String moblie) {
		String username = et_white_contact_username.getText().toString();
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(moblie)) {
			return -1;
		} else if (!PhoneUtils.isMobile(moblie)) {
			UiUtils.showToast(activity, "请输入正确的账号");
			return -1;
		}
		/**
		 * 清空记录 last_username
		 */
		last_username = "";
		return DBUtils.getInstance(activity).insertWhiteContanct(username,
				moblie, new Date(), "");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (subscribe1 != null)
			subscribe1.unsubscribe();
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			switch (v.getId()) {
			case R.id.btn_share_friend:
				showSettingItemView(1);
				break;
			case R.id.btn_show_white_list:
				queryAllWhiteContanct();// 找出所有白名单
				showSettingItemView(2);
				// 只要焦点在白名单上，就隐藏添加白名单的添加联系人布局
				rl_input_white_contact_list.setVisibility(View.INVISIBLE);
				showCheckBox(false);// 隐藏白名单条目的CheckBox
				break;
			case R.id.btn_service_list:
				showSettingItemView(0);
				break;
			case R.id.btn_log_response:
				showSettingItemView(4);
				initLogTime();
				break;
			case R.id.btn_logout:
				showSettingItemView(0);
				break;
			}
		} else {
			if (v.getId() == R.id.et_white_contact_username) {
				last_username = et_white_contact_username.getText().toString();
			}
		}
	}

	/**
	 * 用于记录添加白名单username 失去焦点时的 内容
	 */
	private String last_username = "";
	private Button btn_log_response;
	private RelativeLayout rl_log_response;
	private RadioGroup rg_log_title;
	private EditText et_log_content;
	private Button btn_submit_log;

	private void showSettingItemView(int item) {
		rl_root_share_friend_layout.setVisibility(item == 1 ? View.VISIBLE
				: View.INVISIBLE);
		rl_root_white_view.setVisibility(item == 2 ? View.VISIBLE
				: View.INVISIBLE);

		rl_log_response
				.setVisibility(item == 4 ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		return false;
	}

	private boolean is_submit;
	private String log_title;

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		is_submit = true;
		btn_submit_log.setText("提交");
		RadioButton rb = (RadioButton) group.findViewById(checkedId);
		log_title = rb.getText().toString().trim();
	}

	private void logSubmit() {
		if (is_submit) {
			Observable.just(1).map(new Func1<Integer, Integer>() {
				@Override
				public Integer call(Integer arg0) {
					zip();
					return arg0;
				}

			}).subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Subscriber<Integer>() {
						@Override
						public void onCompleted() {
							upload_log();
							this.unsubscribe();
						}

						@Override
						public void onError(Throwable arg0) {
							Logger.d(
									tag,
									"日志上传",
									null != arg0.getMessage() ? arg0
											.getMessage() : "null");
							this.unsubscribe();
						}

						@Override
						public void onNext(Integer arg0) {
						}
					});
		}

	}

	/**
	 * 上传打包好的日志到服务器
	 */
	private void upload_log() {
		String url = Config.SERVICE_URI + "/uploadFile.do";
		OkHttpUtils
				.post(url)
				.params("userName",
						"8" + SPUtils.getInstance(activity).getUserName())
				.params("txt", zip).execute(new JsonCallBack<String>() {
					@Override
					public void onResponse(String t) {
						Logger.d("/uploadFile.do", "日志上传接口", "onResponse-->"+t);
						String code = new NetMessage(t).getCode();
						if ("200".equals(code)) {
							// deleteLog();
							if (zip != null)
								zip.delete();
							UiUtils.showToast(activity, "问题反馈成功,我们会尽快处理。");
						} else {
							UiUtils.showToast(activity, "问题反馈失败");
						}
					}
					@Override
					public void onError(Call call, Response response,
							Exception e) {
						super.onError(call, response, e);
					
						
						Logger.d("/uploadFile.do", "日志上传接口", "onError->"+e.getMessage());
					}
				});
	}

	/**
	 * 上传成功后，删除日志
	 */
	private void deleteLog() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (zip != null)
					zip.delete();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						if (!files[i].exists()) {
							continue;
						}
						if (files[i].isDirectory()) {
							int size = files[i].listFiles().length;
							File[] list = files[i].listFiles();
							for (int j = 0; j < size; j++) {

								list[j].delete();
							}

						}
						files[i].delete();

					}
				}
			}
		}).start();

	}

	private File zip;
	private File[] files;
	private TextView tv_logtime;

	private void zip() {
		/*
		 * 把内容保存到文件
		 */
		String content = "问题:" + log_title + ";内容:"
				+ et_log_content.getText().toString().trim() + ";问题发生时间:"
				+ tv_logtime.getText().toString().trim();
		StringUtils.saveAsFileWriter(content, Logger.PATH_LOGCAT
				+ File.separator + "question_des.txt");
		/*
		 * 打包压缩
		 */

		String sdCard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
		sdCard_path =sdCard_path.replace("/sdcard", "");//mnt目录
		files = new File[] { new File(Logger.PATH_LOGCAT),
				new File(sdCard_path+ "/demolog"), new File(sdCard_path+"/hrslog"),
				new File(getActivity().getFilesDir(),"hrslog"),
				new File(sdCard_path+"/hme_v_log_all.txt"),
				new File(sdCard_path+"/hme_v_log_api.txt"),
				new File(sdCard_path+"/hme_v_log_error.txt"),
				new File(sdCard_path+"/hme_v_log_io.txt"),
				new File(sdCard_path+"/hme_v_log_trace.txt") };
		zip = new File(sdCard_path+"/tv_logs.zip");
		Log.e("path","filepath"+ zip.getAbsolutePath());
		ZipCompressor zipCompressor = new ZipCompressor();
		try {
			zipCompressor.ZipFiles(zip, "", files);
		} catch (IOException e) {
			e.printStackTrace();
			
		}
	}

	private String logtime;
	private TimePickerDialog timePickerDialog;
	private DatePickerDialog datePickerDialog;
	int f = 0;

	/**
	 * 设置故障发生的时间
	 */
	private void setLogTime() {

		if (datePickerDialog == null) {
			datePickerDialog = new DatePickerDialog(activity,
					DatePickerDialog.THEME_TRADITIONAL,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							logtime = String.format("%d/%02d/%02d", year,
									monthOfYear, dayOfMonth);
							datePickerDialog.cancel();
							if (timePickerDialog == null) {

								timePickerDialog = new TimePickerDialog(
										activity,
										new TimePickerDialog.OnTimeSetListener() {
											@Override
											public void onTimeSet(
													TimePicker view,
													int hourOfDay, int minute) {
												++f;
												if (f == 1) {
													logtime = logtime
															+ String.format(
																	" %02d:%02d",
																	hourOfDay,
																	minute);
													tv_logtime.setText(logtime);

												}

												timePickerDialog.cancel();
												logtime = "";
												f = 0;
											}
										}, hour, minute, true);

							}
							timePickerDialog.show();
						}
					}, year, month, date);
			DatePicker datePicker = datePickerDialog.getDatePicker();
			datePicker.setCalendarViewShown(false);
		}
		datePickerDialog.show();
	}

	private int year;
	private int month;
	private int date;
	private int hour;
	private int minute;
	private Activity activity;

	/**
	 * 初始化log时间
	 */
	private void initLogTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, +1);
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		date = calendar.get(Calendar.DATE);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		tv_logtime.setText(String.format("%d/%02d/%02d %02d:%02d", year, month,
				date, hour, minute));
	}
}
