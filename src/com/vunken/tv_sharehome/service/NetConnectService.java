package com.vunken.tv_sharehome.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.JsonCallBack;
import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.greendao.dao.ContactDao.Properties;
import com.vunken.tv_sharehome.greendao.dao.bean.Contact;
import com.vunken.tv_sharehome.greendao.util.DbCore;
import com.vunken.tv_sharehome.utils.APIUtils;
import com.vunken.tv_sharehome.utils.Logger;

public class NetConnectService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		//startForeground();
		
		
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("NetConnectService", "---------------onStartCommand-------------");
		//initSDK();
		//startForeground(0x111, null);
		int asynContact = 0 ;
		if (intent!=null) {
			if (intent.hasExtra("asynContact")) {
				 asynContact = intent.getIntExtra("asynContact", 0);
					if (asynContact==Config.ASYN_CONTACT) {
						/**
						 * 网络同步联系人操作
						 */
						asynContact();
					}else if(asynContact==Config.INTENT_LOGIN_VALUE) {
						//登录
						APIUtils.getInstance().Login(this);
					}
			}
			
		}
	
		return super.onStartCommand(intent, flags, startId);
	}
	 
	/**
	 * 上传联系人到服务器
	 */
	private void asynContact() {
		//List<Contact> contacts = DbCore.getDaoSession().getContactDao().loadAll();
		List<Contact> contacts =DbCore.getDaoSession().getContactDao().queryBuilder().where(Properties.ContactName.notEq("我的手机")).list();
		if (contacts==null || contacts.size()==0) {
			return;
		}
		
		String url = Config.SERVICE_URI + "/contact/asynContact.do";
		JSONObject json = new JSONObject();
		SharedPreferences	sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
		String login_name = sp.getString(Config.LOGIN_USER_NAME, "");
		String json2 = new Gson().toJson(contacts);
	   
		
		try {
			JSONArray jsonArray = new 	JSONArray(json2);
			json.put("userName", login_name);
		
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
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	
}
