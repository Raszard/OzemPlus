package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightActivity extends AppCompatActivity {

    private EditText edtWeight;
    private Button btnSaveWeight;
    private Button btnExportWeight;
    private WeightChartView weightChart;
    private RecyclerView recyclerWeightHistory;

    private WeightHistoryAdapter historyAdapter;
    private List<WeightEntry> entries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        edtWeight = findViewById(R.id.edtWeight);
        btnSaveWeight = findViewById(R.id.btnSaveWeight);
        btnExportWeight = findViewById(R.id.btnExportWeight);
        weightChart = findViewById(R.id.weightChart);
        recyclerWeightHistory = findViewById(R.id.recyclerWeightHistory);

        recyclerWeightHistory.setLayoutManager(new LinearLayoutManager(this));
        entries.clear();
        entries.addAll(WeightStorage.loadWeights(this));
        historyAdapter = new WeightHistoryAdapter(entries);
        recyclerWeightHistory.setAdapter(historyAdapter);

        weightChart.setData(entries);

        btnSaveWeight.setOnClickListener(v -> salvarPeso());
        btnExportWeight.setOnClickListener(v -> exportarHistorico());
    }

    @Override
    protected void onResume() {
        super.onResume();
        recarregarDados();
    }

    private void recarregarDados() {
        entries.clear();
        entries.addAll(WeightStorage.loadWeights(this));
        historyAdapter.notifyDataSetChanged();
        weightChart.setData(entries);
    }

    private void salvarPeso() {
        String wStr = edtWeight.getText().toString().trim();
        if (wStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_weight_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        float w;
        try {
            w = Float.parseFloat(wStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.toast_weight_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        if (w <= 0) {
            Toast.makeText(this, getString(R.string.toast_weight_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        WeightEntry entry = new WeightEntry(now, w);
        WeightStorage.addWeight(this, entry);

        // Atualiza peso atual no perfil, se existir
        UserProfile profile = UserStorage.loadUserProfile(this);
        if (profile != null) {
            profile.setCurrentWeight(w);
            UserStorage.saveUserProfile(this, profile);
        }

        edtWeight.setText("");
        Toast.makeText(this, getString(R.string.toast_weight_saved), Toast.LENGTH_SHORT).show();

        recarregarDados();
    }

    private void exportarHistorico() {
        if (entries.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_weight_export_no_data), Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.weight_title)).append("\n");
        sb.append("--------------------------------\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // do mais antigo para o mais recente
        for (WeightEntry e : entries) {
            String dateStr = sdf.format(new Date(e.getTimestamp()));
            sb.append(String.format(Locale.getDefault(), "%s - %.1f kg", dateStr, e.getWeight()))
                    .append("\n");
        }

        String texto = sb.toString();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.weight_export_subject));
        sendIntent.putExtra(Intent.EXTRA_TEXT, texto);

        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.weight_export_title));
        startActivity(chooser);
    }
}
