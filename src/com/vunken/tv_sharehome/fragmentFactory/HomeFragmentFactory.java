package com.vunken.tv_sharehome.fragmentFactory;

import java.util.HashMap;
import java.util.Map;

import com.vunken.tv_sharehome.base.BaseFragment;
import com.vunken.tv_sharehome.fragment.AttnFragment;
import com.vunken.tv_sharehome.fragment.CallLogFragment;
import com.vunken.tv_sharehome.fragment.ContactFragment;
import com.vunken.tv_sharehome.fragment.SettingFragment;

import android.support.v4.app.Fragment;

public class HomeFragmentFactory {
	public static Map<Integer, BaseFragment> mFragments = new HashMap<Integer, BaseFragment>();

	public static BaseFragment createFragment(int position) {
		BaseFragment fragment = null;
		fragment = mFragments.get(position);
		if (fragment == null) { //如果等于null，说明集合中没有，就须要重新创建
			if (position == 0) {
				fragment = new ContactFragment();
			} else if (position == 1) {
				fragment = new CallLogFragment();
			} else if (position == 2) {
				fragment = new AttnFragment();
			} else if (position == 3) {
				fragment = new SettingFragment();
			}
		}
		if (fragment != null) {
			mFragments.put(position, fragment);
		}
		return fragment;
	}
}
