package com.vunken.tv_sharehome.serv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.utils.Logger;

public class AsnyContact {
	 
		/**
		 * 上传联系人到服务器
		 */
	public void asynContact(Context context,String userId,String homePhone,String contactName) {
			
			
			String url = Config.SERVICE_URI + "/contact/asynContact.do";
			JSONObject json = new JSONObject();
			SharedPreferences	sp = context.getSharedPreferences(Config.SP_NAME,context.MODE_PRIVATE);
			String login_name = sp.getString(Config.LOGIN_USER_NAME, "");
		
		   
			
			try {
				JSONArray jsonArray = new 	JSONArray();
				json.put("userName", login_name);
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("userId", userId);
				jsonObject.put("homePhone", homePhone);
				jsonObject.put("userName", login_name);
				jsonObject.put("contactName", contactName);
				jsonArray.put(jsonObject);
			
			
				json.put("contacts",jsonArray);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			OkHttpUtils.post(url).params("json",json.toString()).execute(new JsonCallBack<String>() {

				@Override
				public void onResponse(String t) {
					Logger.d("NetConnectService", "同步联系人:","同步联系人入参上传成功");
					
				}
			});
		}
}
