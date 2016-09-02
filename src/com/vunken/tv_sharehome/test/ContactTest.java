package com.vunken.tv_sharehome.test;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.util.Log;
import android.widget.Toast;


public class ContactTest extends AndroidTestCase{
	
	public void test1() {
		ContentResolver cr = getContext().getContentResolver();
    	//先查询raw_contacts表，获取最新联系人的主键，然后主键+1，就是要插入的联系人的id
    	Cursor cursorContactId = cr.query(Uri.parse("content://com.android.contacts/raw_contacts"), new String[]{"_id"}, null, null, null);
    	//默认联系人id就是1
    	int contact_id = 1;
    	for (int i = 0; i < 5; i++) {
			
		
    	
    	if(cursorContactId.moveToLast()){
    		//拿到主键
    		int _id = cursorContactId.getInt(0);
    		//主键+1，就是要插入的联系人id
    		contact_id = ++_id;
    	}
    	
    	ContentValues values = new ContentValues();
    	values.put("contact_id", contact_id);
    	//把联系人id插入raw_contacts数据库
    	cr.insert(Uri.parse("content://com.android.contacts/raw_contacts"), values);
    	
    	values.clear();
    	values.put("data1", "二bi"+i);
    	values.put("mimetype", "vnd.android.cursor.item/name");
    	values.put("raw_contact_id", contact_id);
    	cr.insert(Uri.parse("content://com.android.contacts/data"), values);
    	
    	values.clear();
    	values.put("data1", "1344567"+i);
    	values.put("mimetype", "vnd.android.cursor.item/phone_v2");
    	values.put("raw_contact_id", contact_id);
    	cr.insert(Uri.parse("content://com.android.contacts/data"), values);
    	}
	}
	
	
	public void test2() {
		String pass = "Aaaas2@";
		/**
         * 特殊字符
         */
        String regEx1 = "^[`~!@#$%^&*()+=|{}':;',.<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]{1,}$";

        String regEx2 = "^[0-9]{1,}$";
        String regEx3 = "^[a-z]{1,}$";
        String regEx4 = "^[A-Z]{1,}$";
        int i = 0;
        if( pass.matches(regEx1)) i++;
        if( pass.matches(regEx2)) i++;
        if( pass.matches(regEx3)) i++;
        if( pass.matches(regEx4)) i++;
	}
	
	
	public void test3(Context context){
		
		String serviceString = Context.DOWNLOAD_SERVICE;  
		final DownloadManager downloadManager;  
		downloadManager = (DownloadManager)context.getSystemService(serviceString);  
		  
		Uri uri = Uri.parse("http://developer.android.com/shareables/icon_templates-v4.0.zip");  
		DownloadManager.Request request = new Request(uri);  
		request.setAllowedNetworkTypes(Request.NETWORK_WIFI); 
		request.setTitle("标题");  
		request.setDescription("描述"); 
		request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//		request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/a/a.apk")));
//		request.setDestinationInExternalPublicDir(dirType, subPath)(Environment.DIRECTORY_MUSIC,"Android_Rock.mp3");
		//保存路径
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,"Android_Rock.mp3");
		//可以被系统扫描到此文件
		request.setVisibleInDownloadsUi(true);
		//是否允许网络漫游
		request.setAllowedOverRoaming(true);
		final long myDownloadReference = downloadManager.enqueue(request);  
		
		IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);  
	      
		BroadcastReceiver receiver = new BroadcastReceiver() {  
		  @Override  
		  public void onReceive(Context context, Intent intent) {  
		    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);  
		    if (myDownloadReference == reference) {  
		       //下载完成
		    	//根据下载id查询
		    	Query myDownloadQuery = new Query();  
		        myDownloadQuery.setFilterById(reference);  
		           //从系统数据库中查询 
		        Cursor myDownload = downloadManager.query(myDownloadQuery);  
		        if (myDownload.moveToFirst()) {//把游标移动至 第一个
		          int fileNameIdx =   
		            myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);  
		          int fileUriIdx =   
		            myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);  
		          int sofarx = myDownload.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);  
		         // DownloadManager.s
		          String fileName = myDownload.getString(fileNameIdx);  //文件名
		          String fileUri = myDownload.getString(fileUriIdx);  //文件下载到了哪个路径
		          //当前进度大小，不过这个实时变化的要在内容观查都中获取
		          int sofar = myDownload.getInt(sofarx);  
		          //总进度大小
		          int bytes_total = myDownload.getInt(myDownload.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));  
		          Log.d("tag", fileName + " : " + fileUri);  
		        }  
		        myDownload.close();  
		    }  
		  }  
		};  
		context.registerReceiver(receiver, filter);  
		//取消指定的下载
//		downloadManager.remove(myDownloadReference);
		

	}
	
	private static boolean isNeedDownload(Context context, long id, DownloadManager downloadManager){  
		  
        boolean isNeedDownloadAgain = true;  
  
        DownloadManager.Query query = new DownloadManager.Query();  
        query.setFilterById(id);  
        Cursor cursor = downloadManager.query(query);  
        if(cursor != null && cursor.moveToFirst()){  
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);  
            int status = cursor.getInt(columnIndex);  
            int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);  
            int reason = cursor.getInt(columnReason);  
  
            switch(status){  
                case DownloadManager.STATUS_FAILED:  
                    switch(reason){  
                        case DownloadManager.ERROR_CANNOT_RESUME:  
                            //some possibly transient error occurred but we can't resume the download  
                            break;  
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:  
                            //no external storage device was found. Typically, this is because the SD card is not mounted  
                            break;  
                        case DownloadManager.ERROR_FILE_ALREADY_EXISTS:  
                            //the requested destination file already exists (the download manager will not overwrite an existing file)  
                            break;  
                        case DownloadManager.ERROR_FILE_ERROR:  
                            //a storage issue arises which doesn't fit under any other error code  
                            break;  
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:  
                            //an error receiving or processing data occurred at the HTTP level  
                            break;  
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE://sd卡满了  
                            //here was insufficient storage space. Typically, this is because the SD card is full  
                            break;  
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:  
                            //there were too many redirects  
                            break;  
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:  
                            //an HTTP code was received that download manager can't handle  
                            break;  
                        case DownloadManager.ERROR_UNKNOWN:  
                            //he download has completed with an error that doesn't fit under any other error code  
                            break;  
                    }  
                    isNeedDownloadAgain = true;  
  
                    
                    Toast.makeText(context, "开始重新下载更新!", 0).show();
                    break;  
                case DownloadManager.STATUS_PAUSED:  
  
                    switch(reason){  
                        case DownloadManager.PAUSED_QUEUED_FOR_WIFI:  
                            //the download exceeds a size limit for downloads over the mobile network and the download manager is waiting for a Wi-Fi connection to proceed  
  
                            break;  
                        case DownloadManager.PAUSED_UNKNOWN:  
                            //the download is paused for some other reason  
                            break;  
                        case DownloadManager.PAUSED_WAITING_FOR_NETWORK:  
                            //the download is waiting for network connectivity to proceed  
                            break;  
                        case DownloadManager.PAUSED_WAITING_TO_RETRY:  
                            //the download is paused because some network error occurred and the download manager is waiting before retrying the request  
                            break;  
                    }  
                    isNeedDownloadAgain = false;  
  
                  
                    Toast.makeText(context, "下载已暂停，请继续下载", 0).show();
                    break;  
                case DownloadManager.STATUS_PENDING:  
                    //the download is waiting to start  
                    isNeedDownloadAgain = false;  
                    Toast.makeText(context, "更新正在下载！", 0).show();
                    break;  
                case DownloadManager.STATUS_RUNNING:  
                    //the download is currently running  
                    isNeedDownloadAgain = false;  
                    Toast.makeText(context, "更新正在下载！", 0).show();
                  
                    break;  
                case DownloadManager.STATUS_SUCCESSFUL:  
                    //the download has successfully completed  
                    isNeedDownloadAgain = false;  
                  //  installApk(id, downloadManager, context);  
                    break;  
            }  
  
        }  
        return isNeedDownloadAgain;  
    } 
	
}
