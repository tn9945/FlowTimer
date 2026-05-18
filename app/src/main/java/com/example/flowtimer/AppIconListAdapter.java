package com.example.flowtimer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppIconListAdapter extends BaseAdapter {

    private final Context context;
    private final CharSequence[] labels;
    private final Drawable[] icons;

    public AppIconListAdapter(Context context, CharSequence[] labels, Drawable[] icons) {
        this.context = context;
        this.labels = labels;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return labels == null ? 0 : labels.length;
    }

    @Override
    public Object getItem(int position) {
        return labels[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(12), dp(18), dp(12));

        ImageView iconView = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        iconParams.rightMargin = dp(14);
        iconView.setLayoutParams(iconParams);
        if (icons != null && position < icons.length && icons[position] != null) {
            iconView.setImageDrawable(icons[position]);
        }
        row.addView(iconView);

        TextView labelView = new TextView(context);
        labelView.setText(labels[position]);
        labelView.setTextSize(15f);
        labelView.setTextColor(context.getColor(R.color.flow_text_primary));
        row.addView(labelView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
