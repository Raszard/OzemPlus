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

        // Se a activity for aberta, carrega os dados
        if (medName != null) {
            carregarDados(medName);
        } else {
            finish();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnEditMedication.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, ConfigMedicationActivity.class);
            editIntent.putExtra("medName", medName);
            startActivity(editIntent);
        });

        btnSaveNotes.setOnClickListener(v -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString("notes_" + medName.toLowerCase(), edtNotes.getText().toString())
                    .apply();
            Toast.makeText(this, "Notas salvas.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega ao voltar da edição
        if (medName != null) carregarDados(medName);
    }

    private void carregarDados(String name) {
        List<Medication> list = MedicationStorage.loadMedications(this);
        for (Medication m : list) {
            if (m.getName().equals(name)) {
                txtMedName.setText(m.getName());
                txtMedDose.setText(m.getDose());
                txtMedFrequency.setText(m.getFrequency());

                // Aqui exibe o texto gerado (ex: "Toda Terça às 08:00")
                txtMedNextDate.setText(m.getNextDate());

                // Carrega notas
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                edtNotes.setText(prefs.getString("notes_" + name.toLowerCase(), ""));
                return;
            }
        }
    }
}