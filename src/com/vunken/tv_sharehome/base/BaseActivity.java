package com.vunken.tv_sharehome.base;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 基类
 * */
public abstract class BaseActivity extends Activity implements OnClickListener {
	public final String TAG = "ShareHome_Login";// 日志
	public static Toast mToast;
	public String filename = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "userpic.jpg";
	public String publishfilename = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "publish.jpg";
	protected BaseActivity mcontext;
	protected  String countryCode = "+86"; 
	protected  Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		HuaweiSDKApplication.getApplication().activities.add(this);
		mcontext = this;
		OnCreate();
		
	}

	public abstract void OnCreate();

	public abstract void OnClick(View v);

	/**
	 * 设置点击监听事件
	 * */
	public void SetOnClickListener(View view) {
		view.setOnClickListener(this);
	}

	/**
	 * 设置点击监听事件
	 * */
	public void SetOnClickListener(View... v) {
		for (int i = 0; i < v.length; i++) {
			View view = v[i];
			if (view!=null) {
				view.setOnClickListener(this);
			}
		}
	}

	/**
	 * 吐司
	 * */
	public void showToast(String string) {
		Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT)
				.show();
	}

	

	/**
	 * 正则表达式判断邮箱
	 * */
	public  boolean isValidEmail(String email) {
		boolean flag = false;
		try {
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 正则表达式 判断手机号
	 */
	public  boolean isMobile(String mobile) {
		if (TextUtils.isEmpty(mobile)) {
            return false;
		}
        // 邮箱验证规则
        String regEx = "^(\\+86)?1(\\d{10})$";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regEx);
        // 忽略大小写的写法
        // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(mobile);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
      return rs;
	}

	/**
	 * 正则表达式 判断密码
	 */
	public  boolean isPasswordStandard(String user_wd) {

		// 不能包含中文
		if (hasChinese(user_wd)) {
			return false;
		}

		/**
		 * 正则匹配 \\w{6,18}匹配所有字母、数字、下划线 字符串长度6到18（不含空格）
		 */
		String format = "(@?+\\w){6,18}+";
		if (user_wd.matches(format)) {
			return true;
		}
		return false;
	}

	/**
	 * 中文识别
	 * 
	 * @author luman
	 */
	public  boolean hasChinese(String source) {
		String reg_charset = "([\\u4E00-\\u9FA5]*+)";
		Pattern p = Pattern.compile(reg_charset);
		Matcher m = p.matcher(source);
		boolean hasChinese = false;
		while (m.find()) {
			if (!"".equals(m.group(1))) {
				hasChinese = true;
			}

		}
		return hasChinese;
	}




	/**
	 * 判断当前网络是否连接
	 * 
	 * @param context
	 * @return
	 */
	public  boolean isNetConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		}
		return false;
	}

	/**
	 * 转向另一个页面
	 * 
	 * @param poFrom
	 *            当前activity
	 * @param poTo
	 *            跳转到这个activity
	 * @param pbFinish
	 *            是否finish当前页面
	 * @param pmExtra
	 *            携带数据，不带数据写null Map<String, String> lmExtra = null; String
	 *            msRedirectPage = "登录成功"; if
	 *            (!Utils.isStrEmpty(msRedirectPage)) { lmExtra = new
	 *            HashMap<String, String>(); lmExtra.put("redirect",
	 *            msRedirectPage); }
	 */
	public  void gotoActivity(Activity poFrom, Class<?> poTo,
			boolean pbFinish, Map<String, String> pmExtra) {
		Intent loIntent = new Intent(poFrom, poTo);
		if (pmExtra != null && !pmExtra.isEmpty()) {
			Iterator<String> loKeyIt = pmExtra.keySet().iterator();
			while (loKeyIt.hasNext()) {
				String lsKey = loKeyIt.next();
				loIntent.putExtra(lsKey, pmExtra.get(lsKey));
			}
		}
		if (pbFinish)
			poFrom.finish();
		poFrom.startActivity(loIntent);
	}

	/** 隐藏软键盘 */
	public  void hideSoftInput(Context context) {

		InputMethodManager inputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager.isActive()) {
			inputMethodManager.toggleSoftInput(0, 2); // 隐藏输入盘
			/*inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
					     InputMethodManager.HIDE_NOT_ALWAYS);*/
		}
	}

	/**
	 * dip转换px
	 */
	public  int dip2px(Context context, int dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	/**
	 * 头像圆角
	 * */
	public Bitmap setPicRoundCorner(Bitmap thePic) {
		thePic = getDiskBitmap(filename);
		Bitmap output = Bitmap.createBitmap(thePic.getWidth(),
				thePic.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, thePic.getWidth(), thePic.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, 100, 100, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(thePic, rect, rect, paint);

		return output;
	}

	/**
	 * 将要分享的图片保存到本地图片上
	 * */
	public void savePublishPicture(Bitmap bitmap) {
		File f = new File(publishfilename);
		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);// 把Bitmap对象解析成流
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		HuaweiSDKApplication.getApplication().activities.remove(this);
	}
	/**
	 * 从本地读取头像
	 * */
	public Bitmap getDiskBitmap(String pathString) {
		Bitmap bitmap = null;
		try {
			File file = new File(pathString);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return bitmap;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		OnClick(v);
	}
}
