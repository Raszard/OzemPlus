package com.etheralltda.ozem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WeightChartView extends View {

    private List<WeightEntry> data = new ArrayList<>();

    private Paint axisPaint;
    private Paint linePaint;
    private Paint pointPaint;
    private Paint textPaint;

    public WeightChartView(Context context) {
        super(context);
        init();
    }

    public WeightChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeightChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.GRAY);
        axisPaint.setStrokeWidth(2f);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#2ECC71")); // esmeralda
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.parseColor("#27AE60"));
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(24f);
    }

    public void setData(List<WeightEntry> entries) {
        if (entries == null) {
            this.data = new ArrayList<>();
        } else {
            this.data = new ArrayList<>(entries);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float paddingLeft = 60f;
        float paddingRight = 20f;
        float paddingTop = 20f;
        float paddingBottom = 40f;

        if (data == null || data.isEmpty()) {
            canvas.drawText("Sem dados", paddingLeft, height / 2f, textPaint);
            return;
        }

        // Encontra min e max
        float minW = Float.MAX_VALUE;
        float maxW = Float.MIN_VALUE;
        for (WeightEntry e : data) {
            float w = e.getWeight();
            if (w < minW) minW = w;
            if (w > maxW) maxW = w;
        }

        if (minW == Float.MAX_VALUE || maxW == Float.MIN_VALUE) {
            canvas.drawText("Sem dados", paddingLeft, height / 2f, textPaint);
            return;
        }

        if (minW == maxW) {
            // evita divisão por zero
            minW -= 1f;
            maxW += 1f;
        }

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        // Desenha eixos
        float x0 = paddingLeft;
        float y0 = height - paddingBottom;

        // eixo X
        canvas.drawLine(x0, y0, x0 + chartWidth, y0, axisPaint);
        // eixo Y
        canvas.drawLine(x0, paddingTop, x0, y0, axisPaint);

        // Escala
        int n = data.size();
        float stepX = (n == 1) ? 0 : chartWidth / (n - 1);

        Path path = new Path();

        for (int i = 0; i < n; i++) {
            WeightEntry e = data.get(i);
            float value = e.getWeight();

            float ratio = (value - minW) / (maxW - minW);
            float x = x0 + (stepX * i);
            float y = y0 - (ratio * chartHeight);

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }

            canvas.drawCircle(x, y, 6f, pointPaint);
        }

        canvas.drawPath(path, linePaint);

        // Rótulos de min e max no eixo Y
        canvas.drawText(String.format("%.1f", maxW), 0, paddingTop + 10f, textPaint);
        canvas.drawText(String.format("%.1f", minW), 0, y0, textPaint);
    }
}
