package com.eugene.zoomproject.widget;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by eugene on 10/22/17.
 */

public class XListView extends ListView {
    public XListView(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}
