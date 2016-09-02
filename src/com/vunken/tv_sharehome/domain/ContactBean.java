package com.vunken.tv_sharehome.domain;

import android.graphics.Bitmap;

/**
 * 聯系人
 * @author Administrator
 *
 */
public class ContactBean {
	private String contactName;
	private long contactId;
	private String moblie;
	private Bitmap head;
	private boolean isRCS;
	
	public boolean isRCS() {
		return isRCS;
	}
	
	public long getContactId() {
		return contactId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	public void setRCS(boolean isRCS) {
		this.isRCS = isRCS;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getMoblie() {
		return moblie;
	}
	public void setMoblie(String moblie) {
		this.moblie = moblie;
	}
	public Bitmap getHead() {
		return head;
	}
	public void setHead(Bitmap head) {
		this.head = head;
	}
	@Override
	public String toString() {
		return "ContactBean [contactName=" + contactName + ", moblie=" + moblie
				+ "]";
	}

	private int flag;//记录
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getFlag() {
		return flag;
	}
	
	
}
