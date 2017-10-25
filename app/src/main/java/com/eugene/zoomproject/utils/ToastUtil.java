package com.eugene.zoomproject.utils;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.eugene.zoomproject.application.ZoomApplication;

/**
 * Created by eugene on 10/22/17.
 */

public class ToastUtil {
    private static Context sApplicationContext = ZoomApplication.sZoomApplication;
    private static Toast sToast;

    public static void showToast(String text) {
        if (sToast == null) {
            sToast = Toast.makeText(sApplicationContext, text, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(text);
            sToast.setDuration(Toast.LENGTH_SHORT);
        }
        sToast.show();
    }
}
