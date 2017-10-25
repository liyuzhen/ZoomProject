package com.eugene.zoomproject.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by eugene on 10/15/17.
 * 手势缩放辅助类
 */
public class ScaleImageHelper {
    private static final String TAG = "ScaleImageHelperPro";
    private ImageView mImageView;
    private Matrix mMatrix;
    private int mWidth;
    private int mHeight;
    private int mImageWidth;
    private int mImageHeight;
    private Bitmap mBitmap;
    private Context mContext;
    private ScaleGestureDetector mScaleGestureDetector;

    // 缩放数据相关
    private float mInitScale; // 最初缩放值
    private float mCurScale;  // 当前缩放值
    private int mLastPointerCount = 0;
    private float mLastX;
    private float mLastY;

    public ScaleImageHelper(ImageView imageView) {
        this.mImageView = imageView;
        init();
    }

    private void init() {
        if (mImageView == null) {
            return;
        }
        mContext = mImageView.getContext();

        initScaleGesture();

        mMatrix = new Matrix();
        mImageView.setBackgroundColor(0xFF000000);
        mImageView.setScaleType(ImageView.ScaleType.MATRIX);
        mImageView.setImageMatrix(mMatrix);
        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initPicture();
                mImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        mImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleGestureDetector.onTouchEvent(event);
                translateImage(event);
                return true;
            }
        });
    }

    /**
     * 执行手势平移图片操作
     */
    private void translateImage(MotionEvent event) {
        RectF rectF = getMatrixRectF();
        float height = rectF.height();
        float width = rectF.width();
        int pointerCount = event.getPointerCount();

        // 多点触控， 计算平均坐标
        float averX = 0;
        float averY = 0;

        for (int i = 0; i < pointerCount; i++) {
            averX += event.getX(i);
            averY += event.getY(i);
        }

        averX /= pointerCount;
        averY /= pointerCount;

        if (mLastPointerCount != pointerCount) {
            mLastX = averX;
            mLastY = averY;
        }
        mLastPointerCount = pointerCount;

        // 标志是否可以拖拽
        boolean dragMode = false;
        if (height > mHeight || width > mWidth) {
            dragMode = true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (!dragMode) {
                    break;
                }
                float deltaX = averX - mLastX;
                float deltaY = averY - mLastY;

                if (rectF.width() > mWidth && rectF.height() < mHeight) {
                    deltaY = 0;
                }

                if (rectF.width() < mWidth && rectF.height() > mHeight) {
                    deltaX = 0;
                }

                mMatrix.postTranslate(deltaX, deltaY);
                mImageView.setImageMatrix(mMatrix);
                mLastX = averX;
                mLastY = averY;

                setTranslationPosition();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                resetTouchData();
                break;
        }
    }

    private void initScaleGesture() {
        mScaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                doScale(detector);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
    }

    /**
     * 执行缩放、 位置调整操作
     */
    private void doScale(ScaleGestureDetector detector) {
        if (detector == null) {
            return;
        }

        float scaleFactor = detector.getScaleFactor();

        // 缩放调整
        float fingerCenterX = detector.getFocusX();
        float fingerCenterY = detector.getFocusY();
        mCurScale = getCurScale();

        if (mCurScale * scaleFactor < mInitScale) {
            scaleFactor = 1.0f;
        }

        mMatrix.postScale(scaleFactor, scaleFactor, fingerCenterX, fingerCenterY);

        // 位置调整
        RectF rectF = getMatrixRectF();

        float deltaX = 0F;
        float deltaY = 0F;

        if (rectF.height() >= mHeight) {
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }

            if (rectF.bottom < mHeight) {
                deltaY = mHeight - rectF.bottom;
            }
        }

        if (rectF.width() >= mWidth) {
            if (rectF.left > 0) {
                deltaX = -rectF.left;
            }

            if (rectF.right < mWidth) {
                deltaX = mWidth - rectF.right;
            }

        }

        if (rectF.height() < mHeight) {
            deltaY = (mHeight - rectF.height()) / 2 - rectF.top;
        }

        if (rectF.width() < mWidth) {
            deltaX = (mWidth - rectF.width()) / 2 - rectF.left;
        }

        mMatrix.postTranslate(deltaX, deltaY);
        mImageView.setImageMatrix(mMatrix);
    }

    /**
     * 初始化图像
     */
    private void initPicture() {
        mWidth = mImageView.getWidth();
        mHeight = mImageView.getHeight();

        Drawable drawable = mImageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            mBitmap = ((BitmapDrawable) drawable).getBitmap();
        }

        if (mBitmap == null) {
            return;
        }

        mImageWidth = mBitmap.getWidth();
        mImageHeight = mBitmap.getHeight();


        // 缩放
        if (mWidth >= mImageWidth && mHeight <= mImageHeight) {
            mInitScale = mHeight * 1.0f / mImageHeight;
        }

        if (mWidth <= mImageWidth && mHeight >= mImageHeight) {
            mInitScale = mWidth * 1.0f / mImageWidth;
        }

        if ((mWidth <= mImageWidth && mHeight <= mImageHeight)
                || (mWidth >= mImageWidth && mHeight >= mImageHeight)) {
            float widthScale = mWidth * 1.0f / mImageWidth;
            float heightScale = mHeight * 1.0f / mImageHeight;
            mInitScale = Math.min(widthScale, heightScale);
        }

        int tranX = (int) ((mWidth - mImageWidth * mInitScale) / 2);
        int tranY = (int) ((mHeight - mImageHeight * mInitScale) / 2);
        mMatrix.setScale(mInitScale, mInitScale);
        mMatrix.postTranslate(tranX, tranY);
        mImageView.setImageMatrix(mMatrix);
    }

    /**
     * 重置触摸相关数据
     */
    private void resetTouchData() {
        mLastPointerCount = 0;
    }

    /**
     * 图片的调整矩阵
     */
    private RectF getMatrixRectF() {
        RectF rectF = new RectF();
        Drawable drawable = mImageView.getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            mMatrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 设置平移后图片的位置（边界调整）
     */
    private void setTranslationPosition() {
        RectF rectF = getMatrixRectF();

        float deltaX = 0, deltaY = 0;
        if (rectF.width() > mWidth) {
            if (rectF.left > 0) {
                deltaX = -rectF.left;
            }

            if (rectF.right < mWidth) {
                deltaX = mWidth - rectF.right;
            }
        }

        if (rectF.height() > mHeight) {
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }

            if (rectF.bottom < mHeight) {
                deltaY = mHeight - rectF.bottom;
            }
        }

        mMatrix.postTranslate(deltaX, deltaY);
        mImageView.setImageMatrix(mMatrix);
    }

    /**
     * 当前的缩放尺寸
     */
    private float getCurScale() {
        float value[] = new float[9];
        mMatrix.getValues(value);
        return value[Matrix.MSCALE_X];
    }
}
