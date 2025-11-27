package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class ConfigMedicationActivity extends AppCompatActivity {

    private EditText edtMedName;
    private EditText edtMedDose;
    private EditText edtMedFrequency;
    private EditText edtMedNextDate;
    private Button btnSaveMedication;

    private List<Medication> medications;
    private boolean isEdit = false;
    private String originalName = null; // nome antigo pra localizar na lista

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_medication);

        edtMedName = findViewById(R.id.edtMedName);
        edtMedDose = findViewById(R.id.edtMedDose);
        edtMedFrequency = findViewById(R.id.edtMedFrequency);
        edtMedNextDate = findViewById(R.id.edtMedNextDate);
        btnSaveMedication = findViewById(R.id.btnSaveMedication);

        medications = MedicationStorage.loadMedications(this);

        // Se vier medName no Intent, estamos editando
        originalName = getIntent().getStringExtra("medName");
        if (originalName != null && !originalName.trim().isEmpty()) {
            isEdit = true;
            preencherCamposParaEdicao(originalName);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSaveMedication.setOnClickListener(v -> salvarMedicamento());
    }

    private void preencherCamposParaEdicao(String medName) {
        for (Medication m : medications) {
            if (m.getName() != null && m.getName().equals(medName)) {
                edtMedName.setText(m.getName());
                edtMedDose.setText(m.getDose());
                edtMedFrequency.setText(m.getFrequency());
                edtMedNextDate.setText(m.getNextDate());
                break;
            }
        }
    }

    private void salvarMedicamento() {
        String name = edtMedName.getText().toString().trim();
        String dose = edtMedDose.getText().toString().trim();
        String freq = edtMedFrequency.getText().toString().trim();
        String nextDate = edtMedNextDate.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Informe o nome do medicamento.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEdit && originalName != null) {
            // Atualiza o existente
            boolean found = false;
            for (int i = 0; i < medications.size(); i++) {
                Medication m = medications.get(i);
                if (m.getName() != null && m.getName().equals(originalName)) {
                    m.setName(name);
                    m.setDose(dose);
                    m.setFrequency(freq);
                    m.setNextDate(nextDate);
                    found = true;
                    break;
                }
            }

            // Se por algum motivo não achar, adiciona como novo
            if (!found) {
                Medication novo = new Medication(name, dose, freq, nextDate);
                medications.add(novo);
            }
        } else {
            // Novo medicamento
            Medication novo = new Medication(name, dose, freq, nextDate);
            medications.add(novo);
        }

        MedicationStorage.saveMedications(this, medications);

        Toast.makeText(this, "Medicamento salvo.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
