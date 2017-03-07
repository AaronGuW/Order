package com.example.aaron.order;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Aaron on 2015/11/22.
 */
public class NonSlideViewPager extends ViewPager {

    public NonSlideViewPager(Context context) {
        super(context);
    }

    public NonSlideViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
