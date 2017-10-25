package com.eugene.zoomproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.eugene.zoomproject.R;
import com.eugene.zoomproject.utils.ToastUtil;

/**
 * Created by eugene on 10/22/17.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

    }

    private void initUI() {
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_main_image_test:
                ToastUtil.showToast("单图片缩放测试");
                intent = new Intent(this, SingleZoomImageActivity.class);
                break;
            case R.id.btn_main_image:
                ToastUtil.showToast("图片缩放");
                intent = new Intent(this, ZoomImageActivity.class);
                break;
            case R.id.btn_main_video:
                ToastUtil.showToast("视频缩放");
                intent = new Intent(this, ZoomImageActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
