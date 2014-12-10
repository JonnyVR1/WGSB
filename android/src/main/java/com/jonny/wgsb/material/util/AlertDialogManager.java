package com.jonny.wgsb.material.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.jonny.wgsb.material.R;

public class AlertDialogManager {
    public final void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(message).setCancelable(false);
        if (status != null) builder.setIcon((status) ? R.drawable.success : R.drawable.fail);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}