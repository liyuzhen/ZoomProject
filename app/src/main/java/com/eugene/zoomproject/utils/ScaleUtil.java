package com.eugene.zoomproject.utils;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by eugene on 10/25/17.
 * 缩放平移辅助工具类
 */
public class ScaleUtil {
    /**
     * 图片的调整矩阵
     */
    public static RectF getMatrixRectF(ImageView imageView) {
        if (imageView == null) {
            return null;
        }
        RectF rectF = new RectF();
        Drawable drawable = imageView.getDrawable();
        Matrix matrix = imageView.getImageMatrix();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 当前的缩放尺寸
     */
    public static float getCurScale(Matrix matrix) {
        float value[] = new float[9];
        matrix.getValues(value);
        return value[Matrix.MSCALE_X];
    }

    /**
     * 获取 View 的位置
     */
    public static RectF getImageLocationInWindow(ImageView imageView) {
        if (imageView == null) {
            return null;
        }
        RectF pictureRect = getMatrixRectF(imageView);

        int[] locations = new int[2];
        RectF viewRect = new RectF();
        imageView.getLocationInWindow(locations);
        viewRect.left = locations[0] + pictureRect.left;
        viewRect.top = locations[1] + pictureRect.top;
        viewRect.right = viewRect.left + pictureRect.width();
        viewRect.bottom = viewRect.top + pictureRect.height();
        return viewRect;
    }

    public static RectF getViewLocationInWindow(ImageView imageView) {
        if (imageView == null) {
            return null;
        }
        RectF viewRect = new RectF();
        int[] locations = new int[2];
        imageView.getLocationInWindow(locations);
        viewRect.left = locations[0];
        viewRect.top = locations[1];
        viewRect.right = viewRect.left + imageView.getWidth();
        viewRect.bottom = viewRect.top + imageView.getHeight();
        return viewRect;
    }
}
