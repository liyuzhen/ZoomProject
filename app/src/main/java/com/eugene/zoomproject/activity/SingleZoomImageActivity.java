package com.eugene.zoomproject.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.eugene.zoomproject.R;
import com.eugene.zoomproject.utils.ScaleImageHelper;

/**
 * Created by eugene on 10/22/17.
 */

public class SingleZoomImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image_test);
        new ScaleImageHelper((ImageView) findViewById(R.id.iv_image_test));
    }
}
