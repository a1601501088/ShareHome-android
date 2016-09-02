package com.vunken.tv_sharehome.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.huawei.rcs.RCSService;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.greendao.dao.bean.Contact;
import com.vunken.tv_sharehome.greendao.util.DbCore;

public class ShareHomeService extends RCSService {
	/**
	 * 唯一标识开发者开发的应用，用于与其它系统的服务进行区分。
	 * */
	public static final String SERVICE_NAME="com.vunke.rcs.SERVICE";
	@Override
	public int onStartCommand(Intent arg0, int arg1, int arg2) {
		
		 
		
		return super.onStartCommand(arg0, arg1, arg2);
	}
	
	
}
