package com.eugene.zoomproject.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.eugene.zoomproject.application.ZoomApplication;

/**
 * Created by eugene on 10/22/17.
 */

public class DisplayUtil {
    private static Context sApplicationContext = ZoomApplication.sZoomApplication;
    private static ScreenInfo sScreenInfo;

    public static class ScreenInfo {
        public int screenWidth;
        public int screenHeight;
    }

    public static ScreenInfo getScreenInfo() {
        if (sScreenInfo == null) {
            DisplayMetrics displayMetrics = sApplicationContext.getResources().getDisplayMetrics();
            sScreenInfo = new ScreenInfo();
            sScreenInfo.screenWidth = displayMetrics.widthPixels;
            sScreenInfo.screenHeight = displayMetrics.heightPixels;
        }
        return sScreenInfo;
    }
}
