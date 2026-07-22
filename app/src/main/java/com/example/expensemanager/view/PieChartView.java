package com.example.expensemanager.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A small, dependency-free donut chart. Feed it {@link Slice}s and it draws
 * proportional arcs with a short grow-in animation.
 */
public class PieChartView extends View {

    public static class Slice {
        public final float value;
        public final int color;

        public Slice(float value, int color) {
            this.value = value;
            this.color = color;
        }
    }

    private final List<Slice> slices = new ArrayList<>();
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();

    private float total = 0f;
    private float animatedFraction = 1f;
    @Nullable private ValueAnimator animator;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setStrokeWidth(dp(24));
    }

    public void setData(List<Slice> data) {
        slices.clear();
        total = 0f;
        if (data != null) {
            slices.addAll(data);
            for (Slice s : data) {
                total += s.value;
            }
        }
        startAnimation();
    }

    private void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        animatedFraction = 0f;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(650);
        animator.addUpdateListener(a -> {
            animatedFraction = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (slices.isEmpty() || total <= 0f) {
            return;
        }

        float stroke = arcPaint.getStrokeWidth();
        float pad = stroke / 2f + dp(4);
        float size = Math.min(getWidth(), getHeight());
        float left = (getWidth() - size) / 2f + pad;
        float top = (getHeight() - size) / 2f + pad;
        bounds.set(left, top, left + size - 2 * pad, top + size - 2 * pad);

        final float gap = slices.size() > 1 ? 3f : 0f; // degrees of spacing between slices
        final float sweepBudget = 360f * animatedFraction;

        float startAngle = -90f;
        float consumed = 0f;
        for (Slice s : slices) {
            float fullSweep = 360f * (s.value / total);
            float remaining = sweepBudget - consumed;
            if (remaining <= 0f) {
                break;
            }
            float sweep = Math.min(fullSweep - gap, remaining - gap);
            if (sweep > 0f) {
                arcPaint.setColor(s.color);
                canvas.drawArc(bounds, startAngle + gap / 2f, sweep, false, arcPaint);
            }
            startAngle += fullSweep;
            consumed += fullSweep;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
        }
        super.onDetachedFromWindow();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
