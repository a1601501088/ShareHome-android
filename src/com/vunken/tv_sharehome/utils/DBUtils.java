package com.vunken.tv_sharehome.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.vunken.tv_sharehome.Config;
import com.vunken.tv_sharehome.MySQLiteHelper;
import com.vunken.tv_sharehome.base.HuaweiSDKApplication;
import com.vunken.tv_sharehome.greendao.dao.DaoSession;
import com.vunken.tv_sharehome.greendao.dao.WhiteContanctDao.Properties;
import com.vunken.tv_sharehome.greendao.dao.bean.CallRecorders;
import com.vunken.tv_sharehome.greendao.dao.bean.Contact;
import com.vunken.tv_sharehome.greendao.dao.bean.WhiteContanct;
import com.vunken.tv_sharehome.greendao.util.DbCore;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

public final class DBUtils {
	private static DBUtils instance;
	// private MySQLiteHelper sqLiteHelper;
	private Context context;

	private DBUtils(Context context) {
		this.context = context;
		// sqLiteHelper = new MySQLiteHelper(context);
	};

	public static DBUtils getInstance(Context context) {
		/**
		 * 只在第一次创建对象时加锁
		 */
		if (instance == null) {
			synchronized (DBUtils.class) {
				if (instance == null) {
					instance = new DBUtils(context);
				}
			}
		}
		return instance;
	}

	// cha 入白名单
	public long insertWhiteContanct(String username, String moblie, Date date,
			String face) {

		WhiteContanct whiteContanct = new WhiteContanct();
		whiteContanct.setContactName(username);
		whiteContanct.setHomePhone(moblie);
		whiteContanct.setCreateTime(date);
		whiteContanct.setFace(face);
		long insert = DbCore.getDaoSession().getWhiteContanctDao()
				.insert(whiteContanct);
		return insert;

	}

	// 获取所有白名单
	public List<WhiteContanct> getAllWhiteContanct() {
		QueryBuilder<WhiteContanct> queryBuilder = DbCore.getDaoSession()
				.getWhiteContanctDao().queryBuilder();
		List<WhiteContanct> whiteContancts = queryBuilder.orderDesc(
				Properties.WhiteId).list();
		/**
		 * 获取账号信息，可到账号的手机号码
		 */
		SharedPreferences sp = HuaweiSDKApplication.getApplication()
				.getSharedPreferences(Config.SP_NAME, 0);
		String username = sp.getString(Config.LOGIN_USER_NAME, "");
		List<WhiteContanct> ws = null;
		if (username.startsWith("8")) {
			username = "9" + username.substring(1);
			/**
			 * 通过手机号码查找出对应的bean
			 */
			ws = queryBuilder.where(Properties.HomePhone.eq(username)).list();
			if (ws.size() > 0) {
				WhiteContanct w = ws.get(0);
				/**
				 * 把bean 加到第一个位置
				 */
				boolean remove = whiteContancts.remove(w);
				if (remove) {
					whiteContancts.add(0, w);
				}

			}
		}

		return whiteContancts;
	}

	/**
	 * 删除白名单联系
	 */
	public boolean deleteWhiteContact(long whiteId) {

		DbCore.getDaoSession().getWhiteContanctDao().deleteByKey(whiteId);
		return true;

	}

	/**
	 * 保存通话记录
	 * 
	 * @param phone
	 * @param type
	 * @param call_time
	 * @return
	 */
	public long insertCallRecorder(String phone, String type, String call_time) {
		DaoSession daoSession = DbCore.getDaoSession();
		Query<Contact> query = daoSession
				.getContactDao()
				.queryBuilder()
				.where(com.vunken.tv_sharehome.greendao.dao.ContactDao.Properties.HomePhone
						.eq(phone)).build();
		List<Contact> list = query.list();
		Contact contact = null;
		int size = list.size();
		for (int i = 0; i < size; i++) {
			if (!TextUtils.isEmpty(list.get(i).getContactName())) {
				contact = list.get(i);
				break;
			}
		}

		/**
		 * 构建通话记录bean
		 */
		CallRecorders callRecorders = new CallRecorders();
		callRecorders.setCallRecordersPhone(phone);
		if (contact != null) {// 此号码在联系人中
			callRecorders.setContactName(contact.getContactName());
		} else {
			callRecorders.setContactName("未知号码");
		}

		// callRecorders.setContact(contact);
		callRecorders.setCallType(type);
		callRecorders.setCreateTime(new Date());
		if (!TextUtils.isEmpty(call_time)) {
			callRecorders.setCallTime(call_time);
		}
		Log.e("insertCallRecorder", "phone:" + phone + ";User_name:-->"
				+ callRecorders.getContactName());
		return daoSession.getCallRecordersDao().insert(callRecorders);
	}

	/**
	 * 获取所有的联系人, 并按user_id倒须排列
	 * 
	 * @return
	 */
	public List<Contact> getContactList() {
		QueryBuilder<Contact> queryBuilder = DbCore.getDaoSession()
				.getContactDao().queryBuilder();
		/**
		 * 查询联系人按userId排序
		 */
		List<Contact> contacts = queryBuilder
				.orderDesc(
						com.vunken.tv_sharehome.greendao.dao.ContactDao.Properties.UserId)
				.build().list();
		
		List<Contact> cs = null;
		
			
			/**
			 * 通过手机号码查找出对应的bean
			 */
			cs = queryBuilder
					.where(com.vunken.tv_sharehome.greendao.dao.ContactDao.Properties.ContactName
							.eq("我的手机")).list();
			if (cs.size() > 0) {
				Contact contact = cs.get(0);
				/**
				 * 把bean 加到第一个位置
				 */
				boolean remove = contacts.remove(contact);
				if (remove) {
					contacts.add(0, contact);
				}
			}
		
		return contacts;
	}

	/**
	 * 获取所有通话记录，并Call_id倒须排列
	 * 
	 * @return
	 */

	public List<CallRecorders> getCallRecorderList() {

		return DbCore
				.getDaoSession()
				.getCallRecordersDao()
				.queryBuilder()
				.orderDesc(
						com.vunken.tv_sharehome.greendao.dao.CallRecordersDao.Properties.CallId)
				.build().list();
	}

	/**
	 * 通过手机号获取联系人
	 * 
	 * @param account
	 */
	public Contact getContactByPhone(String account) {
		List<Contact> contacts = DbCore
				.getDaoSession()
				.getContactDao()
				.queryBuilder()
				.where(com.vunken.tv_sharehome.greendao.dao.ContactDao.Properties.HomePhone
						.eq(account)).build().list();
		/**
		 * 未找到联系人
		 */
		if (null == contacts || 1 > contacts.size()) {
			return null;
		}
		/**
		 * 找到了联系人
		 */
		else {
			return contacts.get(0);
		}
	}

	/**
	 * 通话电话号码获取某一个白名单
	 * 
	 * @param userName
	 */
	public WhiteContanct getWhiteContanctByPhone(String userName) {
		List<WhiteContanct> contancts = DbCore.getDaoSession()
				.getWhiteContanctDao().queryBuilder()
				.where(Properties.HomePhone.eq(userName)).build().list();

		/**
		 * 未找到联系人
		 */
		if (null == contancts || 1 > contancts.size()) {
			return null;
		}
		/**
		 * 找到了联系人
		 */
		else {
			return contancts.get(0);
		}

	}

}
