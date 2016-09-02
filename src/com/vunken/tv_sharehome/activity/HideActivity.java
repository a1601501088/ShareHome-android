package com.vunken.tv_sharehome.activity;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.rcs.login.LoginApi;
import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.base.BaseActivity;


public class HideActivity extends BaseActivity {
	EditText ip = null;
	EditText port = null;
	Button commit ;
	@Override
	public void OnCreate() {
		setContentView(R.layout.activity_hide);
		init();
	}

	public void init() {
		ip = (EditText) findViewById(R.id.hide_Ip);
		port = (EditText) findViewById(R.id.hide_port);
		ip.setText(LoginApi.getConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_IP, LoginApi.CONFIG_MINOR_TYPE_DEFAULT));
		port.setText(LoginApi.getConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_PORT, LoginApi.CONFIG_MINOR_TYPE_DEFAULT));
		
//		ip.setText("205.177.226.86");
		ip.setText("222.246.189.244");
		commit = (Button) findViewById(R.id.hide_commit);
		commit.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				String sip = ip.getText().toString();
				String sport = port.getText().toString();
				if(TextUtils.isEmpty(sip) || TextUtils.isEmpty(sport)) {
					showToast("please input value");
				}else{
				LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_IP, LoginApi.CONFIG_MINOR_TYPE_DEFAULT, sip);
				LoginApi.setConfig(LoginApi.CONFIG_MAJOR_TYPE_DM_PORT, LoginApi.CONFIG_MINOR_TYPE_DEFAULT, sport);
				finish();
				}
				return false;
			}
		});
	}

	@Override
	public void OnClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
