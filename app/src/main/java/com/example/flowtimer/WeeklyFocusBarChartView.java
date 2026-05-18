package com.example.flowtimer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.FocusStatsSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WeeklyFocusBarChartView extends View {

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint totalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint distractionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<FocusStatsSnapshot.DailyFocusChartItem> items = new ArrayList<>();
    private final List<float[]> touchAreas = new ArrayList<>();

    public WeeklyFocusBarChartView(Context context) {
        super(context);
        init();
    }

    public WeeklyFocusBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        axisPaint.setColor(Color.rgb(216, 226, 236));
        axisPaint.setStrokeWidth(dp(1));
        textPaint.setColor(Color.rgb(46, 58, 68));
        textPaint.setTextSize(dp(10));
        textPaint.setTextAlign(Paint.Align.CENTER);
        totalPaint.setColor(Color.rgb(78, 163, 241));
        focusPaint.setColor(Color.rgb(134, 201, 107));
        distractionPaint.setColor(Color.rgb(226, 81, 81));
    }

    public void setItems(List<FocusStatsSnapshot.DailyFocusChartItem> chartItems) {
        items.clear();
        if (chartItems != null) {
            items.addAll(chartItems);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        touchAreas.clear();
        if (items.isEmpty()) {
            canvas.drawText("표시할 기록이 없습니다.", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        int left = dp(28);
        int right = getWidth() - dp(12);
        int top = dp(18);
        int bottom = getHeight() - dp(34);
        int chartHeight = Math.max(dp(80), bottom - top);
        long maxValue = 1L;
        for (FocusStatsSnapshot.DailyFocusChartItem item : items) {
            maxValue = Math.max(maxValue, Math.max(item.getTotalDurationMillis(), Math.max(item.getEffectiveFocusDurationMillis(), item.getDistractionDurationMillis())));
        }

        canvas.drawLine(left, bottom, right, bottom, axisPaint);
        canvas.drawLine(left, top, left, bottom, axisPaint);

        float groupWidth = (right - left) / 7f;
        float barWidth = Math.max(dp(5), groupWidth / 5f);
        for (int i = 0; i < items.size(); i++) {
            FocusStatsSnapshot.DailyFocusChartItem item = items.get(i);
            float centerX = left + (groupWidth * i) + (groupWidth / 2f);
            drawBar(canvas, centerX - barWidth, bottom, chartHeight, item.getTotalDurationMillis(), maxValue, totalPaint, barWidth);
            drawBar(canvas, centerX, bottom, chartHeight, item.getEffectiveFocusDurationMillis(), maxValue, focusPaint, barWidth);
            drawBar(canvas, centerX + barWidth, bottom, chartHeight, item.getDistractionDurationMillis(), maxValue, distractionPaint, barWidth);
            canvas.drawText(toShortDate(item.getLabel()), centerX, getHeight() - dp(12), textPaint);
            touchAreas.add(new float[]{centerX - groupWidth / 2f, top, centerX + groupWidth / 2f, bottom, i});
        }
    }

    private void drawBar(Canvas canvas, float x, int bottom, int chartHeight, long value, long maxValue, Paint paint, float width) {
        float ratio = maxValue <= 0L ? 0f : (float) value / (float) maxValue;
        float height = Math.max(value > 0L ? dp(3) : 0, chartHeight * ratio);
        canvas.drawRect(x - width / 2f, bottom - height, x + width / 2f, bottom, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }
        for (float[] area : touchAreas) {
            if (event.getX() >= area[0] && event.getX() <= area[2] && event.getY() >= area[1] && event.getY() <= area[3]) {
                int index = (int) area[4];
                if (index >= 0 && index < items.size()) {
                    FocusStatsSnapshot.DailyFocusChartItem item = items.get(index);
                    if (hasRecord(item)) {
                        showSummary(item);
                    }
                }
                return true;
            }
        }
        return true;
    }

    private boolean hasRecord(FocusStatsSnapshot.DailyFocusChartItem item) {
        return item.getTotalDurationMillis() > 0L
                || item.getEffectiveFocusDurationMillis() > 0L
                || item.getDistractionDurationMillis() > 0L;
    }

    private void showSummary(FocusStatsSnapshot.DailyFocusChartItem item) {
        TextView messageView = new TextView(getContext());
        messageView.setText(createSummaryText(item));
        messageView.setTextSize(15f);
        int padding = dp(18);
        messageView.setPadding(padding, padding / 2, padding, padding / 2);
        new AlertDialog.Builder(getContext())
                .setTitle(item.getLabel() + " 집중 요약")
                .setView(messageView)
                .setPositiveButton("확인", null)
                .show();
    }

    private SpannableStringBuilder createSummaryText(FocusStatsSnapshot.DailyFocusChartItem item) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        appendColoredLabel(builder, "총 집중 시간", DurationFormatter.formatShortDuration(item.getTotalDurationMillis()), Color.rgb(78, 163, 241));
        builder.append("\n");
        appendColoredLabel(builder, "실제 집중 시간", DurationFormatter.formatShortDuration(item.getEffectiveFocusDurationMillis()), Color.rgb(134, 201, 107));
        builder.append("\n");
        appendColoredLabel(builder, "집중 방해 시간", DurationFormatter.formatShortDuration(item.getDistractionDurationMillis()), Color.rgb(226, 81, 81));
        return builder;
    }

    private void appendColoredLabel(SpannableStringBuilder builder, String label, String value, int color) {
        int start = builder.length();
        builder.append(label);
        builder.setSpan(new ForegroundColorSpan(color), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(": ").append(value);
    }

    private String toShortDate(String label) {
        if (label == null) {
            return "";
        }
        if (label.length() >= 5) {
            return label.substring(label.length() - 5);
        }
        return label;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
