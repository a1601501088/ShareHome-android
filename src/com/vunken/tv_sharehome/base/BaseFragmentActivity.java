package com.vunken.tv_sharehome.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/12/26.
 */
public abstract class BaseFragmentActivity extends FragmentActivity implements
		OnPageChangeListener, OnClickListener {
	protected Context mcontext;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		HuaweiSDKApplication.getApplication().activities.add(this);
		mcontext = this;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		HuaweiSDKApplication.getApplication().activities.remove(this);
		
	}

	public void showToast(String string) {
		Toast.makeText(mcontext, string, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 设置点击监听事件
	 * */
	public void SetOnClickListener(View view) {
		view.setOnClickListener(this);
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
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		OnPagerSelected(arg0);
	}

	public abstract void OnPagerSelected(int arg0);
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		//super.onSaveInstanceState(outState);
	}

}