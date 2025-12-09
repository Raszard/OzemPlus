package com.etheralltda.ozem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WeightChartView extends View {

    private List<WeightEntry> data = new ArrayList<>();
    private Paint linePaint;
    private Paint fillPaint;
    private Paint axisPaint;
    private Paint textPaint;
    private Path chartPath;
    private Path fillPath;

    public WeightChartView(Context context) { super(context); init(); }
    public WeightChartView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public WeightChartView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        // Linha principal (Verde vibrante)
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#00C853"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        // Preenchimento (Gradiente)
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        // Eixos (Sutis)
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.parseColor("#EAECF0"));
        axisPaint.setStrokeWidth(2f);

        // Texto
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#667085"));
        textPaint.setTextSize(28f);

        chartPath = new Path();
        fillPath = new Path();
    }

    public void setData(List<WeightEntry> entries) {
        if (entries == null) this.data = new ArrayList<>();
        else this.data = new ArrayList<>(entries);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float pLeft = 80f, pBottom = 60f, pTop = 20f, pRight = 20f;

        if (data == null || data.isEmpty()) {
            // REFATORADO: Usando strings.xml
            String noDataText = getContext().getString(R.string.weight_chart_no_data);
            float textWidth = textPaint.measureText(noDataText);
            canvas.drawText(noDataText, (w - textWidth) / 2f, h / 2f, textPaint);
            return;
        }

        // Min/Max cálculo
        float minVal = Float.MAX_VALUE, maxVal = Float.MIN_VALUE;
        for (WeightEntry e : data) {
            if (e.getWeight() < minVal) minVal = e.getWeight();
            if (e.getWeight() > maxVal) maxVal = e.getWeight();
        }
        if (minVal == maxVal) { minVal -= 1; maxVal += 1; }

        float chartW = w - pLeft - pRight;
        float chartH = h - pTop - pBottom;
        int size = data.size();
        float stepX = (size > 1) ? chartW / (size - 1) : 0;

        // Configura gradiente dinamicamente baseado na altura
        fillPaint.setShader(new LinearGradient(0, pTop, 0, h - pBottom,
                Color.parseColor("#4D00C853"), // Verde translúcido
                Color.parseColor("#0500C853"), // Quase transparente
                Shader.TileMode.CLAMP));

        chartPath.reset();
        fillPath.reset();

        // Desenhar Caminhos
        for (int i = 0; i < size; i++) {
            float val = data.get(i).getWeight();
            // Normalizar Y (inverter pois canvas Y cresce para baixo)
            float ratio = (val - minVal) / (maxVal - minVal);
            float x = pLeft + (i * stepX);
            float y = (h - pBottom) - (ratio * chartH);

            if (i == 0) {
                chartPath.moveTo(x, y);
                fillPath.moveTo(x, h - pBottom); // Começa embaixo
                fillPath.lineTo(x, y);
            } else {
                chartPath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            // Desenhar ponto
            canvas.drawCircle(x, y, 8f, linePaint); // Ponto na linha
        }

        // Fechar caminho de preenchimento
        if (size > 0) {
            float lastX = pLeft + ((size - 1) * stepX);
            fillPath.lineTo(lastX, h - pBottom);
            fillPath.close();
            canvas.drawPath(fillPath, fillPaint);
        }

        // Desenhar linha por cima
        canvas.drawPath(chartPath, linePaint);

        // Eixo X e Y
        canvas.drawLine(pLeft, h - pBottom, w, h - pBottom, axisPaint);

        // Legendas Y (Max e Min)
        canvas.drawText(String.format("%.1f", maxVal), 10, pTop + 20, textPaint);
        canvas.drawText(String.format("%.1f", minVal), 10, h - pBottom, textPaint);
    }
}