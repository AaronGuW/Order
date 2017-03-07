package com.example.aaron.order;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;

import java.util.ArrayList;

/**
 * Created by Aaron on 2015/11/21.
 */
public class WeekDayPicker extends View {
    private Paint paint;
    private int textcolor;
    private int textcolorunabled;
    private float textsize;
    private String on = "1111111";
    private float twidth[] = new float[7], theight;
    private String weekday[] = new String[]{"Mo","Tu","We","Th","Fr","Sa","Su"};
    private int width,height;
    private int clickedDay = -1;


    public WeekDayPicker(Context context) {
        this(context,null);
    }

    public WeekDayPicker(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WeekDayPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.WeekDayPicker);
        textcolor = mTypedArray.getColor(R.styleable.WeekDayPicker_Textcolor, Color.WHITE);
        textcolorunabled = mTypedArray.getColor(R.styleable.WeekDayPicker_Textcolorunabled, Color.GRAY);
        textsize = mTypedArray.getDimension(R.styleable.WeekDayPicker_Textsize, 12);

        paint.setTextSize(textsize);
        twidth[0] = paint.measureText("Mo");
        twidth[1] = paint.measureText("Tu");
        twidth[2] = paint.measureText("We");
        twidth[3] = paint.measureText("Th");
        twidth[4] = paint.measureText("Fr");
        twidth[5] = paint.measureText("Sa");
        twidth[6] = paint.measureText("Su");

        mTypedArray.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        width = getWidth();
        height = getHeight();
        theight = height*11/25;
        int interval = width/7,start = interval/2,radius = height*6/32;
        for (int i = 0 ; i != 7 ; ++i) {
            if (on.substring(i,i+1).compareTo("1") == 0) {
                paint.setColor(textcolor);
                canvas.drawText(weekday[i],start+i*interval,theight,paint);
                canvas.drawCircle(start+interval*i,height-radius,radius,paint);
            } else {
                paint.setColor(textcolorunabled);
                canvas.drawText(weekday[i],start+interval*i,height-theight/2,paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int rawX = (int)event.getX();
                clickedDay = rawX/(width/7);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (clickedDay != -1) {
                    on = on.substring(0, clickedDay) + (((on.substring(clickedDay, clickedDay + 1)).compareTo("1") == 0) ? "0" : "1") + on.substring(clickedDay + 1, 7);
                    invalidate();
                }
                clickedDay = -1;
                break;
        }
        return true;
    }

    public synchronized void setWeekday(String ON) {
        on = ON;
        invalidate();
    }

    public synchronized String getWeekday() { return on; }
}
