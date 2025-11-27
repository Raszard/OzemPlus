package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MedicationDetailsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "glp1_prefs";

    private TextView txtMedName;
    private TextView txtMedDose;
    private TextView txtMedFrequency;
    private TextView txtMedNextDate;
    private Button btnEditMedication;
    private Button btnSaveNotes;
    private EditText edtNotes;

    private String medName; // usado pra notas e editar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_details);

        txtMedName = findViewById(R.id.txtMedName);
        txtMedDose = findViewById(R.id.txtMedDose);
        txtMedFrequency = findViewById(R.id.txtMedFrequency);
        txtMedNextDate = findViewById(R.id.txtMedNextDate);
        btnEditMedication = findViewById(R.id.btnEditMedication);
        btnSaveNotes = findViewById(R.id.btnSaveNotes);
        edtNotes = findViewById(R.id.edtNotes);

        // Lê os dados enviados pelo adapter
        Intent intent = getIntent();
        medName = intent.getStringExtra("medName");
        String medDose = intent.getStringExtra("medDose");
        String medFreq = intent.getStringExtra("medFreq");
        String medNextDate = intent.getStringExtra("medNextDate");

        if (medName == null || medName.trim().isEmpty()) {
            Toast.makeText(this, "Medicamento não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtMedName.setText(valueOrDash(medName));
        txtMedDose.setText(valueOrDash(medDose));
        txtMedFrequency.setText(valueOrDash(medFreq));
        txtMedNextDate.setText(valueOrDash(medNextDate));

        // Carrega anotações existentes
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String notesKey = getNotesKeyForName(medName);
        String notes = prefs.getString(notesKey, "");
        edtNotes.setText(notes);

        // Editar medicamento (abre tela de configuração)
        btnEditMedication.setOnClickListener(v -> {
            Intent editIntent = new Intent(MedicationDetailsActivity.this, ConfigMedicationActivity.class);
            editIntent.putExtra("medName", medName);
            startActivity(editIntent);
        });

        // Salvar anotações
        btnSaveNotes.setOnClickListener(v -> {
            String text = edtNotes.getText().toString().trim();
            prefs.edit().putString(notesKey, text).apply();
            Toast.makeText(MedicationDetailsActivity.this,
                    getString(R.string.toast_notes_saved),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Quando volta da tela de edição, recarrega dose/frequência/data
        if (medName != null) {
            List<Medication> list = MedicationStorage.loadMedications(this);
            for (Medication m : list) {
                if (m.getName() != null && m.getName().equals(medName)) {
                    txtMedDose.setText(valueOrDash(m.getDose()));
                    txtMedFrequency.setText(valueOrDash(m.getFrequency()));
                    txtMedNextDate.setText(valueOrDash(m.getNextDate()));
                    break;
                }
            }

            // E recarrega as notas (caso algo tenha mudado)
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String notes = prefs.getString(getNotesKeyForName(medName), "");
            edtNotes.setText(notes);
        }
    }

    private String valueOrDash(String v) {
        if (v == null || v.trim().isEmpty()) return "-";
        return v;
    }

    private String getNotesKeyForName(String name) {
        String keyName = (name == null) ? "" : name.trim().toLowerCase().replace(" ", "_");
        return "notes_" + keyName;
    }
}
