package com.vunken.tv_sharehome.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.RxBus;
import com.vunken.tv_sharehome.activity.HomeActivity;
import com.vunken.tv_sharehome.base.BaseFragment;
import com.vunken.tv_sharehome.domain.ContactNumBean;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.PhoneUtils;
import com.vunken.tv_sharehome.utils.StringUtils;
import com.vunken.tv_sharehome.utils.UiUtils;

/**
 * 设置点击监听事件
 * */
public class AttnFragment extends BaseFragment {
	private ImageButton Clear, one, two, three, four, five, six, seven, eight,
			nine, zero, xin, jin;
	public ImageButton ib_add_contact;
	private EditText Number; // 电话号码
	private String PhoneNumber = "";
	private ImageButton home_call;
	private Intent intent;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_attn, null);
		init(view);
		setViewData();
		initListener();
		return view;
	}

	private void setViewData() {
		/**
		 * 设置editext的字体大小
		 */
		// 新建一个可以添加属性的文本对象
		SpannableString ss = new SpannableString("请输入手机号");
		// 新建一个属性对象,设置文字的大小
		AbsoluteSizeSpan ass = new AbsoluteSizeSpan(UiUtils.dip2px(23), true);
		// 附加属性到文本
		ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		// 设置hint
		Number.setHint(new SpannedString(ss)); // 一定要进行转换,否则属性会消失

		Number.setHintTextColor(getResources().getColor(R.color.white));
	}

	private void initListener() {
		// Number.setOnKeyListener(keyListener);
		Clear.setOnKeyListener(keyListener);
		one.setOnKeyListener(keyListener);
		two.setOnKeyListener(keyListener);
		three.setOnKeyListener(keyListener);
		four.setOnKeyListener(keyListener);
		five.setOnKeyListener(keyListener);
		six.setOnKeyListener(keyListener);
		seven.setOnKeyListener(keyListener);
		eight.setOnKeyListener(keyListener);
		nine.setOnKeyListener(keyListener);
		zero.setOnKeyListener(keyListener);
		xin.setOnKeyListener(keyListener);
		jin.setOnKeyListener(keyListener);
		home_call.setOnKeyListener(keyListener);
		ib_add_contact.setOnKeyListener(keyListener);
		home_call_mobile.setOnKeyListener(keyListener);

		home_call_mobile.setOnClickListener(this);
		Number.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					if (TextUtils.isEmpty(Number.getText().toString().trim())) {
						v.requestFocus();
						return true;
					} else if (Number.getSelectionStart() == 0) {
						return true;
					}
					return false;
				}
				return false;
			}
		});
		/**
		 * 隐藏键盘
		 */
		Number.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(getActivity().INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(Number.getWindowToken(), 0);
			}
		});

		contactNumBean = new ContactNumBean();
		ib_add_contact.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String moblie = Number.getText().toString();
				if (TextUtils.isEmpty(moblie)) {
					UiUtils.showToast(getActivity(), "" + "号码为空");
					return;
				} else if (!StringUtils.isNumber(moblie)) {
					UiUtils.showToast(getActivity(), "" + "只能为数字");
					return;
				}
				contactNumBean.setTag(200);
				contactNumBean.setMoblie(moblie);
				RxBus.getInstance().post(contactNumBean);
				Number.setText("");
			}
		});

	}

	private OnKeyListener keyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (keyCode >= 7 && keyCode <= 16) {
					Number.append((keyCode - 7) + "");
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_STAR) {
					String string = Number.getText().toString();
					if (!TextUtils.isEmpty(string)) {
						String substring = string.substring(0,
								string.length() - 1);
						Number.setText(substring);
						return true;
					}

				} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					switch (v.getId()) {
					case R.id.attn_phone:
					case R.id.attn_1:
					case R.id.attn_4:
					case R.id.attn_7:
					case R.id.attn_xin:
						((HomeActivity) getActivity()).dial.requestFocus();
						return true;

					}
				}else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					switch (v.getId()) {
					case R.id.attn_jin:
						three.requestFocus();
						return true;
					case R.id.attn_0:
						two.requestFocus();
						return true;
					case R.id.attn_xin:
						one.requestFocus();
						return true;
					case R.id.home_call_mobile:
						home_call.requestFocus();
						return true;

					}
				}
			}

			return false;
		}
	};
	private ContactNumBean contactNumBean;
	private ImageButton home_call_mobile;

	private void init(View view) {
		Clear = (ImageButton) view.findViewById(R.id.attn_clear);// 清除号码
		one = (ImageButton) view.findViewById(R.id.attn_1);// 拨号键盘 1
		two = (ImageButton) view.findViewById(R.id.attn_2);// 拨号键盘 2
		three = (ImageButton) view.findViewById(R.id.attn_3);// 拨号键盘 3
		four = (ImageButton) view.findViewById(R.id.attn_4);// 拨号键盘 4
		five = (ImageButton) view.findViewById(R.id.attn_5);// 拨号键盘 5
		six = (ImageButton) view.findViewById(R.id.attn_6);// 拨号键盘 6
		seven = (ImageButton) view.findViewById(R.id.attn_7);// 拨号键盘 7
		eight = (ImageButton) view.findViewById(R.id.attn_8);// 拨号键盘 8
		nine = (ImageButton) view.findViewById(R.id.attn_9);// 拨号键盘 9
		zero = (ImageButton) view.findViewById(R.id.attn_0);// 拨号键盘 0
		xin = (ImageButton) view.findViewById(R.id.attn_xin);// 拨号键盘 *
		jin = (ImageButton) view.findViewById(R.id.attn_jin);// 拨号键盘 #
		home_call = (ImageButton) view.findViewById(R.id.home_call);// 拨号
		ib_add_contact = (ImageButton) view.findViewById(R.id.attn_add);// 拨号
		home_call_mobile = (ImageButton) view.findViewById(R.id.home_call_mobile);
		Number = (EditText) view.findViewById(R.id.attn_phone);// 电话号码
		Number.setFilters(new InputFilter[] { new InputFilter.LengthFilter(12) });

		zero.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				PhoneNumber = Number.getText().toString().trim();
				Number.setText(PhoneNumber + "+");
				return false;
			}
		});
		/**
		 * 设置点击监听事件
		 * */
		SetOnClickListener(Clear, one, two, three, four, five, six, seven,
				eight, nine, zero, xin, jin, home_call);
	}

	@Override
	public void onClick(View v) {
		PhoneNumber = Number.getText().toString().trim();
		super.onClick(v);
		switch (v.getId()) {
		case R.id.attn_clear:
			ClearNumber();
			break;
		case R.id.attn_1:
			Number.setText(PhoneNumber + "1");
			break;
		case R.id.attn_2:
			Number.setText(PhoneNumber + "2");
			break;
		case R.id.attn_3:
			Number.setText(PhoneNumber + "3");
			break;
		case R.id.attn_4:
			Number.setText(PhoneNumber + "4");
			break;
		case R.id.attn_5:
			Number.setText(PhoneNumber + "5");
			break;
		case R.id.attn_6:
			Number.setText(PhoneNumber + "6");
			break;
		case R.id.attn_7:
			Number.setText(PhoneNumber + "7");
			break;
		case R.id.attn_8:
			Number.setText(PhoneNumber + "8");
			break;
		case R.id.attn_9:
			Number.setText(PhoneNumber + "9");
			break;
		case R.id.attn_0:
			Number.setText(PhoneNumber + "0");
			break;
		case R.id.attn_xin:
			Number.setText(PhoneNumber + "*");
			break;
		case R.id.attn_jin:// 手机视频
			Number.setText(PhoneNumber + "#");

			break;
		case R.id.home_call:// tv视频
			String moblieNum2 = Number.getText().toString().trim();

			PhoneUtils.callVideo(getActivity(), moblieNum2, "8");
			break;
		case R.id.home_call_mobile: // 手机
			String moblieNum = Number.getText().toString().trim();

			PhoneUtils.callVideo(getActivity(), moblieNum, "9");

			break;
		}
	}

	private void ClearNumber() {
		PhoneNumber = Number.getText().toString().trim();
		if (PhoneNumber.length() == 0) {

		} else if (PhoneNumber.length() > 0) {
			StringBuffer buffer = new StringBuffer(PhoneNumber);
			buffer.deleteCharAt(buffer.length() - 1);
			Number.setText(buffer.toString().trim());
		}
	}
}
