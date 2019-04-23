package com.mustupid.pitchgram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class PitchgramView extends View {

    private final Paint mPaint = new Paint();
    private final float[] mData = new float[Settings.PITCHGRAM_MAX_WIDTH];
    private final int[] mColor = new int[Settings.PITCHGRAM_MAX_WIDTH];
    private int mCursor;

    public PitchgramView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(Settings.PITCHGRAM_PEN_SIZE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        int width = getWidth();
        int height = getHeight();
        int j = 0;
        for (int i = mCursor + 1; i < width; i++) {
            mPaint.setColor(mColor[i]);
            canvas.drawPoint(j, mData[i] * height, mPaint);
            j++;
        }
        for (int i = 0; i <= mCursor; i++) {
            mPaint.setColor(mColor[i]);
            canvas.drawPoint(j, mData[i] * height, mPaint);
            j++;
        }
    }

    /**
     * Convert cents to position relative to keyboard image.
     * @param cents cents
     * @return position relative to keyboard image in [0, 1]
     */
    private float centsToPosition(float cents) {
        return 1 - (cents / 100 + Settings.REFERENCE_MIDI - Settings.ANCHOR_MIDI + 0.5f) / Settings.NUM_KEYS;
    }

    void addPoint(float cents, float confidence) {
        mCursor++;
        mCursor %= getWidth();
        mData[mCursor] = centsToPosition(cents);

        float alpha = Math.max(0, 1 - (1 - confidence) / (1 - Settings.threshold2));
        mColor[mCursor] = Color.argb(Math.round(alpha * 0xff), 0, 0, 0);

        if (confidence >= Settings.threshold2) {
            ScrollView scrollView = (ScrollView) getParent().getParent();
            int scrollTop = scrollView.getScrollY();
            int scrollBottom = scrollTop + scrollView.getHeight();
            int height = getHeight();
            if (mData[mCursor] * height < scrollTop) {
                scrollView.smoothScrollBy(0, -Settings.PITCHGRAM_SCROLL_SPEED);
            } else if (mData[mCursor] * height > scrollBottom) {
                scrollView.smoothScrollBy(0, Settings.PITCHGRAM_SCROLL_SPEED);
            }
        }

        postInvalidate();
    }

}
