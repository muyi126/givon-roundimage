/* 
 * Copyright 2013 Share.Ltd  All rights reserved.
 * Share.Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @FileUtils.java - 2013-10-8 上午10:26:23 - Carson 
 * @author YanXu Email:981385016@qq.com
 * @version 1.0
 */

package com.alan.roundimageview;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

public class FileUtils {

	@SuppressLint("NewApi")
	public static File getExternalCacheDir(Context context) {
		File file = null;
		if (isSDCardWriteAble()) {
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
				file = context.getExternalCacheDir();
			}
			if (file == null) {
				String str = "/Android/data/" + context.getPackageName() + "/cache/";
				file = new File(Environment.getExternalStorageDirectory().getPath() + str);
			}
		} else {
			file = context.getCacheDir();
		}
		if (file != null) {
			file.mkdirs();
		}
		return file;
	}

	@SuppressLint("NewApi")
	public static boolean isExternalStorageRemovable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	public static boolean isSDCardWriteAble() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| (!isExternalStorageRemovable())) {
			return true;
		}
		return false;
	}

	private static String bytesToHexString(byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static String hashMd5Key(String key) {
		if (key == null) {
			key = "";
		} else {
			try {
				final MessageDigest mDigest = MessageDigest.getInstance("MD5");
				mDigest.update(key.getBytes());
				key = bytesToHexString(mDigest.digest());
			} catch (NoSuchAlgorithmException e) {
				key = String.valueOf(key.hashCode());
			}
		}
		return key;
	}

}
