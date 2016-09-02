package com.vunken.tv_sharehome.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class NetMessage {
	 private JSONObject json;

	public  NetMessage(String data) {
		if (TextUtils.isEmpty(data)) {
			return;
		}
		try {
			json = new JSONObject(data);
			
		} catch (JSONException e) {
			e.printStackTrace();
		Log.e("josn", "服务器传过来的json数据错误");
		}
		 
	}
	/**
	 * 获取code
	 * @return
	 */
	public String getCode(){
		if (json!=null) {
			if (json.has("code")) {
			try {
				return	json.getString("code");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			}
			
		}
		return "";
	}
	/**
	 * 获取message
	 * @return
	 */
	public String getMessage(){
		if (json!=null) {
			if (json.has("message")) {
			try {
				return	json.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			}
			
		}
		return "";
	}
	
	/**
	 * 获取不同key的内容
	 * @return
	 */
	public String getData(String key){
		if (json!=null) {
			if (json.has(key)) {
			try {
				return	json.getString(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			}
			
		}
		return "";
	}
}
