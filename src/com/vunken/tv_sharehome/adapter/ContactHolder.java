package com.vunken.tv_sharehome.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vunken.tv_sharehome.R;
import com.vunken.tv_sharehome.domain.ContactBean;
import com.vunken.tv_sharehome.greendao.dao.bean.Contact;
import com.vunken.tv_sharehome.utils.UiUtils;

public class ContactHolder extends BaseHolder {
	private TextView tv_name, tv_moblie;

	@Override
	protected View initView() {
		View view = View.inflate(UiUtils.getContext(),
				R.layout.listview_contacts, null);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		tv_moblie = (TextView) view.findViewById(R.id.tv_moblie);
		return view;
	}

	@Override
	protected void refreshView(Object data, int position, ViewGroup parent) {
		Contact contactBean = (Contact) data;
		//显示姓名
		String name = contactBean.getContactName();
			tv_name.setText(name);
			String home_phone = contactBean.getHomePhone();
			tv_moblie.setText(home_phone+"" );	
			/*if (contactBean.getIs_rcs()) {
				tv_moblie.append("    RCS用户");
			}*/
			
		
		/*//显示号码
		long contactId = contactSummary.getContactId();
		Contact contact = ContactApi.getContact ( contactId);
		if (contact!=null) {
			List<Phone> phones =contact.getPhones();
			boolean b = phones.get(0).isRcsUser();
			String moblie = phones.get(0).getNumber();
			if (TextUtils.isEmpty(moblie)) {
				tv_moblie.setText("00000000" );
			} else {
				tv_moblie.setText(moblie);
				if (b) {
					tv_moblie.append( "   RCS用户");
				}
			}
		}
		//头像
		//Bitmap bitmap = contact.getPhoto(UiUtils.getContext());
*/		
	}

}
