package com.example.spendsmart.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {
    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint holePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private List<CategorySlice> slices = new ArrayList<>();

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holePaint.setColor(0xFFF3F6F6);
        emptyPaint.setColor(0xFFE0E7E7);
        slicePaint.setStyle(Paint.Style.FILL);
    }

    public void setSlices(List<CategorySlice> slices) {
        this.slices = slices != null ? slices : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float left = (getWidth() - size) / 2f;
        float top = (getHeight() - size) / 2f;
        float inset = size * 0.08f;
        oval.set(left + inset, top + inset, left + size - inset, top + size - inset);

        if (slices.isEmpty()) {
            canvas.drawOval(oval, emptyPaint);
            float hole = size * 0.28f;
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, hole, holePaint);
            return;
        }

        float start = -90f;
        for (CategorySlice slice : slices) {
            float sweep = slice.percentage * 360f;
            slicePaint.setColor(slice.color);
            canvas.drawArc(oval, start, Math.max(sweep, 0.5f), true, slicePaint);
            start += sweep;
        }
        float hole = size * 0.28f;
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, hole, holePaint);
    }
}
