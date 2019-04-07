package com.mustupid.pitchgram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PitchgramView extends View {

    private final Paint mPaint = new Paint();
    private static final int MAX_WIDTH = 2000; // must >= getWidth()
    private final float[] mData = new float[MAX_WIDTH];
    private final int[] mColor = new int[MAX_WIDTH];
    private int mCursor;
    private static final float THRESHOLD = 0.25f;
    private static final float PEN_SIZE = 3;
    private static final int ANCHOR_MIDI = 26; // D1
    private static final float ANCHOR_POSITION = 3.5f;

    /**
     * Convert cents to position relative to keyboard image.
     * @param cents cents
     * @return position relative to keyboard image in [0, 1]
     */
    float centsToPostion(float cents) {
        return 1 - 1.0f / 52 * (ANCHOR_POSITION + (cents - (ANCHOR_MIDI - PitchDetector.LOWEST_NOTE) * 100) / 1200 * 7);
    }

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
        mPaint.setStrokeWidth(PEN_SIZE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(30);
        canvas.drawText(Integer.toString(Math.round(mData[mCursor])), 10, 30, mPaint);
        canvas.drawText(Integer.toString(mColor[mCursor]  & 0xff), 100, 30, mPaint);

        int size = getWidth();
        int height = getHeight();
        int j = 0;
        for (int i = mCursor + 1; i < size; i++) {
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
        mData[mCursor] = centsToPostion(cents);

        double gray = Math.min(1, (1 - confidence) / THRESHOLD);
        int grayInt = (int) (gray * 255 + 0.5);
        mColor[mCursor] = 0xff000000 | (grayInt << 16) | (grayInt <<  8) | grayInt;
        postInvalidate();
    }

}
