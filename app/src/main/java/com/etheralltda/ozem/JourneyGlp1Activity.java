package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JourneyGlp1Activity extends AppCompatActivity {

    private TextView txtDaysBadge;
    private TextView txtInfoWeight;
    private TextView txtInfoDiff;
    private TextView txtNauseaScore;
    private TextView txtFatigueScore;
    private TextView txtSatietyScore;
    private LineChart chartWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_glp1);

        txtDaysBadge = findViewById(R.id.txtDaysBadge);
        txtInfoWeight = findViewById(R.id.txtInfoWeight);
        txtInfoDiff = findViewById(R.id.txtInfoDiff);
        txtNauseaScore = findViewById(R.id.txtNauseaScore);
        txtFatigueScore = findViewById(R.id.txtFatigueScore);
        txtSatietyScore = findViewById(R.id.txtSatietyScore);
        chartWeight = findViewById(R.id.chartWeight);

        // Back "←" volta para a tela anterior
        TextView txtBack = findViewById(R.id.txtBack);
        txtBack.setOnClickListener(v -> onBackPressed());

        carregarResumoPeso();
        carregarSintomas();
        configurarGraficoPeso();
    }

    private void carregarResumoPeso() {
        UserProfile profile = UserStorage.loadUserProfile(this);
        if (profile != null && profile.getCurrentWeight() > 0) {
            float current = profile.getCurrentWeight();
            txtInfoWeight.setText(String.format(Locale.getDefault(), "%.1f kg", current));

            float target = profile.getTargetWeight();
            if (target > 0) {
                float diff = current - target; // positivo = acima da meta
                String diffText = String.format(Locale.getDefault(), "%.1f kg", diff);
                txtInfoDiff.setText(diffText);

                if (diff > 0) {
                    // ainda acima da meta
                    txtInfoDiff.setTextColor(Color.parseColor("#D32F2F")); // vermelho
                } else if (diff < 0) {
                    // abaixo da meta (perdeu peso)
                    txtInfoDiff.setTextColor(Color.parseColor("#2E7D32")); // verde
                } else {
                    txtInfoDiff.setTextColor(Color.parseColor("#616161")); // neutro
                }
            } else {
                txtInfoDiff.setText("+0.0 kg");
                txtInfoDiff.setTextColor(Color.parseColor("#616161"));
            }
        } else {
            txtInfoWeight.setText("-- kg");
            txtInfoDiff.setText("+0.0 kg");
            txtInfoDiff.setTextColor(Color.parseColor("#616161"));
        }

        // Por enquanto, deixa o "25 dias" fixo.
        // Depois podemos calcular baseado na data de início do tratamento.
        txtDaysBadge.setText("25 dias");
    }

    private void carregarSintomas() {
        List<SymptomEntry> sintomas = SymptomStorage.loadSymptoms(this);
        if (sintomas != null && !sintomas.isEmpty()) {
            SymptomEntry last = sintomas.get(sintomas.size() - 1);
            txtNauseaScore.setText(last.getNausea() + "/10");
            txtFatigueScore.setText(last.getFatigue() + "/10");
            txtSatietyScore.setText(last.getSatiety() + "/10");
        } else {
            txtNauseaScore.setText("0/10");
            txtFatigueScore.setText("0/10");
            txtSatietyScore.setText("0/10");
        }
    }

    private void configurarGraficoPeso() {
        if (chartWeight == null) return;

        // EXEMPLO: dados fictícios só para ver o gráfico funcionando.
        // Depois podemos trocar para o histórico real de pesos.
        List<Entry> entries = new ArrayList<>();
        // 10 pontos simulando leve queda de peso
        for (int i = 0; i < 10; i++) {
            float x = i;
            float y = 86f - i * 0.4f;
            entries.add(new Entry(x, y));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Peso (kg)");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setColor(Color.parseColor("#7E57C2"));
        dataSet.setCircleColor(Color.parseColor("#7E57C2"));

        LineData lineData = new LineData(dataSet);
        chartWeight.setData(lineData);

        chartWeight.getDescription().setEnabled(false);
        chartWeight.getAxisRight().setEnabled(false);
        chartWeight.getLegend().setEnabled(false);

        XAxis xAxis = chartWeight.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        chartWeight.getAxisLeft().setDrawGridLines(true);

        chartWeight.invalidate();
    }
}
