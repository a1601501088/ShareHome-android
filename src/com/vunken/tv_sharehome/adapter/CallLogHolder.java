package com.vunken.tv_sharehome.adapter;

import java.util.Date;

import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.fragment.CallLogFragment;
import com.vunken.tv_sharehome.greendao.dao.bean.CallRecorders;
import com.vunken.tv_sharehome.utils.UiUtils;

public class CallLogHolder extends BaseHolder {
	private TextView calllog_name, calllog_phoneNumber, calllog_date;
	private ImageView calllog_type;
	private CallLogFragment callLogFragment;

	public CallLogHolder(CallLogFragment callLogFragment) {
		this.callLogFragment = callLogFragment;
	}

	@Override
	protected View initView() {
		View view = View.inflate(UiUtils.getContext(),
				R.layout.listview_calllog, null);
		calllog_name = (TextView) view.findViewById(R.id.calllog_name);
		calllog_phoneNumber = (TextView) view
				.findViewById(R.id.calllog_phoneNumber);
		calllog_date = (TextView) view.findViewById(R.id.calllog_date);

		calllog_type = (ImageView) view.findViewById(R.id.calllog_type);

		return view;
	}

	@Override
	protected void refreshView(Object data, final int position, ViewGroup parent) {
		if (data == null) {
			return;
		}
		CallRecorders call_log = (CallRecorders) data;

		
		String number = call_log.getCallRecordersPhone() + "";
			if (number.startsWith("8")||number.startsWith("9")) {
				number = number.substring(1);
			}
		calllog_phoneNumber.setText(number);
		calllog_name.setText(call_log.getContactName());
		Date create_time = call_log.getCreateTime();
		if (create_time != null) {
			String localeString = create_time.toLocaleString();
			String[] split = localeString.split(" ");
			if (DateUtils.isToday(create_time.getTime())) {// 使用系统工具类判断是否是今天
				calllog_date.setText(split[1]);
			} else {
				calllog_date.setText(split[0].substring(2));
			}
		}

		String call_type = call_log.getCallType();
		/**
		 * 0 未接 1 已接 2 拨打
		 */
		if (Config.CALLRECORDER_TYPE_MISSED.equals(call_type)) {
			calllog_type.setBackgroundResource(R.drawable.type_missing);
		} else if (Config.CALLRECORDER_TYPE_RECEIVED.equals(call_type)) {
			calllog_type.setBackgroundResource(R.drawable.type_incoming);
		} else if (Config.CALLRECORDER_TYPE_DIAL.equals(call_type)) {
			calllog_type.setBackgroundResource(R.drawable.type_outgoing);
		}

	}

}
