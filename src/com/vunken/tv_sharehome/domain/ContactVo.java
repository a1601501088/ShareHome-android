package com.vunken.tv_sharehome.domain;

import java.util.ArrayList;

import com.vunken.tv_sharehome.greendao.dao.bean.Contact;

public class ContactVo {
	
	public String code;
	public String message;
	
	public ArrayList<Contact> contacts;

	@Override
	public String toString() {
		return "ContactVo [code=" + code + ", message=" + message
				+ ", contacts=" + contacts + "]";
	}
	

}
