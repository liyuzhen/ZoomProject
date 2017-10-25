package com.eugene.zoomproject.application;

import android.app.Application;
import android.widget.Toast;

import com.eugene.zoomproject.utils.ToastUtil;

/**
 * Created by eugene on 10/22/17.
 */

public class ZoomApplication extends Application {
    public static ZoomApplication sZoomApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sZoomApplication = this;
    }
}

