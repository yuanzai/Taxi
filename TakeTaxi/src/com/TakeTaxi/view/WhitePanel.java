package com.TakeTaxi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class WhitePanel extends LinearLayout 
{ 
    private Paint innerPaint, borderPaint ;
    
    public WhitePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WhitePanel(Context context) {
        super(context);
        init();
    }

    private void init() {
        innerPaint = new Paint();
        innerPaint.setARGB(255, 255, 255, 255); //gray
        innerPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setARGB(255, 0, 0, 0);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Style.STROKE);
        borderPaint.setStrokeWidth(6);
    }
    
    public void setInnerPaint(Paint innerPaint) {
        this.innerPaint = innerPaint;
    }

    public void setBorderPaint(Paint borderPaint) {
        this.borderPaint = borderPaint;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        
        RectF drawRect = new RectF();
        drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
        canvas.drawRect(drawRect, innerPaint);
        canvas.drawRect(drawRect, borderPaint);
        
        super.dispatchDraw(canvas);
    }
}