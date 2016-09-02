package com.vunken.tv_sharehome.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class BaseFragment extends Fragment implements OnClickListener{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	/**
	 * 设置点击监听事件
	 * */
	public void SetOnClickListener(View... v) {
		for (int i = 0; i < v.length; i++) {
			View view = v[i];
			view.setOnClickListener(this);
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
