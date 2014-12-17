package com.jonny.wgsb.material.util;

import android.os.Build;

public class CompatUtils {
    public static boolean isNotLegacyApi11() {
        return Build.VERSION.SDK_INT >= 11;
    }

    public static boolean isNotLegacyApi13() {
        return Build.VERSION.SDK_INT >= 13;
    }

    public static boolean isNotLegacyHoneyComb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isNotLegacyJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isNotLegacyLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}