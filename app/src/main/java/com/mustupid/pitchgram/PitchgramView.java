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
    private static final int MAX_WIDTH = 2000; // must >= getWidth()
    private final float[] mData = new float[MAX_WIDTH];
    private final int[] mColor = new int[MAX_WIDTH];
    private int mCursor;
    private static final float THRESHOLD = 0.25f;
    private static final float PEN_SIZE = 3;
    private static final int ANCHOR_MIDI = 21;

    /**
     * Convert cents to position relative to keyboard image.
     * @param cents cents
     * @return position relative to keyboard image in [0, 1]
     */
    private float centsToPosition(float cents) {
        return 1 - (cents / 100 + PitchDetector.LOWEST_NOTE - ANCHOR_MIDI + 0.5f) / 88;
    }

    public PitchgramView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(PEN_SIZE);
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

    void addPoint(float cents, float confidence) {
        mCursor++;
        mCursor %= getWidth();
        mData[mCursor] = centsToPosition(cents);

        double gray = Math.min(1, (1 - confidence) / THRESHOLD);
        int grayInt = (int) (gray * 255 + 0.5);
        mColor[mCursor] = 0xff000000 | (grayInt << 16) | (grayInt <<  8) | grayInt;

        if (confidence >= THRESHOLD) {
            ScrollView scrollView = (ScrollView) getParent().getParent();
            int scrollTop = scrollView.getScrollY();
            int scrollBottom = scrollTop + scrollView.getHeight();
            int height = getHeight();
            if (mData[mCursor] * height < scrollTop) {
                scrollView.smoothScrollBy(0, -10);
            } else if (mData[mCursor] * height > scrollBottom) {
                scrollView.smoothScrollBy(0, 10);
            }
        }

        postInvalidate();
    }

}
