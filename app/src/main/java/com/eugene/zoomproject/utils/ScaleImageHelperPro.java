package com.eugene.zoomproject.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by eugene on 10/15/17.
 * 手势缩放辅助类
 */
public class ScaleImageHelperPro {
    private static final String TAG = "ScaleImageHelperPro";
    private ImageView mOriginalImageView;
    private ImageView mNewImageView;
    private WindowManager mWindowManager;
    private Context mContext;
    private ScaleGestureDetector mScaleGestureDetector;

    // 缩放数据相关
    private float mLastTouchAverX;
    private float mLastTouchAverY;

    private float mInitPictureScale = 0;
    private boolean mIsPictureStateChanged;  // 标识当前图片是否有手动缩放或手动平移

    private boolean mIsAnimating = false;
    private float mLastTranDeltaX = 0;
    private float mLastTranDeltaY = 0;

    private Matrix mNewImageViewMatrix;

    public ScaleImageHelperPro(ImageView imageView) {
        this.mOriginalImageView = imageView;
        init();
    }

    private void init() {
        if (mOriginalImageView == null) {
            return;
        }
        mContext = mOriginalImageView.getContext();

        initScaleGesture();

        mOriginalImageView.setScaleType(ImageView.ScaleType.MATRIX);
        mOriginalImageView.setImageMatrix(new Matrix());
        mOriginalImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initOriginPictureLocation();
                mOriginalImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        mOriginalImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mIsAnimating) {
                    return true;
                }

                if (checkIsNeedTouchEvent(event)) {
                    requestDisallowParentInterceptTouchEvent(true);
                    createOrResetNewImageView();
                    mScaleGestureDetector.onTouchEvent(event);
                    translateImage(event);
                } else {
                    backToOriginalLocationWithAnim();
                }
                return true;
            }
        });
    }

    /**
     * 判断是否需要 Touch 事件
     */
    private boolean checkIsNeedTouchEvent(MotionEvent event) {
        return event != null && event.getPointerCount() >= 2;
    }

    // TODO: 10/22/17 解决冲突问题， 解法比较恶心， 待完善
    private void requestDisallowParentInterceptTouchEvent(boolean disallow) {
        if (mOriginalImageView == null) {
            return;
        }
        mOriginalImageView.getParent().requestDisallowInterceptTouchEvent(disallow);
    }

    private void createOrResetNewImageView() {
        if (mIsAnimating || mIsPictureStateChanged) {
            return;
        }

        if (mNewImageView == null) {
            mNewImageView = new ImageView(mContext);
            mNewImageView.setScaleType(ImageView.ScaleType.MATRIX);
            mNewImageViewMatrix = new Matrix();
            mNewImageView.setImageMatrix(mNewImageViewMatrix);
            mNewImageView.setImageDrawable(mOriginalImageView.getDrawable());

            final DisplayUtil.ScreenInfo screenInfo = DisplayUtil.getScreenInfo();
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(screenInfo.screenWidth, screenInfo.screenHeight);
            // TODO: 10/22/17 此处状态栏半透明有 API 限制
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
            params.format = PixelFormat.TRANSPARENT;
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            mWindowManager.addView(mNewImageView, params);  // 此处需申请权限

            mNewImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    adjustNewPictureLocation();
                    mNewImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        } else {
            adjustNewPictureLocation();
            mNewImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化图像
     */
    private void initOriginPictureLocation() {
        int width = mOriginalImageView.getWidth();
        int height = mOriginalImageView.getHeight();
        final ImageView imageView = mOriginalImageView;
        Bitmap bitmap = null;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }

        if (bitmap == null) {
            return;
        }

        Matrix matrix = imageView.getImageMatrix();
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        float scaleValue = 1.0f;
        // 缩放
        if (width >= imageWidth && height <= imageHeight) {
            scaleValue = height * 1.0f / imageHeight;
        }

        if (width <= imageWidth && height >= imageHeight) {
            scaleValue = width * 1.0f / imageWidth;
        }

        if ((width <= imageWidth && height <= imageHeight)
                || (width >= imageWidth && height >= imageHeight)) {
            float widthScale = width * 1.0f / imageWidth;
            float heightScale = height * 1.0f / imageHeight;
            scaleValue = Math.min(widthScale, heightScale);
        }

        int tranX = (int) ((width - imageWidth * scaleValue) / 2);
        int tranY = (int) ((height - imageHeight * scaleValue) / 2);
        matrix.setScale(scaleValue, scaleValue);
        matrix.postTranslate(tranX, tranY);
        imageView.setImageMatrix(matrix);
        mInitPictureScale = scaleValue;
    }


    /**
     * 执行手势平移图片操作
     */
    private void translateImage(MotionEvent event) {
        if (mNewImageView == null) {
            return;
        }
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

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = averX - mLastTouchAverX;
                float deltaY = averY - mLastTouchAverY;

                if (mLastTouchAverX == 0 && mLastTouchAverY == 0) {
                    deltaX = 0;
                    deltaY = 0;
                }

                mNewImageViewMatrix.postTranslate(deltaX, deltaY);
                mNewImageView.setImageMatrix(mNewImageViewMatrix);
                mLastTouchAverX = averX;
                mLastTouchAverY = averY;
                onPictureStateChanged();
                break;
        }
    }

    /**
     * 重置触摸平移相关数据
     */
    private void resetTouchData() {
        mLastTouchAverX = 0;
        mLastTouchAverY = 0;
    }

    private void initScaleGesture() {
        mScaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                doScale(detector);
                Log.e(TAG, "matrix = " + mNewImageViewMatrix.toString());
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                if (mNewImageView == null) {
                    return false;
                }
                onPictureStateChanged();
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        });
    }

    /**
     * 图像变化调用
     */
    private void onPictureStateChanged() {
        mIsPictureStateChanged = true;
        mOriginalImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOriginalImageView.setVisibility(View.INVISIBLE);
            }
        }, 50);
    }

    /**
     * 执行缩放操作
     */
    private void doScale(ScaleGestureDetector detector) {
        if (detector == null) {
            return;
        }

        // 缩放调整
        // 由于手势作用在 mOriginalImageView 上， 得出了 fingerCenterX 为相对于父布局的位置
        // 所以需要转化为作用在 mNewImageView 上的数值
        RectF rectf = ScaleUtil.getViewLocationInWindow(mOriginalImageView);
        float fingerCenterX = detector.getFocusX() + rectf.left;
        float fingerCenterY = detector.getFocusY() + rectf.top;
        float scaleFactor = detector.getScaleFactor();
        float curScale = ScaleUtil.getCurScale(mNewImageViewMatrix);
        if (curScale * scaleFactor - mInitPictureScale < 1e-15) {
            scaleFactor = curScale / mInitPictureScale;
        }
        mNewImageViewMatrix.postScale(scaleFactor, scaleFactor, fingerCenterX, fingerCenterY);
        mNewImageView.setImageMatrix(mNewImageViewMatrix);
        mNewImageView.invalidate();
    }

    /**
     * 调整图片的位置
     */
    private void adjustNewPictureLocation() {
        RectF rect = ScaleUtil.getImageLocationInWindow(mOriginalImageView);
        float curScaleValue = ScaleUtil.getCurScale(mOriginalImageView.getImageMatrix());
        mNewImageViewMatrix = new Matrix();
        mNewImageViewMatrix.postScale(curScaleValue, curScaleValue);
        mNewImageViewMatrix.postTranslate(rect.left, rect.top);
        mNewImageView.setImageMatrix(mNewImageViewMatrix);
    }

    /**
     * 执行动画回到原来位置
     */
    private void backToOriginalLocationWithAnim() {
        if (!mIsPictureStateChanged) {
            return;
        }

        final RectF curPictureRect = ScaleUtil.getImageLocationInWindow(mNewImageView);
        RectF originPictureRect = ScaleUtil.getImageLocationInWindow(mOriginalImageView);
        final float deltaX = originPictureRect.width() / 2 + originPictureRect.left - (curPictureRect.width() / 2 + curPictureRect.left);
        final float deltaY = originPictureRect.height() / 2 + originPictureRect.top - (curPictureRect.height() / 2 + curPictureRect.top);
        final float newScale = ScaleUtil.getCurScale(mNewImageView.getImageMatrix());
        final float originScale = ScaleUtil.getCurScale(mOriginalImageView.getImageMatrix());
        final float deltaScale = originScale - newScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mNewImageViewMatrix = mNewImageView.getImageMatrix();
                float scaleFactor = (newScale + deltaScale * value) / ScaleUtil.getCurScale(mNewImageView.getImageMatrix());
                RectF curPictureRect = ScaleUtil.getImageLocationInWindow(mNewImageView);
                float centerX = curPictureRect.left + curPictureRect.width() / 2;
                float centerY = curPictureRect.top + curPictureRect.height() / 2;

                mNewImageViewMatrix.postScale(scaleFactor, scaleFactor, centerX, centerY);
                mNewImageViewMatrix.postTranslate(deltaX * value - mLastTranDeltaX, deltaY * value - mLastTranDeltaY);
                mLastTranDeltaX = deltaX * value;
                mLastTranDeltaY = deltaY * value;
                mNewImageView.setImageMatrix(mNewImageViewMatrix);
                mNewImageView.invalidate();
                requestDisallowParentInterceptTouchEvent(true);
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                resetNewImageViewData();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }
        });
        valueAnimator.setDuration(500);
        valueAnimator.start();
    }

    /**
     * 重置 NewImageView 相关属性
     */
    private void resetNewImageViewData() {
        mOriginalImageView.setVisibility(View.VISIBLE);
        mLastTranDeltaX = 0;
        mLastTranDeltaY = 0;

        mOriginalImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mNewImageView.setVisibility(View.GONE);
            }
        }, 100);

        mIsPictureStateChanged = false;
        resetTouchData();
        requestDisallowParentInterceptTouchEvent(false);
    }


    public void onDetroy() {
        if (mWindowManager != null) {
            mWindowManager.removeView(mNewImageView);
        }
    }

}
