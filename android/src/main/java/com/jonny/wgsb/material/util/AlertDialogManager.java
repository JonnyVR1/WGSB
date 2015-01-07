package com.jonny.wgsb.material.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jonny.wgsb.material.R;

public class AlertDialogManager {
    public final void showAlertDialog(Context context, String title, String content) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .icon(context.getResources().getDrawable(R.drawable.fail))
                .positiveText(context.getString(R.string.ok))
                .show();
    }
}