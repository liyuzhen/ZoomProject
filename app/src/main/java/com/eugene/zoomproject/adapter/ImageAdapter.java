package com.eugene.zoomproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.eugene.zoomproject.R;
import com.eugene.zoomproject.utils.ScaleImageHelper;
import com.eugene.zoomproject.utils.ScaleImageHelperPro;

/**
 * Created by eugene on 10/22/17.
 */

public class ImageAdapter extends BaseAdapter {
    private static final String TAG = "ImageAdapter";
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    public ImageAdapter(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.layout_item_image, null);
            new ScaleImageHelperPro((ImageView) convertView.findViewById(R.id.iv_image));
        }
        return convertView;
    }
}
