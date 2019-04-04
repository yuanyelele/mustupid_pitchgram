package com.mustupid.pitchgram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PitchgramView extends View {

    private final Paint mPaint = new Paint();
    private static final int SIZE = 2000; // must >= getWidth()
    private final float[] mData = new float[SIZE];
    private int mCursor;
    private static final int MAX_CENTS = 4200;
    private static final int SMOOTH_LENGTH = 3;

    public PitchgramView(Context context) {
        super(context);
    }

    public PitchgramView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PitchgramView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(3);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(30);
        if (mData[mCursor] == -1) {
            canvas.drawText("N/A", 10, 30, mPaint);
        } else {
            canvas.drawText(Integer.toString(Math.round(mData[mCursor])), 10, 30, mPaint);
        }
        int size = getWidth();
        int height = getHeight();
        int j = 0;
        for (int i = mCursor + 1; i < size; i++) {
            canvas.drawPoint(j, height - mData[i] / MAX_CENTS * height, mPaint);
            j++;
        }
        for (int i = 0; i <= mCursor; i++) {
            canvas.drawPoint(j, height - mData[i] / MAX_CENTS * height, mPaint);
            j++;
        }
    }

    void addPoint(float cents) {
        mCursor++;
        mCursor %= getWidth();
        mData[mCursor] = cents;
        smooth();
        postInvalidate();
    }

    private void smooth() {
        if (mData[mCursor] == -1)
            return;
        int size = getWidth();
        int num = 0;
        float sum = 0;
        for (int i = 0; i < SMOOTH_LENGTH; i++) {
            int j = (mCursor - i + size) % size;
            if (mData[j] != -1 && Math.abs(mData[j] - mData[mCursor]) < 100) {
                num++;
                sum += mData[j];
            }
        }
        mData[mCursor] = sum / num;
    }

}
