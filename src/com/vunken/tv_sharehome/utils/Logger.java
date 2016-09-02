package com.vunken.tv_sharehome.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * log日志统计保存
 * 
 * @author way
 * 
 */

public class Logger {
	private String tag = "Logger";
	private static Logger INSTANCE = null;
	public static String PATH_LOGCAT;
	private LogDumper mLogDumper = null;
	private int mPId;

	/**
	 * 
	 * 初始化目录
	 * 
	 * */
	public void init(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
			PATH_LOGCAT = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + File.separator + "shareHomelogger";
		} else {// 如果SD卡不存在，就保存到本应用的目录下
			PATH_LOGCAT = context.getFilesDir().getAbsolutePath()
					+ File.separator + "shareHomelogger";
		}
		File file = new File(PATH_LOGCAT);
		if (!file.exists()) {
			file.mkdirs();
		}
		/*
		 * 只保存后5个log
		 */
		File[] listFiles = file.listFiles();
		
		List<File> asList = Arrays.asList( listFiles);
		Collections.sort(asList);
		if (listFiles.length>4) {
			for (int i = 0; i < listFiles.length; i++) {
				if (listFiles.length-i>4) {
					listFiles[i].delete();
				}else {
					break;
				}
				
			}
		}
		
	}

	public static Logger getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new Logger(context);
		}
		return INSTANCE;
	}

	private Logger(Context context) {
		init(context);
		mPId = android.os.Process.myPid();
	}

	public void start() {
		if (mLogDumper == null)
			mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
		mLogDumper.start();
	}

	public void stop() {
		if (mLogDumper != null) {
			mLogDumper.stopLogs();
			mLogDumper = null;
		}
	}

	private class LogDumper extends Thread {

		private Process logcatProc;
		private BufferedReader mReader = null;
		private boolean mRunning = true;
		String cmds = null;
		private String mPID;
		private FileOutputStream out = null;

		public LogDumper(String pid, String dir) {
			mPID = pid;
			try {
				out = new FileOutputStream(new File(dir, 
						getDateEN() + ".log"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/**
			 * 
			 * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
			 * 
			 * 显示当前mPID程序的 E和W等级的日志.
			 * 
			 * */

		 cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
			 // cmds = "logcat | grep \"(" + mPID + ")\"";//打印所有日志信息
			// cmds = "logcat -s way";//打印标签过滤信息
			//cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";
			// cmds ="logcat -f"+ PATH_LOGCAT+"/"+getDateEN() + ".log";

		}

		public void stopLogs() {
			mRunning = false;
		}

		@Override
		public void run() {
			try {
				
				logcatProc =  Runtime.getRuntime().exec(cmds);
				mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
				String line = null;
				while (mRunning && (line = mReader.readLine()) != null) {
					if (!mRunning) {
						break;
					}
					if (line.length() == 0) {
						continue;
					}
					if (out != null && line.contains(mPID)) {
						out.write((getDateEN() + " " + line + "\n")
								.getBytes());
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (logcatProc != null) {
					logcatProc.destroy();
					logcatProc = null;
				}
				if (mReader != null) {
					try {
						mReader.close();
						mReader = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					out = null;
				}

			}

		}

	}
	
	/*public  String getFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date(System.currentTimeMillis()));
		return date;// 2012年10月03日 23:41:31
	}*/

	public  String getDateEN() {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date1 = format1.format(new Date(System.currentTimeMillis()));
		return date1;// 2012-10-03 23:41:31
	}
	
	
	private static  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS"); 

	
	public static void d(String tag,String inter,String msg){
		Log.e(tag, "[d]==>"+inter+":"+msg+";time:"+getTime());
	}
	public  static void e(String tag,String inter,String msg){
		Log.e(tag, "[e]==>"+inter+":"+msg+";time:"+getTime());
	}
	
	
	

	private static String getTime(){
		Date date = Calendar.getInstance().getTime();
		String format = sdf.format(date);
		return format;
	}
	
	/*OK，所有事情做完之后，在我们的应用中start一下就OK了，使用完之后，记得调用一下stop：
	public class GPSApplication extends Application { 

	@Override 
	public void onCreate() { 
	// TODO Auto-generated method stub 
	LogcatHelper.getInstance(this).start(); 
	} 
	}*/

}
