package com.jonny.wgsb.material.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jonny.wgsb.material.R;

public class AlertDialogManager {
    public final void showAlertDialog(Context context, String title, String content) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .icon(ContextCompat.getDrawable(context, R.drawable.fail))
                .positiveText(context.getString(R.string.ok))
                .show();
    }
}