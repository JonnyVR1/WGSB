package com.jonny.wgsb;

import android.os.Build;

class CompatUtils {
    public static boolean isNotLegacyApi11() {
        return Build.VERSION.SDK_INT >= 11;
    }
    public static boolean isNotLegacyApi13() {
        return Build.VERSION.SDK_INT >= 13;
    }
    public static boolean isNotLegacyHoneyComb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
}