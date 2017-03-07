package com.example.aaron.order;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Aaron on 2015/11/18.
 */
public class MyViewPager extends ViewPager {
    private Context context;
    private static boolean willIntercept = true;

    public MyViewPager(Context context) {
        super(context);
        this.context = context;
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if(willIntercept){
            return super.onInterceptTouchEvent(arg0);
        }else{
            return false;
        }

    }

    public static void setTouchIntercept(boolean value){
        willIntercept = value;
    }
}
