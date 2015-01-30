package com.jonny.wgsb.material.util;

import android.content.Context;
import android.content.Intent;

public final class CommonUtilities {
    public static final String SERVER_URL = "http://app.wirralgrammarboys.com/android/register.php";
    public static final String SERVER_UPDATE_URL = "http://app.wirralgrammarboys.com/android/update.php";
    public static final String SERVER_UNREGISTER_URL = "http://app.wirralgrammarboys.com/android/unregister.php";
    public static final String SENDER_ID = "75999379623";
    public static final String TAG = "WGSB:GCM";
    public static final String DISPLAY_MESSAGE_ACTION = "com.jonny.wgsb.material.DISPLAY_MESSAGE";
    private static final String EXTRA_MESSAGE = "message";

    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
