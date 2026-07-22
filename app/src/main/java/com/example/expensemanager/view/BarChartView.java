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
 * A small, dependency-free grouped bar chart: two bars (income, expense) per
 * period with a short label underneath and a grow-in animation.
 */
public class BarChartView extends View {

    public static class Group {
        public final String label;
        public final float income;
        public final float expense;

        public Group(String label, float income, float expense) {
            this.label = label;
            this.income = income;
            this.expense = expense;
        }
    }

    private final List<Group> groups = new ArrayList<>();
    private int incomeColor = 0xFF00B894;
    private int expenseColor = 0xFFFF6B6B;
    private int labelColor = 0xFF8A8FA6;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private float maxValue = 0f;
    private float animatedFraction = 1f;
    @Nullable private ValueAnimator animator;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(dp(11));
    }

    public void setColors(int income, int expense, int label) {
        incomeColor = income;
        expenseColor = expense;
        labelColor = label;
        invalidate();
    }

    public void setData(List<Group> data) {
        groups.clear();
        maxValue = 0f;
        if (data != null) {
            groups.addAll(data);
            for (Group g : data) {
                maxValue = Math.max(maxValue, Math.max(g.income, g.expense));
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
        if (groups.isEmpty()) {
            return;
        }

        float labelArea = dp(22);
        float baseline = getHeight() - labelArea;
        float top = dp(6);
        float usableHeight = baseline - top;

        int n = groups.size();
        float groupWidth = (float) getWidth() / n;
        float barWidth = dp(13);
        float innerGap = dp(3);
        float radius = dp(4);

        for (int i = 0; i < n; i++) {
            Group g = groups.get(i);
            float centerX = groupWidth * i + groupWidth / 2f;

            float incomeHeight = barHeight(g.income, usableHeight);
            float expenseHeight = barHeight(g.expense, usableHeight);

            float leftX = centerX - barWidth - innerGap / 2f;
            barPaint.setColor(incomeColor);
            barRect.set(leftX, baseline - incomeHeight, leftX + barWidth, baseline);
            canvas.drawRoundRect(barRect, radius, radius, barPaint);

            float rightX = centerX + innerGap / 2f;
            barPaint.setColor(expenseColor);
            barRect.set(rightX, baseline - expenseHeight, rightX + barWidth, baseline);
            canvas.drawRoundRect(barRect, radius, radius, barPaint);

            labelPaint.setColor(labelColor);
            canvas.drawText(g.label, centerX, getHeight() - dp(6), labelPaint);
        }
    }

    private float barHeight(float value, float usableHeight) {
        if (maxValue <= 0f) {
            return 0f;
        }
        return value / maxValue * usableHeight * animatedFraction;
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
