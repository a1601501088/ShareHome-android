package com.vunken.tv_sharehome.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	public static boolean isNumber(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str.trim());
		if (isNum.matches()) {
			return true;
		} else {
			return false;
		}
	}

	// 保存字符串到文件中
	public static void saveAsFileWriter(String content, String file) {
		FileWriter fwriter = null;
		try {
			fwriter = new FileWriter(file);
			fwriter.write(content);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				fwriter.flush();
				fwriter.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
