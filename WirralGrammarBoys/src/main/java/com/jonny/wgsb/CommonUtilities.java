package com.jonny.wgsb;

import android.content.Context;
import android.content.Intent;

final class CommonUtilities {
    static final String SERVER_URL = "http://app.wirralgrammarboys.com/android/register.php";
    static final String SERVER_UPDATE_URL = "http://app.wirralgrammarboys.com/android/update.php";
    static final String SERVER_UNREGISTER_URL = "http://app.wirralgrammarboys.com/android/unregister.php";
    static final String SENDER_ID = "75999379623";
    static final String TAG = "WGSB:GCM";
    static final String DISPLAY_MESSAGE_ACTION = "com.jonny.wgsb.DISPLAY_MESSAGE";
    private static final String EXTRA_MESSAGE = "message";

    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}