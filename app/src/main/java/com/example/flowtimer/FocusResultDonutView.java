package com.example.flowtimer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class FocusResultDonutView extends View {

    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long focusMillis;
    private long breakMillis;
    private long distractionMillis;
    private int score;

    public FocusResultDonutView(Context context) {
        super(context);
        init();
    }

    public FocusResultDonutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.ROUND);
        centerPaint.setColor(Color.WHITE);
        centerPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.rgb(46, 58, 68));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    public void setData(long focusMillis, long breakMillis, long distractionMillis, int score) {
        this.focusMillis = Math.max(0L, focusMillis);
        this.breakMillis = Math.max(0L, breakMillis);
        this.distractionMillis = Math.max(0L, distractionMillis);
        this.score = score;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = Math.min(getWidth(), getHeight()) - dp(24);
        if (size <= 0) {
            return;
        }
        float left = (getWidth() - size) / 2f;
        float top = (getHeight() - size) / 2f;
        RectF rect = new RectF(left, top, left + size, top + size);
        segmentPaint.setStrokeWidth(dp(20));
        long total = Math.max(1L, focusMillis + breakMillis + distractionMillis);
        float start = -90f;
        start = drawSegment(canvas, rect, start, focusMillis, total, Color.rgb(134, 201, 107));
        start = drawSegment(canvas, rect, start, breakMillis, total, Color.rgb(78, 163, 241));
        drawSegment(canvas, rect, start, distractionMillis, total, Color.rgb(226, 81, 81));
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, size / 2.9f, centerPaint);
        textPaint.setTextSize(dp(18));
        canvas.drawText("집중 점수", getWidth() / 2f, getHeight() / 2f - dp(6), textPaint);
        textPaint.setTextSize(dp(30));
        canvas.drawText(score + "점", getWidth() / 2f, getHeight() / 2f + dp(30), textPaint);
    }

    private float drawSegment(Canvas canvas, RectF rect, float start, long value, long total, int color) {
        if (value <= 0L) {
            return start;
        }
        float sweep = Math.max(2f, (value * 360f) / total);
        segmentPaint.setColor(color);
        canvas.drawArc(rect, start, sweep, false, segmentPaint);
        return start + sweep;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
