package com.jonny.wgsb;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import java.lang.reflect.Method;

public class SystemBarTintManager {
    public static final int DEFAULT_TINT_COLOR = 0x99000000;
    private static String sNavBarOverride;
    private final SystemBarConfig mConfig;
    private boolean mStatusBarAvailable;
    private boolean mNavBarAvailable;
    private boolean mStatusBarTintEnabled;
    private boolean mNavBarTintEnabled;
    private View mStatusBarTintView;
    private View mNavBarTintView;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
                sNavBarOverride = null;
            }
        }
    }

    @TargetApi(19)
    public SystemBarTintManager(Activity activity) {
        Window win = activity.getWindow();
        ViewGroup decorViewGroup = (ViewGroup) win.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int[] attrs = {android.R.attr.windowTranslucentStatus, android.R.attr.windowTranslucentNavigation};
            TypedArray a = activity.obtainStyledAttributes(attrs);
            try {
                mStatusBarAvailable = a.getBoolean(0, false);
                mNavBarAvailable = a.getBoolean(1, false);
            } finally {
                a.recycle();
            }
            WindowManager.LayoutParams winParams = win.getAttributes();
            int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if ((winParams.flags & bits) != 0) mStatusBarAvailable = true;
            bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            if ((winParams.flags & bits) != 0) mNavBarAvailable = true;
        }
        mConfig = new SystemBarConfig(activity, mStatusBarAvailable, mNavBarAvailable);
        if (!mConfig.hasNavigtionBar()) mNavBarAvailable = false;
        if (mConfig.isImmersiveMode(activity)) {
            mStatusBarAvailable = false;
            mNavBarAvailable = false;
        }
        if (mStatusBarAvailable) setupStatusBarView(activity, decorViewGroup);
        if (mNavBarAvailable) setupNavBarView(activity, decorViewGroup);
    }

    public void setNavigationBarTintEnabled(boolean enabled) {
        mNavBarTintEnabled = enabled;
        if (mNavBarAvailable) mNavBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    public void setTintColor(int color) {
        setStatusBarTintColor(color);
        setNavigationBarTintColor(color);
    }

    public void setTintResource(int res) {
        setStatusBarTintResource(res);
        setNavigationBarTintResource(res);
    }

    public void setTintDrawable(Drawable drawable) {
        setStatusBarTintDrawable(drawable);
        setNavigationBarTintDrawable(drawable);
    }

    public void setTintAlpha(float alpha) {
        setStatusBarAlpha(alpha);
        setNavigationBarAlpha(alpha);
    }

    public void setStatusBarTintColor(int color) {
        if (mStatusBarAvailable) mStatusBarTintView.setBackgroundColor(color);
    }

    public void setStatusBarTintResource(int res) {
        if (mStatusBarAvailable) mStatusBarTintView.setBackgroundResource(res);
    }

    @SuppressWarnings("deprecation")
    public void setStatusBarTintDrawable(Drawable drawable) {
        if (mStatusBarAvailable) mStatusBarTintView.setBackgroundDrawable(drawable);
    }

    @TargetApi(11)
    public void setStatusBarAlpha(float alpha) {
        if (mStatusBarAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mStatusBarTintView.setAlpha(alpha);
        }
    }

    public void setNavigationBarTintColor(int color) {
        if (mNavBarAvailable) mNavBarTintView.setBackgroundColor(color);
    }

    public void setNavigationBarTintResource(int res) {
        if (mNavBarAvailable) mNavBarTintView.setBackgroundResource(res);
    }

    @SuppressWarnings("deprecation")
    public void setNavigationBarTintDrawable(Drawable drawable) {
        if (mNavBarAvailable) mNavBarTintView.setBackgroundDrawable(drawable);
    }

    @TargetApi(11)
    public void setNavigationBarAlpha(float alpha) {
        if (mNavBarAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mNavBarTintView.setAlpha(alpha);
        }
    }

    public SystemBarConfig getConfig() {
        return mConfig;
    }

    public boolean isStatusBarTintEnabled() {
        return mStatusBarTintEnabled;
    }

    public void setStatusBarTintEnabled(boolean enabled) {
        mStatusBarTintEnabled = enabled;
        if (mStatusBarAvailable)
            mStatusBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    public boolean isNavBarTintEnabled() {
        return mNavBarTintEnabled;
    }

    private void setupStatusBarView(Context context, ViewGroup decorViewGroup) {
        mStatusBarTintView = new View(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mConfig.getStatusBarHeight());
        params.gravity = Gravity.TOP;
        if (mNavBarAvailable && !mConfig.isNavigationAtBottom())
            params.rightMargin = mConfig.getNavigationBarWidth();
        mStatusBarTintView.setLayoutParams(params);
        mStatusBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
        mStatusBarTintView.setVisibility(View.GONE);
        decorViewGroup.addView(mStatusBarTintView);
    }

    private void setupNavBarView(Context context, ViewGroup decorViewGroup) {
        mNavBarTintView = new View(context);
        LayoutParams params;
        if (mConfig.isNavigationAtBottom()) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, mConfig.getNavigationBarHeight());
            params.gravity = Gravity.BOTTOM;
        } else {
            params = new LayoutParams(mConfig.getNavigationBarWidth(), LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.RIGHT;
        }
        mNavBarTintView.setLayoutParams(params);
        mNavBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
        mNavBarTintView.setVisibility(View.GONE);
        decorViewGroup.addView(mNavBarTintView);
    }

    public static class SystemBarConfig {
        private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
        private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
        private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
        private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
        private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";
        private final boolean mTranslucentStatusBar;
        private final boolean mTranslucentNavBar;
        private final int mStatusBarHeight;
        private final int mActionBarHeight;
        private final boolean mHasNavigationBar;
        private final int mNavigationBarHeight;
        private final int mNavigationBarWidth;
        private final boolean mInPortrait;
        private final float mSmallestWidthDp;

        private SystemBarConfig(Activity activity, boolean translucentStatusBar, boolean traslucentNavBar) {
            Resources res = activity.getResources();
            mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
            mSmallestWidthDp = getSmallestWidthDp(activity);
            mStatusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME);
            mActionBarHeight = getActionBarHeight(activity);
            mNavigationBarHeight = getNavigationBarHeight(activity);
            mNavigationBarWidth = getNavigationBarWidth(activity);
            mHasNavigationBar = (mNavigationBarHeight > 0);
            mTranslucentStatusBar = translucentStatusBar;
            mTranslucentNavBar = traslucentNavBar;
        }

        @TargetApi(14)
        private int getActionBarHeight(Context context) {
            int result = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                TypedValue tv = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
                result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
            return result;
        }

        @TargetApi(14)
        private int getNavigationBarHeight(Context context) {
            Resources res = context.getResources();
            int result = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (hasNavBar(context)) {
                    String key;
                    if (mInPortrait) key = NAV_BAR_HEIGHT_RES_NAME;
                    else key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                    return getInternalDimensionSize(res, key);
                }
            }
            return result;
        }

        @TargetApi(14)
        private int getNavigationBarWidth(Context context) {
            Resources res = context.getResources();
            int result = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (hasNavBar(context))
                    return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME);
            }
            return result;
        }

        @TargetApi(14)
        private boolean hasNavBar(Context context) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android");
            if (resourceId != 0) {
                boolean hasNav = res.getBoolean(resourceId);
                if ("1".equals(sNavBarOverride)) hasNav = false;
                else if ("0".equals(sNavBarOverride)) hasNav = true;
                return hasNav;
            } else return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }

        private boolean isImmersiveMode(Activity activity) {
            try {
                int immersive = android.provider.Settings.System.getInt(activity.getContentResolver(), "immersive_mode");
                if (immersive == 1) return true;
            } catch (Exception ignored) {
            }
            return false;
        }

        private int getInternalDimensionSize(Resources res, String key) {
            int result = 0;
            int resourceId = res.getIdentifier(key, "dimen", "android");
            if (resourceId > 0) result = res.getDimensionPixelSize(resourceId);
            return result;
        }

        @SuppressLint("NewApi")
        private float getSmallestWidthDp(Activity activity) {
            DisplayMetrics metrics = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            else activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float widthDp = metrics.widthPixels / metrics.density;
            float heightDp = metrics.heightPixels / metrics.density;
            return Math.min(widthDp, heightDp);
        }

        public boolean isNavigationAtBottom() {
            return (mSmallestWidthDp >= 600 || mInPortrait);
        }

        public int getStatusBarHeight() {
            return mStatusBarHeight;
        }

        public int getActionBarHeight() {
            return mActionBarHeight;
        }

        public boolean hasNavigtionBar() {
            return mHasNavigationBar;
        }

        public int getNavigationBarHeight() {
            return mNavigationBarHeight;
        }

        public int getNavigationBarWidth() {
            return mNavigationBarWidth;
        }

        public int getPixelInsetTop(boolean withActionBar) {
            return mStatusBarHeight + (withActionBar ? mActionBarHeight : 0);
        }

        public int getPixelInsetBottom() {
            if (mTranslucentNavBar && isNavigationAtBottom()) return mNavigationBarHeight;
            else return 0;
        }

        public int getPixelInsetRight() {
            if (mTranslucentNavBar && !isNavigationAtBottom()) return mNavigationBarWidth;
            else return 0;
        }
    }
}