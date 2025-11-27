package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InjectionActivity extends AppCompatActivity {

    private Spinner spInjectionMed;
    private RadioGroup rgLocation;
    private RadioButton rbAbdomen, rbThigh, rbArm;
    private TextView txtInjectionDiagram;
    private TextView txtLastInjectionInfo;
    private TextView txtNextSuggestionInfo;
    private Button btnConfirmInjection;
    private Button btnExportInjectionHistory;
    private RecyclerView recyclerInjections;

    private List<Medication> medications = new ArrayList<>();
    private List<InjectionEntry> entries = new ArrayList<>();
    private InjectionHistoryAdapter adapter;

    private SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private String preselectedMedName; // vindo da notificação (opcional)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_injection);

        spInjectionMed = findViewById(R.id.spInjectionMed);
        rgLocation = findViewById(R.id.rgLocation);
        rbAbdomen = findViewById(R.id.rbAbdomen);
        rbThigh = findViewById(R.id.rbThigh);
        rbArm = findViewById(R.id.rbArm);
        txtInjectionDiagram = findViewById(R.id.txtInjectionDiagram);
        txtLastInjectionInfo = findViewById(R.id.txtLastInjectionInfo);
        txtNextSuggestionInfo = findViewById(R.id.txtNextSuggestionInfo);
        btnConfirmInjection = findViewById(R.id.btnConfirmInjection);
        btnExportInjectionHistory = findViewById(R.id.btnExportInjectionHistory);
        recyclerInjections = findViewById(R.id.recyclerInjections);

        // Lê o medicamento enviado pela notificação (se vier)
        Intent intent = getIntent();
        preselectedMedName = intent.getStringExtra("medName");

        carregarMedicamentos();
        configurarRecycler();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        txtInjectionDiagram.setText(getString(R.string.injection_rotation_diagram));

        btnConfirmInjection.setOnClickListener(v -> confirmarAplicacao());
        btnExportInjectionHistory.setOnClickListener(v -> exportarHistorico());

        spInjectionMed.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                atualizarDiagrama();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                atualizarDiagrama();
            }
        });

        atualizarDiagrama();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recarregarDados();
    }

    private void carregarMedicamentos() {
        medications.clear();
        medications.addAll(MedicationStorage.loadMedications(this));

        if (medications.isEmpty()) {
            Toast.makeText(this, getString(R.string.injection_no_meds), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        List<String> names = new ArrayList<>();
        for (Medication m : medications) {
            String n = m.getName();
            if (n == null || n.trim().isEmpty()) n = "Medicamento";
            names.add(n);
        }

        ArrayAdapter<String> adapterSpin = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                names
        );
        adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInjectionMed.setAdapter(adapterSpin);

        // Se a Activity foi aberta a partir de uma notificação com medName, seleciona o medicamento
        if (preselectedMedName != null) {
            int index = -1;
            for (int i = 0; i < medications.size(); i++) {
                String n = medications.get(i).getName();
                if (n != null && n.equals(preselectedMedName)) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                spInjectionMed.setSelection(index);
            }
        }
    }

    private void configurarRecycler() {
        recyclerInjections.setLayoutManager(new LinearLayoutManager(this));
        entries.clear();
        entries.addAll(InjectionStorage.loadInjections(this));
        adapter = new InjectionHistoryAdapter(entries);
        recyclerInjections.setAdapter(adapter);
    }

    private void recarregarDados() {
        entries.clear();
        entries.addAll(InjectionStorage.loadInjections(this));
        adapter.notifyDataSetChanged();
        atualizarDiagrama();
    }

    private String getSelectedMedicationName() {
        int pos = spInjectionMed.getSelectedItemPosition();
        if (pos < 0 || pos >= medications.size()) return null;
        Medication m = medications.get(pos);
        String name = m.getName();
        if (name == null || name.trim().isEmpty()) name = "Medicamento";
        return name;
    }

    private void atualizarDiagrama() {
        String medName = getSelectedMedicationName();
        if (medName == null) {
            txtLastInjectionInfo.setText(getString(R.string.injection_last_none));
            txtNextSuggestionInfo.setText(getString(R.string.injection_next_start));
            return;
        }

        InjectionEntry last = InjectionStorage.getLastInjectionForMed(this, medName);
        if (last == null) {
            txtLastInjectionInfo.setText(getString(R.string.injection_last_none));
            txtNextSuggestionInfo.setText(getString(R.string.injection_next_start));
            return;
        }

        String dateStr = sdfDate.format(new Date(last.getTimestamp()));
        String lastLocLabel = getLocationLabel(last.getLocationCode());

        String lastText = getString(R.string.injection_last_label) + " " + dateStr + " - " + lastLocLabel;
        txtLastInjectionInfo.setText(lastText);

        String nextCode = getNextLocationCode(last.getLocationCode());
        String nextLabel = getLocationLabel(nextCode);
        String nextText = getString(R.string.injection_next_label) + " " + nextLabel;
        txtNextSuggestionInfo.setText(nextText);
    }

    private String getNextLocationCode(String code) {
        if ("abdomen".equals(code)) return "thigh";
        if ("thigh".equals(code)) return "arm";
        if ("arm".equals(code)) return "abdomen";
        return "abdomen";
    }

    private String getLocationLabel(String code) {
        if ("abdomen".equals(code)) {
            return getString(R.string.injection_location_abdomen);
        } else if ("thigh".equals(code)) {
            return getString(R.string.injection_location_thigh);
        } else if ("arm".equals(code)) {
            return getString(R.string.injection_location_arm);
        }
        return code != null ? code : "";
    }

    private void confirmarAplicacao() {
        String medName = getSelectedMedicationName();
        if (medName == null) {
            Toast.makeText(this, getString(R.string.toast_injection_med_required), Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgLocation.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, getString(R.string.toast_injection_location_required), Toast.LENGTH_SHORT).show();
            return;
        }

        String locationCode;
        if (checkedId == rbAbdomen.getId()) {
            locationCode = "abdomen";
        } else if (checkedId == rbThigh.getId()) {
            locationCode = "thigh";
        } else {
            locationCode = "arm";
        }

        long now = System.currentTimeMillis();
        InjectionEntry entry = new InjectionEntry(now, medName, locationCode);
        InjectionStorage.addInjection(this, entry);

        Toast.makeText(this, getString(R.string.toast_injection_saved), Toast.LENGTH_SHORT).show();

        recarregarDados();
    }

    private void exportarHistorico() {
        if (entries.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_injection_export_no_data), Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.injection_title)).append("\n");
        sb.append("--------------------------------\n\n");

        for (InjectionEntry e : entries) {
            String dateStr = sdfDateTime.format(new Date(e.getTimestamp()));
            String medName = e.getMedicationName();
            if (medName == null || medName.trim().isEmpty()) {
                medName = "Medicamento";
            }
            String locLabel = getLocationLabel(e.getLocationCode());
            sb.append(
                    String.format(Locale.getDefault(),
                            "%s - %s - %s",
                            dateStr,
                            medName,
                            locLabel
                    )
            ).append("\n");
        }

        String texto = sb.toString();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.injection_export_subject));
        sendIntent.putExtra(Intent.EXTRA_TEXT, texto);

        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.injection_export_title));
        startActivity(chooser);
    }
}
