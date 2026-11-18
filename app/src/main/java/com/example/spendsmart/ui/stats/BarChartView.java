package com.example.spendsmart.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {
    public static class Bar {
        public final String label;
        public final float value;
        public final int color;

        public Bar(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private List<Bar> bars = new ArrayList<>();

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setTextSize(28f);
        textPaint.setColor(0xFF5A6464);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setBars(List<Bar> bars) {
        this.bars = bars != null ? bars : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bars.isEmpty()) {
            return;
        }
        float max = 0.1f;
        for (Bar bar : bars) {
            max = Math.max(max, bar.value);
        }
        float width = getWidth();
        float height = getHeight();
        float labelSpace = 40f;
        float chartHeight = height - labelSpace - 8f;
        float gap = 12f;
        float barWidth = (width - gap * (bars.size() + 1)) / bars.size();
        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            float left = gap + i * (barWidth + gap);
            float barH = (bar.value / max) * chartHeight;
            float top = 8f + (chartHeight - barH);
            rect.set(left, top, left + barWidth, 8f + chartHeight);
            barPaint.setColor(bar.color);
            canvas.drawRoundRect(rect, 16f, 16f, barPaint);
            canvas.drawText(bar.label, left + barWidth / 2f, height - 8f, textPaint);
        }
    }
}
