package com.mustupid.pitchgram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CentsView extends View {
    private float mDeviation;
    private int mColor;
    private float mCents;
    private static final float SMOOTHNESS = 0.95f;
    private final Paint mPaint = new Paint();
    private static final float THRESHOLD = 0.25f;
    private static final int[] COLORS = new int[] {
            0xFFB71C1C, 0xFFD32F2F, 0xFFF44336, 0xFFE57373, 0xFFFFCDD2,
            0xFFBBDEFB, 0xFF64B5F6, 0xFF2196F3, 0xFF1976D2, 0xFF0D47A1
    };
    private static final float SIZE = 16;

    public CentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CentsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float step = getHeight() / 10f;
        for (int i = 0; i < 10; i++) {
            mPaint.setColor(COLORS[i]);
            canvas.drawRect(0, step * i, getWidth(), step * (i + 1), mPaint);
        }

        mPaint.setColor(mColor);
        float position = getHeight() * (0.5f - mDeviation);
        canvas.drawRect(0, position - SIZE / 2, getWidth(), position + SIZE / 2, mPaint);
    }

    void addCents(float cents, float confidence) {
        if (confidence >= 1 - THRESHOLD) {
            mCents = mCents * SMOOTHNESS + cents * (1 - SMOOTHNESS);
            mDeviation = mCents / 100 - Math.round(mCents / 100);
        }
        double gray = Math.max(0, 1 - (1 - confidence) / THRESHOLD);
        int grayInt = (int) (gray * 255 + 0.5);
        mColor = Color.argb(grayInt, 0, 0, 0);
        postInvalidate();
    }
}
