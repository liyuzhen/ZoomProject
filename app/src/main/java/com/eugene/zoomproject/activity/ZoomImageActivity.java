package com.eugene.zoomproject.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;

import com.eugene.zoomproject.adapter.ImageAdapter;
import com.eugene.zoomproject.R;
import com.eugene.zoomproject.utils.ScaleImageHelper;

public class ZoomImageActivity extends AppCompatActivity {
    private ImageView mImageView;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_zoom_image);

        findViewById();
        initView();
    }

    private void findViewById() {
        mImageView = (ImageView) findViewById(R.id.iv_main_image);
        mListView = (ListView) findViewById(R.id.lv_image_list);
    }

    private void initView() {
        new ScaleImageHelper(mImageView);
        mListView.setAdapter(new ImageAdapter(this));
    }
}
