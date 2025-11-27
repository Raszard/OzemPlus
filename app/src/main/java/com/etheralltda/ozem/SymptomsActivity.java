package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SymptomsActivity extends AppCompatActivity {

    private Spinner spNausea;
    private Spinner spFatigue;
    private Spinner spSatiety;
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

        spNausea = findViewById(R.id.spNausea);
        spFatigue = findViewById(R.id.spFatigue);
        spSatiety = findViewById(R.id.spSatiety);
        edtSymptomNotes = findViewById(R.id.edtSymptomNotes);
        btnSaveSymptom = findViewById(R.id.btnSaveSymptom);
        btnExportSymptoms = findViewById(R.id.btnExportSymptoms);
        recyclerSymptoms = findViewById(R.id.recyclerSymptoms);

        configurarSpinners();

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

    private void configurarSpinners() {
        String[] options = new String[]{
                "0 - Não senti",
                "1 - Muito leve",
                "2 - Leve",
                "3 - Moderado",
                "4 - Forte",
                "5 - Muito forte"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spNausea.setAdapter(adapter);
        spFatigue.setAdapter(adapter);
        spSatiety.setAdapter(adapter);

        // Começar em 0 (não senti)
        spNausea.setSelection(0);
        spFatigue.setSelection(0);
        spSatiety.setSelection(0);
    }

    private int getIntensityFromSpinner(Spinner spinner) {
        String value = (String) spinner.getSelectedItem();
        if (value == null || value.isEmpty()) return 0;
        try {
            String first = value.split(" ")[0];
            return Integer.parseInt(first);
        } catch (Exception e) {
            return 0;
        }
    }

    private void salvarRegistro() {
        int nausea = getIntensityFromSpinner(spNausea);
        int fatigue = getIntensityFromSpinner(spFatigue);
        int satiety = getIntensityFromSpinner(spSatiety);
        String notes = edtSymptomNotes.getText().toString().trim();

        if (nausea == 0 && fatigue == 0 && satiety == 0 && notes.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_symptoms_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        SymptomEntry entry = new SymptomEntry(now, nausea, fatigue, satiety, notes);
        SymptomStorage.addSymptom(this, entry);

        edtSymptomNotes.setText("");
        spNausea.setSelection(0);
        spFatigue.setSelection(0);
        spSatiety.setSelection(0);

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

        // Do mais antigo para o mais recente
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
