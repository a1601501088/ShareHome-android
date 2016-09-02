package com.vunken.tv_sharehome.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.color;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.activity.HomeActivity;
import com.vunken.tv_sharehome.adapter.BaseHolder;
import com.vunken.tv_sharehome.adapter.ContactHolder;
import com.vunken.tv_sharehome.adapter.DefaultAdapter;
import com.vunken.tv_sharehome.base.BaseFragment;
import com.vunken.tv_sharehome.greendao.dao.ContactDao.Properties;
import com.vunken.tv_sharehome.greendao.dao.bean.Contact;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.serv.AsnyContact;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.PhoneUtils;
import com.vunken.tv_sharehome.utils.UiUtils;

import de.greenrobot.dao.query.QueryBuilder;

public class ContactFragment extends BaseFragment implements
		OnFocusChangeListener, TextWatcher {
	private ListView lv_contact;
	private Button btn_add_contanct;
	private ContactAdapter adapter;
	private String TAG = "ContactFragment";
	// 上传与更新联系人的类
	private AsnyContact asnyContact = new AsnyContact();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 获取所有系统联系人。 ContactApi.LIST_FILTER_RCS：获取所有系统联系人中的 RCS 用户
		contactBeanList = ((HomeActivity) getActivity()).contacts;
		rootView = inflater.inflate(R.layout.fragment_contact, null);
		SharedPreferences sp = getActivity().getSharedPreferences(
				Config.SP_NAME, getActivity().MODE_PRIVATE);
		login_name = sp.getString(Config.LOGIN_USER_NAME, "");
		initViews();
		setViewState();
		// initRxObservable();
		initAnimation();
		initPopupWindow();
		initListener();
		return rootView;
	}

	private void initAnimation() {
		set = new AnimationSet(false);
		ScaleAnimation sa = new ScaleAnimation(0.5f, 1f, 0.5f, 1f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		sa.setDuration(300);
		AlphaAnimation aa = new AlphaAnimation(0.5f, 1f);
		aa.setDuration(300);
		set.addAnimation(aa);
		set.addAnimation(sa);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private EditText et_search_contact;
	public EditText et_username;
	private EditText et_moblie;
	private Button btn_save_contact;
	private View rootView;
	public RelativeLayout rl_add_contact_layout;
	private Button btn_cancel_contact;
	private List<Contact> contactBeanList;
	private int clickItemPositon = -1;// 当前点击的item position
	private int flag = -1;

	private void initListener() {
		btn_save_contact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				et_moblie.getText().toString();
				// 添加联系人
				saveContact();
			}
		});
		// 打开添加联系人面板
		btn_add_contanct.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rl_add_contact_layout.setVisibility(View.VISIBLE);
				et_username.requestFocus();
			}
		});
		// 取消添加联系人按钮
		btn_cancel_contact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				et_search_contact.requestFocus();
			}
		});
		et_username.setOnKeyListener(keyListener1);
		et_moblie.setOnKeyListener(keyListener1);
		btn_cancel_contact.setOnKeyListener(keyListener1);
		btn_add_contanct.setOnKeyListener(keyListener1);
		lv_contact.setOnKeyListener(keyListener1);
		btn_save_contact.setOnKeyListener(keyListener1);
		et_search_contact.setOnKeyListener(keyListener1);

		et_search_contact.setOnFocusChangeListener(this);
		et_search_contact.addTextChangedListener(this);

		lv_contact.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				clickItemPositon = position;
				contactBean = adapter.getItem(position);
				if (position == 0
						&& "我的手机".equals(contactBean.getContactName())) {
					/**
					 * 如果item是我的手机，直接拨打手机电话
					 */
					PhoneUtils.callVideo(getActivity(),
							contactBean.getHomePhone(), "9");
				} else {
					flag = 1;// 标记这是点击lv_contact的item
					showPopWind(parent, view, position, id);
				}
			}
		});

		// pop中的拨tv打视频通话
		ll_call_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PhoneUtils.callVideo(getActivity(), contactBean.getHomePhone(),
						"8");
				popDismiss();
			}
		});
		// pop中的拨打手机视频通话
		ll_call_phone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PhoneUtils.callVideo(getActivity(), contactBean.getHomePhone(),
						"9");
				popDismiss();
			}
		});
		// pop中的编辑
		ll_edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popDismiss();
				upadateContact();
			}
		});
		// pop中的删除
		ll_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popDismiss();
				deleteContact();
			}
		});

	}

	/**
	 * 删除联系人
	 */
	private List<Long> userIds = null;

	protected void deleteContact() {
		Long userId = contactBean.getUserId();
		DbCore.getDaoSession().getContactDao().deleteByKey(userId);
		if (contactBeanList.contains(contactBean)) {
			contactBeanList.remove(contactBean);
			adapter.setList(contactBeanList);
			et_search_contact.setText("");

			/**
			 * 传入删除的userId
			 */
			if (userIds == null) {
				userIds = new ArrayList<Long>();
			} else {
				userIds.clear();
			}

			userIds.add(userId);
			delectNetContact(userIds);
		}

	}

	/**
	 * 单个删除网络联系人
	 * 
	 */
	private void delectNetContact(List<Long> userIds) {
		String url = Config.SERVICE_URI + "/contact/deleteContactList.do";
		JSONObject json = new JSONObject();
		JSONArray jsonArray = new JSONArray();

		for (int i = 0; i < userIds.size(); i++) {
			jsonArray.put(userIds.get(i));
		}

		// jsonArray.put(jsonObject);

		try {
			json.put("userIds", jsonArray);
			json.put("userName", login_name + "");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Logger.d(TAG, "tv：删除联系人入参", json.toString());
		OkHttpUtils.post(url).params("json", json.toString())
				.execute(new JsonCallBack<String>() {

					@Override
					public void onResponse(String t) {
						Logger.d(TAG, "tv：删除联系人回参", t);
					}
				});

	}

	/**
	 * 修改联系人
	 */
	protected void upadateContact() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final AlertDialog dialog = builder.create();

		View v = View.inflate(getActivity(),
				R.layout.dialog_edit_contact_layout, null);
		final EditText et_username = (EditText) v
				.findViewById(R.id.et_username);
		final EditText et_account = (EditText) v.findViewById(R.id.et_account);

		final String contact_name = contactBean.getContactName();
		final String contact_account = contactBean.getHomePhone() + "";
		et_username.setHint(contact_name);
		et_account.setHint(contact_account);
		et_username.setText(contact_name);
		et_account.setText(contact_account);

		dialog.setView(v, 0, 0, 0, 0);
		dialog.setMessage("修改联系人");
		v.findViewById(R.id.btn_cancel).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		v.findViewById(R.id.btn_confrim).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						String username = et_username.getText().toString();
						String account = et_account.getText().toString();

						boolean isNameEmpty = TextUtils.isEmpty(username);
						boolean isAccountEmpty = TextUtils.isEmpty(account);
						if (isNameEmpty && isAccountEmpty) {
							dialog.dismiss();
						} else if (isNameEmpty) {
							UiUtils.showToast(getActivity(), "请输入姓名");
							return;
						} else if (isAccountEmpty) {
							UiUtils.showToast(getActivity(), "请输入账号");
							return;
						} else if (!isNameEmpty && !isAccountEmpty) {
							if (username.equals(contact_name)
									&& account.equals(contact_account)) {
								// 表示联系数据未修改
							} else {// 表示要联系人数据已修改
								contactBean.setContactName(username);
								contactBean.setHomePhone(account);
								refreshItemView(flag, username, account);
								DbCore.getDaoSession().getContactDao()
										.update(contactBean);
								/**
								 * 更新联系人同步到服务器
								 */
								if (contactBean.getUserId()!=0) {
									asnyContact.asynContact(getActivity(), contactBean.getUserId()+"", account,
											username);					
								}
								
							}

							dialog.dismiss();
						}
					}
				});
		dialog.show();
	}

	/**
	 * 修改联系人后修改listview的item
	 * 
	 * @param flag2
	 * @param account
	 *            联系人号码
	 * @param username
	 *            联系人姓名
	 */
	protected void refreshItemView(int flag, String username, String account) {
		if (clickItemPositon != -1) {
			if (flag == 1) {
				/**
				 * lvListview
				 */
				View childAt = lv_contact.getChildAt(clickItemPositon);
				TextView tv_name = (TextView) childAt
						.findViewById(R.id.tv_name);
				TextView tv_moblie = (TextView) childAt
						.findViewById(R.id.tv_moblie);
				tv_name.setText(username);
				tv_moblie.setText(account);

			} else if (flag == 2) {
				/**
				 * searhListView
				 */
			}
		}
	}

	/**
	 * 添加联系人
	 */
	protected void saveContact() {
		String contact_username = et_username.getText().toString();
		String home_phone = et_moblie.getText().toString();
		if (TextUtils.isEmpty(contact_username)) {
			UiUtils.showToast(getActivity(), "姓名为空");
			return;
		}
		if (TextUtils.isEmpty(home_phone)) {
			UiUtils.showToast(getActivity(), "号码为空");
			return;
		}

		Contact contact = new Contact();
		contact.setUserName(login_name);
		contact.setContactName(contact_username);
		contact.setHomePhone(home_phone);
		long insert = DbCore.getDaoSession().getContactDao().insert(contact);
		String userId = "";
		if (insert > 0) {// 添加成功
			/**
			 * 查出刚插入的数据为了那到userId
			 */
			QueryBuilder<Contact> where = DbCore
					.getDaoSession()
					.getContactDao()
					.queryBuilder()
					.where(Properties.UserName.eq(login_name),
							Properties.ContactName.eq(contact_username),
							Properties.HomePhone.eq(home_phone));
			List<Contact> list = where.list();

			if (list.size() > 0) {
				userId = list.get(0).getUserId() + "";
			}
			int index = 0;
			/**
			 * 判断list的每一条item是否为自己的手机号
			 * 
			 */

			int childCount = lv_contact.getChildCount();
			if (childCount > 0) {
				TextView tv_name = (TextView) lv_contact.getChildAt(0)
						.findViewById(R.id.tv_name);
				String name = tv_name.getText().toString();
				/**
				 * 表示为自己的手机号,因把数据插入自己的手机号之后
				 */
				if ("我的手机".equals(name)) {
					index = 1;
				}
			}

			/**
			 * 刷新listView
			 */
			contactBeanList.add(index, contact);
			adapter.setList(contactBeanList);
			UiUtils.showToast(getActivity(), "添加联系人成功");
			et_username.setText("");
			et_moblie.setText("");
			et_search_contact.setText("");
			et_username.requestFocus();
			/**
			 * 把联系人添加到服务器
			 * 
			 */
			if (!TextUtils.isEmpty(userId)) {
				asnyContact.asynContact(getActivity(), userId,home_phone,contact_username);
			}

		}
	}

	private Contact contactBean = new Contact();// 记住当前listview的item电话号码與name
	private PopupWindow popupWindow;
	private View popView;
	private LinearLayout ll_call_tv, ll_edit, ll_delete, ll_call_phone;

	private void initPopupWindow() {
		// 填充一个view对象让pop显示
		popView = View.inflate(getActivity(), R.layout.view_pop, null);
		ll_call_tv = (LinearLayout) popView.findViewById(R.id.ll_call_tv);
		ll_call_phone = (LinearLayout) popView.findViewById(R.id.ll_call_phone);
		ll_edit = (LinearLayout) popView.findViewById(R.id.ll_edit);
		ll_delete = (LinearLayout) popView.findViewById(R.id.ll_delete);

		// 创建 popupWindow -2 ：表示 popupWindow的大小使用View对象的包裹内容
		popupWindow = new PopupWindow(popView, -2, -2);
		// 设置popupWindow背景颜色，这样才能实现popupWindow的动画效果
		popupWindow.setBackgroundDrawable(new ColorDrawable(color.transparent));
		// 要使用pop有点击事件，就要设置如下属性
		popupWindow.setFocusable(true); // 使pop获取焦点
		popupWindow.setBackgroundDrawable(new BitmapDrawable()); // 设置背景
		popupWindow.setOutsideTouchable(true); // 触摸 ，

	}

	// PopupWindow
	private void showPopWind(AdapterView<?> parent, View clickV, int position,
			long id) {
		// [x,y]
		int[] location = new int[2];
		// 获取View在window的显示位置 [x,y]
		clickV.getLocationInWindow(location);
		popDismiss();
		// popupWindow的显示位置 parent : 父类，Gravity.LEFT+ Gravity.TOP：离屏幕左边，上方
		// location[1]表示y轴
		popupWindow.showAtLocation(parent, Gravity.LEFT + Gravity.TOP,
				UiUtils.dip2px(500), location[1] + UiUtils.dip2px(0));
		popView.startAnimation(set);
	}

	// 隐藏pop显示框
	private void popDismiss() {
		// 如果pop不为空并已经显示，就把关闭
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		String content = et_search_contact.getText().toString()
				.replaceAll("\\-|\\s", "");
		if (TextUtils.isEmpty(content)) {
			adapter.setList(contactBeanList);
		} else {
			/**
			 * 通过查找搜索内容 // User_name or Home_phone
			 */
			QueryBuilder<Contact> queryBuilder = DbCore.getDaoSession()
					.getContactDao().queryBuilder();
			// DbCore.getDaoMaster().getDatabase().query(table, columns,
			// selection, selectionArgs, groupBy, having, orderBy)
			QueryBuilder.LOG_SQL = true;
			QueryBuilder.LOG_VALUES = true;
			/**
			 * select * from contact where contact_name like '%q%' or home_phone
			 * like '%9%' 搜索contact_name 或 home_phone符合內容的聯系人
			 */
			queryBuilder.whereOr(
					Properties.ContactName.like("%" + content + "%"),
					Properties.HomePhone.like("%" + content + "%"));

			// queryBuilder.orderDesc(Properties.UserId);
			List<Contact> contactList = queryBuilder.build().list();

			for (int i = 0; i < contactList.size(); i++) {
				Log.e(TAG, contactList.get(i).toString());
			}
			adapter.setList(contactList);
		}

		lv_contact.setSelection(0);
	}

	private OnKeyListener keyListener1 = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (v.getId()) {
				case R.id.btn_cancel_contact:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						et_search_contact.requestFocus();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						btn_save_contact.requestFocus();
						return true;
					}
					break;
				case R.id.et_username:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						// et_search_contact.requestFocus();
						if (TextUtils.isEmpty(et_username.getText().toString()
								.trim())) {
							et_search_contact.requestFocus();
							return true;
						} else if (et_username.getSelectionStart() == 0) {
							return true;
						}
					}
					break;
				case R.id.et_moblie:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						// et_search_contact.requestFocus();
						if (TextUtils.isEmpty(et_moblie.getText().toString()
								.trim())) {
							et_search_contact.requestFocus();
							return true;
						} else if (et_moblie.getSelectionStart() == 0) {
							return true;
						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						btn_cancel_contact.requestFocus();
						return true;
					}
					break;
				case R.id.btn_add_contanct:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						et_search_contact.requestFocus();
						return true;
					}
					break;
				case R.id.lv_contact:

					if (keyCode == 21) {
						((HomeActivity) getActivity()).home_contact
								.requestFocus();
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						int select_item = lv_contact.getSelectedItemPosition();
						if (select_item == adapter.getCount() - 1) {
							lv_contact.setSelected(true);
							lv_contact.setSelection(0);
							return true;
						}
					}
					break;
				case R.id.btn_save_contact:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						btn_cancel_contact.requestFocus();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						btn_cancel_contact.requestFocus();
						return true;
					}
					break;
				case R.id.et_search_contact:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						if (TextUtils.isEmpty(et_search_contact.getText()
								.toString().trim())) {
							((HomeActivity) getActivity()).home_contact
									.requestFocus();
							return true;
						} else if (et_search_contact.getSelectionStart() == 0) {
							return true;
						}
						return false;

					}
					break;
				}
			}
			return false;

		}
	};

	private AnimationSet set;
	private String login_name;

	private void initViews() {
		lv_contact = (ListView) rootView.findViewById(R.id.lv_contact);
		lv_contact.setItemsCanFocus(true);// 设置item项的子控件能够获得焦点
		btn_add_contanct = (Button) rootView
				.findViewById(R.id.btn_add_contanct);
		et_search_contact = (EditText) rootView
				.findViewById(R.id.et_search_contact);
		et_username = (EditText) rootView.findViewById(R.id.et_username);
		et_moblie = (EditText) rootView.findViewById(R.id.et_moblie);
		btn_save_contact = (Button) rootView
				.findViewById(R.id.btn_save_contact);
		rl_add_contact_layout = (RelativeLayout) rootView
				.findViewById(R.id.rl_add_contact_layout);
		btn_cancel_contact = (Button) rootView
				.findViewById(R.id.btn_cancel_contact);
		adapter = new ContactAdapter(contactBeanList);
		lv_contact.setAdapter(adapter);
	}

	private void setViewState() {
		et_search_contact.setHintTextColor(getResources().getColor(
				R.color.white));
		et_username.setHintTextColor(getResources().getColor(R.color.white));
		et_moblie.setHintTextColor(getResources().getColor(R.color.white));

	}

	private class ContactAdapter extends DefaultAdapter<Contact> {

		protected ContactAdapter(List<Contact> list) {
			super(list);
		}

		@Override
		protected BaseHolder getHolder() {
			return new ContactHolder();
		}

	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.et_search_contact:
			if (hasFocus) {
				rl_add_contact_layout.setVisibility(View.INVISIBLE);
			}
			break;
		}

	}

	public void setFlag(boolean flag, String moblie2dial) {

		if (flag) {

			rl_add_contact_layout.setVisibility(View.VISIBLE);
			if (et_moblie != null) {
				et_moblie.setText(moblie2dial);
			}
			if (et_username != null) {
				et_username.requestFocus();
			}
			((HomeActivity) getActivity()).home_contact.setCompoundDrawables(
					null, ((HomeActivity) getActivity()).drawable_contact_two,
					null, null);
			flag = false;
		}
	}

}
