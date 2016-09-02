package com.vunken.tv_sharehome.adapter;

import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.greendao.dao.bean.WhiteContanct;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class WhiteHolder extends BaseHolder {

	private TextView tv_username,tv_moblie;
	private CheckBox cb_check;

	@Override
	protected View initView() {
		View v =View.inflate(HuaweiSDKApplication.getApplication(), R.layout.list_item_white_contanct, null);
		tv_username = (TextView) v.findViewById(R.id.tv_username);
		tv_moblie = (TextView) v.findViewById(R.id.tv_moblie);
		cb_check = (CheckBox) v.findViewById(R.id.cb_check);
		
		return v;
	}

	@Override
	protected void refreshView(Object data, int position, ViewGroup parent) {
		WhiteContanct whiteContanct = (WhiteContanct) data;
		
		tv_username.setText(whiteContanct.getContactName());
		tv_moblie.setText(whiteContanct.getHomePhone());
	}


}
