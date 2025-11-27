package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.slider.Slider; // Importante: Material Slider

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SymptomsActivity extends AppCompatActivity {

    // Componentes atualizados
    private Slider sliderNausea;
    private Slider sliderFatigue;
    private Slider sliderSatiety;

    private EditText edtSymptomNotes;
    private Button btnSaveSymptom;
    private Button btnExportSymptoms;
    private RecyclerView recyclerSymptoms;

    private SymptomHistoryAdapter adapter;
    private List<SymptomEntry> entries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        // Vincular novos IDs
        sliderNausea = findViewById(R.id.sliderNausea);
        sliderFatigue = findViewById(R.id.sliderFatigue);
        sliderSatiety = findViewById(R.id.sliderSatiety);

        edtSymptomNotes = findViewById(R.id.edtSymptomNotes);
        btnSaveSymptom = findViewById(R.id.btnSaveSymptom);
        btnExportSymptoms = findViewById(R.id.btnExportSymptoms);
        recyclerSymptoms = findViewById(R.id.recyclerSymptoms);

        // Configurar RecyclerView
        recyclerSymptoms.setLayoutManager(new LinearLayoutManager(this));
        entries.clear();
        entries.addAll(SymptomStorage.loadSymptoms(this));
        adapter = new SymptomHistoryAdapter(entries);
        recyclerSymptoms.setAdapter(adapter);

        btnSaveSymptom.setOnClickListener(v -> salvarRegistro());
        btnExportSymptoms.setOnClickListener(v -> exportarDiario());
    }

    @Override
    protected void onResume() {
        super.onResume();
        recarregarDados();
    }

    private void salvarRegistro() {
        // Ler valores dos Sliders (retornam float, convertemos para int)
        int nausea = (int) sliderNausea.getValue();
        int fatigue = (int) sliderFatigue.getValue();
        int satiety = (int) sliderSatiety.getValue();
        String notes = edtSymptomNotes.getText().toString().trim();

        if (nausea == 0 && fatigue == 0 && satiety == 0 && notes.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_symptoms_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        SymptomEntry entry = new SymptomEntry(now, nausea, fatigue, satiety, notes);
        SymptomStorage.addSymptom(this, entry);

        // Resetar campos
        edtSymptomNotes.setText("");
        sliderNausea.setValue(0);
        sliderFatigue.setValue(0);
        sliderSatiety.setValue(0);

        Toast.makeText(this, getString(R.string.toast_symptoms_saved), Toast.LENGTH_SHORT).show();
        recarregarDados();
    }

    private void recarregarDados() {
        entries.clear();
        entries.addAll(SymptomStorage.loadSymptoms(this));
        adapter.notifyDataSetChanged();
    }

    private void exportarDiario() {
        if (entries.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_symptoms_export_no_data), Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.symptoms_title)).append("\n");
        sb.append("--------------------------------\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (SymptomEntry e : entries) {
            String dateStr = sdf.format(new Date(e.getTimestamp()));
            sb.append(
                    String.format(Locale.getDefault(),
                            "%s - Náusea: %d, Cansaço: %d, Saciedade: %d",
                            dateStr,
                            e.getNausea(),
                            e.getFatigue(),
                            e.getSatiety()
                    )
            );
            String notes = e.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                sb.append(" - ").append(notes.trim());
            }
            sb.append("\n");
        }

        String texto = sb.toString();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.symptoms_export_subject));
        sendIntent.putExtra(Intent.EXTRA_TEXT, texto);

        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.symptoms_export_title));
        startActivity(chooser);
    }
}