package com.example.aaron.order;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by Aaron on 2015/11/18.
 */
public class MyListView extends ListView {
    private static boolean willIntercept = true;

    public MyListView(Context context) {
        super(context, null);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
