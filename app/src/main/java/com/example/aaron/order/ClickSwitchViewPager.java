package com.example.aaron.order;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Aaron on 2015/11/23.
 */
public class ClickSwitchViewPager extends ViewPager{
    private boolean clicked;
    public int viewcnt;

    public ClickSwitchViewPager(Context context) {
        super(context);
    }

    public ClickSwitchViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clicked = true;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (clicked) {
                    setCurrentItem((getCurrentItem() + 1) % viewcnt);
                    invalidate();
                    clicked = false;
                }
                break;
        }
        return true;
    }
}
