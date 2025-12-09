package com.etheralltda.ozem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MedicationDetailsActivity extends AppCompatActivity {

    private TextView txtMedName, txtMedDose, txtMedFrequency, txtMedNextDate;
    private EditText edtNotes;
    private Button btnSaveNotes, btnEditMedication;
    private String medNameOriginal;
    private Medication currentMed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_details);

        initViews();

        medNameOriginal = getIntent().getStringExtra("medName");
        loadFullMedicationData();

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFullMedicationData();
    }

    private void initViews() {
        txtMedName = findViewById(R.id.txtMedName);
        txtMedDose = findViewById(R.id.txtMedDose);
        txtMedFrequency = findViewById(R.id.txtMedFrequency);
        txtMedNextDate = findViewById(R.id.txtMedNextDate);
        edtNotes = findViewById(R.id.edtNotes);
        btnSaveNotes = findViewById(R.id.btnSaveNotes);
        btnEditMedication = findViewById(R.id.btnEditMedication);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadFullMedicationData() {
        if (medNameOriginal == null) return;

        List<Medication> list = MedicationStorage.loadMedications(this);
        currentMed = null; // Reseta antes de buscar

        for (Medication m : list) {
            if (m.getName().equals(medNameOriginal)) {
                currentMed = m;
                break;
            }
        }

        if (currentMed != null) {
            txtMedName.setText(currentMed.getName());
            txtMedDose.setText(currentMed.getDose());

            // Usa Utils para tradução dinâmica
            txtMedFrequency.setText(MedicationUtils.getLocalizedFrequency(this, currentMed.getFrequency()));
            txtMedNextDate.setText(MedicationUtils.getLocalizedNextDate(this, currentMed));

            SharedPreferences prefs = getSharedPreferences("glp1_prefs", MODE_PRIVATE);
            String noteKey = "notes_" + currentMed.getName().trim().toLowerCase().replace(" ", "_");
            edtNotes.setText(prefs.getString(noteKey, ""));
        } else {
            // Se o medicamento foi deletado ou não existe mais
            Toast.makeText(this, getString(R.string.toast_details_edit_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        btnSaveNotes.setOnClickListener(v -> {
            if (currentMed == null) return;
            String content = edtNotes.getText().toString();
            String noteKey = "notes_" + currentMed.getName().trim().toLowerCase().replace(" ", "_");

            getSharedPreferences("glp1_prefs", MODE_PRIVATE)
                    .edit().putString(noteKey, content).apply();

            Toast.makeText(this, getString(R.string.toast_details_notes_saved), Toast.LENGTH_SHORT).show();
        });

        // CORREÇÃO AQUI: Lógica manual para achar o índice pelo NOME
        btnEditMedication.setOnClickListener(v -> {
            if (currentMed != null) {
                List<Medication> list = MedicationStorage.loadMedications(this);
                int index = -1;

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getName().equals(currentMed.getName())) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    Intent intent = new Intent(this, ConfigMedicationActivity.class);
                    intent.putExtra("edit_index", index);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Erro ao abrir edição", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}