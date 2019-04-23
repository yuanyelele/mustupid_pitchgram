package com.mustupid.pitchgram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CentsView extends View {

    private final int[] COLORS = new int[] {
            getResources().getColor(R.color.red_900),
            getResources().getColor(R.color.red_700),
            getResources().getColor(R.color.red_500),
            getResources().getColor(R.color.red_300),
            getResources().getColor(R.color.red_100),
            getResources().getColor(R.color.blue_100),
            getResources().getColor(R.color.blue_300),
            getResources().getColor(R.color.blue_500),
            getResources().getColor(R.color.blue_700),
            getResources().getColor(R.color.blue_900),
    };
    private final Paint mPaint = new Paint();
    private float mCents;
    private int mColor;

    public CentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float step = 1.0f * getHeight() / COLORS.length;
        for (int i = 0; i < COLORS.length; i++) {
            mPaint.setColor(COLORS[i]);
            canvas.drawRect(0, step * i, getWidth(), step * (i + 1), mPaint);
        }

        mPaint.setColor(mColor);
        float position = getHeight() * (0.5f - (mCents - Math.round(mCents)));
        canvas.drawRect(0, position - Settings.CENTS_INDICATOR_SIZE / 2,
                getWidth(), position + Settings.CENTS_INDICATOR_SIZE / 2, mPaint);
    }

    void addCents(float cents, float confidence) {
        cents /= 100;
        if (confidence >= Settings.threshold2) {
            if (Math.abs(mCents - cents) < 1)
                mCents = mCents * Settings.smoothness + cents * (1 - Settings.smoothness);
            else
                mCents = cents;
        }

        float alpha = Math.max(0, 1 - (1 - confidence) / (1 - Settings.threshold2));
        mColor = Color.argb(Math.round(alpha * 0xff), 0, 0, 0);

        postInvalidate();
    }
}
