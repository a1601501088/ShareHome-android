package com.vunken.tv_sharehome.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.rcs.call.CallApi;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.service.CaaSSdkService;

public class UiUtils {
	/**
	 * 获取到字符数组
	 * 
	 * @param tabNames
	 *            字符数组的id
	 */
	public static String[] getStringArray(int tabNames) {
		return getResource().getStringArray(tabNames);
	}

	public static Resources getResource() {
		return HuaweiSDKApplication.getApplication().getResources();
	}

	public static Context getContext() {
		return HuaweiSDKApplication.getApplication();
	}

	/** dip转换px */
	public static int dip2px(int dip) {
		final float scale = getResource().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

	/** pxz转换dip */

	public static int px2dip(int px) {
		final float scale = getResource().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	/**
	 * 使用系统工具类判断是否是今天 是今天就显示发送的小时分钟 不是今天就显示发送的那一天
	 * */
	public static String getDate(Context context, long when) {
		String date = null;
		if (DateUtils.isToday(when)) {
			date = DateFormat.getTimeFormat(context).format(when);
		} else {
			date = DateFormat.getDateFormat(context).format(when);
		}
		return date;
	}

	public static Toast mToast;

	public static void showToast(Context mContext, String msg) {
		if (mToast == null) {
			mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
		}
		mToast.setText(msg);
		mToast.show();
	}
	public static void showToast( String msg) {
		showToast(HuaweiSDKApplication.getApplication(), msg);
	}
	// 图片id 转 drawable类型
	public static Drawable getDrawalbe(int id) {
		return getResource().getDrawable(id);
	}

	/**
	 * 关闭软键盘
	 * 
	 * @param mEditText输入框
	 * @param mContext上下文
	 */
	public static void closeKeybord(EditText mEditText, Context mContext) {
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}

	/**
	 * 
	 * @param et
	 * @param v
	 * @return
	 */
	public static boolean isLeftGoFcous(EditText et, View v) {
		if (et != null) {
			if (TextUtils.isEmpty(et.getText().toString().trim())) {
				if (v != null) {
					v.requestFocus();
				}

			} else if (et.getSelectionStart() == 0) {
				return true;
			}
		}
		return false;
	}

	public static void setMaxLength(EditText et, int max) {
		et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(max) });

	}

	/**
	 * 关闭本地摄像头
	 */
	public static void closeLocalView() {
				//CaaSSdkService.closeLocalView();
				CallApi.closeLocalView();
	}

	/**
	 * 打开本地摄像头
	 */
	public static void openLocalView() {
				// 打开摄像头
				//CaaSSdkService.openLocalView();
				
				CallApi.openLocalView();
	}
	
	public static void setLocalStatus( boolean status){
		CaaSSdkService.setLocalCamaraStatus(status);
	}

}
