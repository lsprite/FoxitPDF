package com.nemo.foxitpdf.util;

import android.text.TextUtils;
import android.util.Log;

public class LogUtil {
	public static final boolean DEBUG = false;
	private static final String TAG = "LOG_NEMO";

	public static void e(String s) {
		if (DEBUG) {
			if (DEBUG) {
				if (TextUtils.isEmpty(s)) {
					Log.e(TAG, "传入的string是null");
				} else {
					Log.e(TAG, s);
				}
			}
		}
	}

	public static void e(String tag, String s) {
		if (TextUtils.isEmpty(tag)) {
			tag = TAG;
		}
		if (DEBUG) {
			if (DEBUG) {
				if (TextUtils.isEmpty(s)) {
					Log.e(tag, "传入的string是null");
				} else {
					Log.e(tag, s);
				}
			}
		}
	}

	public static void log(String tag, String s) {
		if (TextUtils.isEmpty(tag)) {
			tag = TAG;
		}
		if (DEBUG) {
			if (DEBUG) {
				if (TextUtils.isEmpty(s)) {
					Log.e(tag, "传入的string是null");
				} else {
					Log.e(tag, s);
				}
			}
		}
	}
}
