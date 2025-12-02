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
    private TextView txtMedName, txtMedDose, txtMedFrequency, txtMedNextDate;
    private Button btnEditMedication, btnSaveNotes;
    private EditText edtNotes;
    private String medName;

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

        Intent intent = getIntent();
        medName = intent.getStringExtra("medName");

        if (medName != null) {
            carregarDados(medName);
        } else {
            finish();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // --- CORREÇÃO AQUI ---
        btnEditMedication.setOnClickListener(v -> {
            // Carrega a lista para descobrir o índice deste medicamento
            List<Medication> lista = MedicationStorage.loadMedications(this);
            int indexEncontrado = -1;

            for (int i = 0; i < lista.size(); i++) {
                if (lista.get(i).getName().equals(medName)) {
                    indexEncontrado = i;
                    break;
                }
            }

            if (indexEncontrado != -1) {
                Intent editIntent = new Intent(this, ConfigMedicationActivity.class);
                // Passa o índice correto ("edit_index") que o ConfigMedicationActivity espera
                editIntent.putExtra("edit_index", indexEncontrado);
                startActivity(editIntent);
            } else {
                Toast.makeText(this, getString(R.string.toast_details_edit_error), Toast.LENGTH_SHORT).show();
            }
        });
        // ---------------------

        btnSaveNotes.setOnClickListener(v -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString("notes_" + medName.toLowerCase(), edtNotes.getText().toString())
                    .apply();
            Toast.makeText(this, getString(R.string.toast_details_notes_saved), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (medName != null) carregarDados(medName);
    }

    private void carregarDados(String name) {
        List<Medication> list = MedicationStorage.loadMedications(this);
        for (Medication m : list) {
            if (m.getName().equals(name)) {
                txtMedName.setText(m.getName());
                txtMedDose.setText(m.getDose());
                txtMedFrequency.setText(m.getFrequency());
                txtMedNextDate.setText(m.getNextDate());

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                edtNotes.setText(prefs.getString("notes_" + name.toLowerCase(), ""));
                return;
            }
        }
    }
}