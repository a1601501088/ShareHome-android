package com.vunken.tv_sharehome.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.vunken.tv_sharehome.Config;

public class SPUtils {
	private static SPUtils instance;
	private Context context;
	private SharedPreferences sp;
	private SPUtils(){}
	private SPUtils(Context context){
		this.context = context;
		 sp = context.getSharedPreferences(Config.SP_NAME, 0);
	}
	
	
	
	public static SPUtils getInstance(Context context) {
		if (instance==null) {
			instance = new SPUtils(context);
		}
		return instance;
	}

	public  void setUserName(String username){
		sp.edit().putString(Config.LOGIN_USER_NAME, username).commit();
	}

	public  String getUserName(){
	
		String userName = sp.getString(Config.LOGIN_USER_NAME, "");
		return userName;
		
	}
	
	public  String getPassWrod(){
		String pass = sp.getString(Config.LOGIN_PASSWORD, "");
		try {
			pass = Encrypt3DES.getInstance().decrypt(pass);
			return pass;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
		
	}
	public  SPUtils setPassWrod(String pass){
		sp.edit().putString(Config.LOGIN_PASSWORD, pass).commit();
		return this;
	}
	/**
	 * 每一次进入app的标识状态
	 * @param first
	 * false 表示没有进入过app
	 * true 表示进入过app
	 */
	public  SPUtils setfirstEntryPpplication(Boolean first){
		sp.edit().putBoolean(Config.FIRST_ENTRY_APPLICATION, first).commit();
		return this;
	}
	
	
	public  SPUtils clear(){
		sp.edit().clear().commit();
		return this;
	}
}
