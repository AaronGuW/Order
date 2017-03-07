package com.example.aaron.order;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Aaron on 2015/11/17.
 */
public class SwitchButton extends View {

    private Paint paint;
    private int ThumbColor;
    private float ThumbRadius;
    private int BackgroundColor,shadowColor, BackgroundOffColor, ThumbOffColor, BackgroundOffFill;
    private float height;
    private float width;
    private float centerX,centerY;
    private float thumberX_ON, thumberX_OFF;
    private boolean IsOn;
    private boolean isSliding;
    private float mX;
    private RectF rect;
    private boolean switchable;
    public int setting_mode;
    public Event BindEvent;
    public wifi BindWifi;
    public Context context;
    public Handler handler;

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.SwitchButton);

        //获取自定义属性和默认值
        height = mTypedArray.getDimension(R.styleable.SwitchButton_background_height, 10);
        width = mTypedArray.getDimension(R.styleable.SwitchButton_background_width, 30);
        ThumbRadius = mTypedArray.getDimension(R.styleable.SwitchButton_ThumbRadius, 7);
        BackgroundColor = mTypedArray.getColor(R.styleable.SwitchButton_Background, Color.GREEN);
        ThumbColor = mTypedArray.getColor(R.styleable.SwitchButton_ThumbColor, Color.BLUE);
        shadowColor = mTypedArray.getColor(R.styleable.SwitchButton_shadowColor, Color.BLACK);
        BackgroundOffColor = mTypedArray.getColor(R.styleable.SwitchButton_BackgroundOffColor, Color.GRAY);
        BackgroundOffFill = mTypedArray.getColor(R.styleable.SwitchButton_BackgroundOffFill, Color.GRAY);
        ThumbOffColor = mTypedArray.getColor(R.styleable.SwitchButton_ThumbOffColor, Color.GRAY);
        IsOn = mTypedArray.getBoolean(R.styleable.SwitchButton_IsOn, false);
        mTypedArray.recycle();

        switchable = true;
        setting_mode = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        centerX = getWidth()/2;
        centerY = getHeight()/2;
        thumberX_OFF = getHeight()/2;
        thumberX_ON = getWidth() - getHeight()/2;
        rect = new RectF(centerX-width/2,centerY-height/2,centerX+width/2,centerY+height/2);
        paint.setAntiAlias(true);
        if (IsOn) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(BackgroundColor);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(BackgroundOffFill);
            canvas.drawRoundRect(rect, height / 2, height / 2, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(getHeight()/22);
            paint.setColor(BackgroundOffColor);
        }

        canvas.drawRoundRect(rect,height/2,height/2,paint);

        paint.setStyle(Paint.Style.FILL);
        if (isSliding) {
            mX = mX < 4 + ThumbRadius ? 4 + ThumbRadius:mX;
            mX = mX > getWidth() - ThumbRadius - 4 ? getWidth() - ThumbRadius - 4: mX;
            if (IsOn) {
                paint.setColor(shadowColor);
                canvas.drawCircle(mX,centerY,ThumbRadius+4,paint);
                paint.setColor(ThumbColor);
                canvas.drawCircle(mX,centerY,ThumbRadius-2,paint);
            } else {
                paint.setColor(shadowColor);
                canvas.drawCircle(mX,centerY,ThumbRadius+4, paint);
                paint.setColor(ThumbOffColor);
                canvas.drawCircle(mX,centerY,ThumbRadius-2, paint);
            }
        } else {
            if (IsOn) {
                paint.setColor(shadowColor);
                canvas.drawCircle(thumberX_ON, centerY, ThumbRadius, paint);
                paint.setColor(ThumbColor);
                canvas.drawCircle(thumberX_ON, centerY, ThumbRadius - 2, paint);
            } else {
                paint.setColor(shadowColor);
                canvas.drawCircle(thumberX_OFF, centerY, ThumbRadius, paint);
                paint.setColor(ThumbOffColor);
                canvas.drawCircle(thumberX_OFF, centerY, ThumbRadius - 2, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent Event) {
        if (switchable) {
            switch (Event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MyListView.setTouchIntercept(false);
                    MyViewPager.setTouchIntercept(false);
                    mX = Event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    if (isSliding) {
                        isSliding = false;
                        mX = Event.getX();
                        IsOn = mX > centerX;
                        if (setting_mode == 0) {
                            if (IsOn != BindEvent.getStatus()) {
                                BindEvent.setStatus(IsOn);
                                Message msg = new Message();
                                msg.arg1 = (int)BindEvent.getEid();
                                msg.arg2 = IsOn?1:0;
                                handler.sendMessage(msg);
                            }
                        }
                        else if (setting_mode == 3) {
                            if (IsOn != BindWifi.getStatus()) {
                                BindWifi.setStatus(IsOn);
                                Bundle bundle = new Bundle();
                                bundle.putString("ssid",BindWifi.getSSID());
                                bundle.putBoolean("status",IsOn);
                                Message msg = new Message();
                                msg.obj = 3;
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                            }
                        }
                        else if (setting_mode == 4 || setting_mode == 2) {
                            Message msg = new Message();
                            msg.obj = setting_mode;
                            msg.arg1 = IsOn?1:0;
                            handler.sendMessage(msg);
                        }
                        else {
                            Message msg = new Message();
                            msg.obj = setting_mode;
                            msg.what = 0;
                            handler.sendMessage(msg);
                        }
                    } else {
                        IsOn = !IsOn;
                        if (setting_mode == 0) {
                            BindEvent.setStatus(IsOn);
                            Message msg = new Message();
                            msg.arg1 = (int)BindEvent.getEid();
                            msg.arg2 = IsOn?1:0;
                            handler.sendMessage(msg);
                        }
                        else if (setting_mode == 3) {
                            BindWifi.setStatus(IsOn);
                            Bundle bundle = new Bundle();
                            bundle.putString("ssid",BindWifi.getSSID());
                            bundle.putBoolean("status",IsOn);
                            Message msg = new Message();
                            msg.obj = 3;
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                        else if (setting_mode == 4 || setting_mode == 2) {
                            Message msg = new Message();
                            msg.obj = setting_mode;
                            msg.arg1 = IsOn?1:0;
                            handler.sendMessage(msg);
                        }
                        else {
                            Message msg = new Message();
                            msg.obj = setting_mode;
                            msg.what = 0;
                            handler.sendMessage(msg);
                        }
                    }
                    MyListView.setTouchIntercept(true);
                    MyViewPager.setTouchIntercept(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    isSliding = true;
                    mX = Event.getX();
                    break;
            }
            this.invalidate();
        }
        return true;
    }

    public synchronized void setSwitchable(boolean s) { switchable = s; }
    public synchronized boolean getStatus() { return IsOn; }
    public synchronized void setStatus(boolean status) {
        if (IsOn != status) {
            IsOn = status;
            this.invalidate();
        }
    }

}
