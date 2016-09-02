package com.vunken.tv_sharehome.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.vunken.tv_sharehome.base.HuaweiSDKApplication;

import android.os.Environment;
  
/** 
 * @ClassName: ZipCompressor 
 * @CreateTime 
 * @author : 陈庚 
 * 
 */  
public class ZipCompressor {  
	
	private String tag = "ZipCompressor";
	
	/** 
     * 压缩文件-由于out要在递归调用外,所以封装一个方法用来 
     * 调用ZipFiles(ZipOutputStream out,String path,File... srcFiles) 
     * @param zip  压缩文件存放位置
     * @param path 
     * @param srcFiles 
     * @throws IOException 
     * @author isea533 
     */  
    public  void ZipFiles(File zip,String path,File... srcFiles) throws IOException{ 
    	long startTime = System.currentTimeMillis();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));  
       ZipFiles(out,path,srcFiles);  
        out.close(); 
    	long endTime = System.currentTimeMillis();
        Logger.d(tag, "文件压缩方法所需时间", endTime - startTime+"");
    }  
    /** 
     * 压缩文件-File 
     * @param zipFile  zip文件 
     * @param srcFiles 被压缩源文件 
     * @author isea533 
     */  
    private  void ZipFiles(ZipOutputStream out,String path,File... srcFiles){  
        path = path.replaceAll("\\*", "/");  
        if(!path.endsWith("/")){  
            path+="/";  
        }  
        byte[] buf = new byte[1024];  
        try {  
            for(int i=0;i<srcFiles.length;i++){  
            	if (!srcFiles[i].exists()) {
            		continue;
				}
                if(srcFiles[i].isDirectory()){  
                
                	String sdCard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                	
                    File[] files = srcFiles[i].listFiles();  
                    String srcPath = srcFiles[i].getName(); 
                    if ((HuaweiSDKApplication.getApplication().getFilesDir()+"/hrslog").equals(srcFiles[i].getAbsolutePath())) {
                    	srcPath = "app_"+srcPath;
					}else if ((sdCard_path+"/hrslog").equals(srcFiles[i].getAbsolutePath())) {
						srcPath = "sdk_"+srcPath;
					}
                    srcPath = srcPath.replaceAll("\\*", "/");  
                    if(!srcPath.endsWith("/")){  
                        srcPath+="/";  
                    }  
                    out.putNextEntry(new ZipEntry(path+srcPath));  
                    ZipFiles(out,path+srcPath,files);  
                }  
                else{  
                    FileInputStream in = new FileInputStream(srcFiles[i]);  
                    out.putNextEntry(new ZipEntry(path + srcFiles[i].getName()));  
                    int len;  
                    while((len=in.read(buf))>0){  
                        out.write(buf,0,len);  
                    }  
                    out.closeEntry();  
                    in.close();  
                }  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}  
