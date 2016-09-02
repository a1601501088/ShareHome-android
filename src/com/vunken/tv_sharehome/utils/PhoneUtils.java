package com.vunken.tv_sharehome.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.call.CallOut_Activity;

public class PhoneUtils {

	/**
	 * 视频呼叫
	 * @param context
	 * @param moblie  手机号
	 * @param userType  拨打tv端还是手机端, 8tv，9手机
	 */
	public static void callVideo(Context context, String moblie, String userType) {
		// String moblieNum = Number.getText().toString().trim();
		if (TextUtils.isEmpty(moblie)) {
			return;
		}
		Intent intent = new Intent(context, CallOut_Activity.class);
		intent.putExtra("is_video_call", true);
		if (moblie.startsWith(Config.CALL_BEFORE)) {
			moblie = moblie.split(Config.CALL_BEFORE)[1];
			if (!moblie.startsWith("8") && !moblie.startsWith("9")) {
				moblie = userType + moblie;
			}
			intent.putExtra("PhoneNumber", Config.CALL_BEFORE+moblie);
		} else {
			intent.putExtra("PhoneNumber", Config.CALL_BEFORE + userType
					+ moblie);
		}
		context.startActivity(intent);
	}

	/**
	 * 正则表达式 判断手机号
	 */
	public static boolean isMobile(String mobile) {
		if (TextUtils.isEmpty(mobile)) {
			return false;
		}
		
		String regEx = "^(\\+86)?1[3,5,8](\\d{9})$";
		// 编译正则表达式
		Pattern pattern = Pattern.compile(regEx);
		// 忽略大小写的写法
		// Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(mobile);
		// 字符串是否与正则表达式相匹配
		boolean rs = matcher.matches();
		return rs;
	}

}
