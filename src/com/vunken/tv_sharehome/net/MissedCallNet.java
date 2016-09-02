package com.vunken.tv_sharehome.net;

import okhttp3.Call;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.greendao.dao.bean.Contact;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.utils.CommonUtil;
import com.vunken.tv_sharehome.utils.DBUtils;
import com.vunken.tv_sharehome.utils.Logger;
import com.vunken.tv_sharehome.utils.NetMessage;
import com.vunken.tv_sharehome.utils.UiUtils;
/**
 * 未接来电提醒, 以短信方式发送给对方
 * @author Administrator
 *
 */
public class MissedCallNet {
	/**
	 * 
	 * @param callingPhone 主叫号码
	 * @param calledPhone	被叫号码
	 * @param callTime    拨号时间
	 * @param callingType  主叫类型
	 * @param calledType	被叫类型
	 * @param callback   给外部的回调
	 */
	public MissedCallNet(String callingPhone, String calledPhone,
			 String callTime, String callingType,
			String calledType, final MissedCallNetCallback callback) {
		if (CommonUtil.isNetConnected(HuaweiSDKApplication.getApplication())) {
			/**
			 * 获取被叫号码在本机中的姓名
			 */
		Contact contact = DBUtils.getInstance(HuaweiSDKApplication.getApplication()).getContactByPhone(calledPhone);
		String calledName = "未知号码 ";
		if (contact!=null) {
			String contactName = contact.getContactName();
			if (!TextUtils.isEmpty(contactName)) {
				calledName = contactName;
			}
		}
		JSONObject json = new JSONObject();
		try {
			json.put("callingPhone", callingPhone);
			json.put("calledPhone", calledPhone);
			json.put("calledName", calledName);
			json.put("callTime", callTime);
			json.put("callingType", callingType);
			json.put("calledType", calledType);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		OkHttpUtils.post(Config.SERVICE_URI + "/missedCall.do")
				.params("json", json.toString())
				.execute(new JsonCallBack<String>() {
					@Override
					public void onResponse(String t) {
						Logger.d("MissedCallNet", "/missedCall.do", t);
						NetMessage netMessage = new NetMessage(t);
						if ("200".equals(netMessage.getCode())) {
							if (callback != null) {
								callback.onSuccess(netMessage.getCode(), netMessage.getMessage());
							}
						} else {
							if (callback != null) {
								callback.onFail(netMessage.getCode(), netMessage.getMessage());
							}
						}
					}
					@Override
					public void onError(Call call, Response response,
							Exception e) {
						super.onError(call, response, e);
						Logger.d("MissedCallNet", "/missedCall.do",e.getMessage());
					}
				});
		}else {
			UiUtils.showToast( "网络出现问题,请检查你的网络!");
		}
	}

	public interface MissedCallNetCallback {
		void onSuccess(String code, String messge);

		void onFail(String code, String messge);
	}
}
