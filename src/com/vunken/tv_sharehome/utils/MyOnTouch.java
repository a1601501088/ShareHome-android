/*首先  修改包名，       例如 package com.aaa.bbb.ccc; (aaa.bbb.ccc)是自己的包名*/
package com.vunken.tv_sharehome.utils;

import android.app.Activity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
/**
 * 只需要  new MyOnTouch(控件名 ,类名.this); 
 * 随意拖动控件，但是没有固定在最左边或者最右边，注意:拖动位置只能在该控件的布局范围内
 *  需要传一个view   也就是实例化后的控件       一个Activity    类.this
 * */
public class MyOnTouch implements OnTouchListener {
	private View v;
	private Activity activity;
	private int lastX, lastY;
	public MyOnTouch(View view,Activity activity) {
		this.v=view;
		v.setOnTouchListener(this);
		this.activity = activity;
	}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		Display dis = activity.getWindowManager().getDefaultDisplay();
		int screenWidth = dis.getWidth();
		int screenHeight = dis.getHeight();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			break;

		case MotionEvent.ACTION_MOVE:
			int dx = (int) event.getRawX() - lastX;
			int dy = (int) event.getRawY() - lastY;
			int top = view.getTop() + dy;
			int left = view.getLeft() + dx;
			if (top <= 0) {
				top = 0;
			}						//控件
			if (top >= screenHeight - v.getHeight()) {
									//控件
				top = screenHeight - v.getHeight();
			}						//控件
			if (left >= screenWidth - v.getWidth()) {
									//控件
				left = screenWidth - v.getWidth();
			}

			if (left <= 0) {
				left = 0;
			}							//控件
			view.layout(left, top, left + v.getWidth(), top
					//控件
					+ v.getHeight());
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			break;
		case MotionEvent.ACTION_UP:

			break;

		}
		return true;
	}

}
