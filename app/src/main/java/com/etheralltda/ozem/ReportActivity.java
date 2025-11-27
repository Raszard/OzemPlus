package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private TextView btnBackReport;
    private TextView txtReportDate;
    private TextView txtReportWeight;
    private TextView txtReportTarget;

    private LinearLayout containerMeds;
    private LinearLayout containerInjections;
    private LinearLayout containerSymptoms;

    private Button btnExportFinal;

    private static final String PREFS_NAME = "glp1_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initViews();
        loadData();

        btnBackReport.setOnClickListener(v -> finish());
        btnExportFinal.setOnClickListener(v -> exportarResumo());
    }

    private void initViews() {
        btnBackReport = findViewById(R.id.btnBackReport);
        txtReportDate = findViewById(R.id.txtReportDate);
        txtReportWeight = findViewById(R.id.txtReportWeight);
        txtReportTarget = findViewById(R.id.txtReportTarget);

        containerMeds = findViewById(R.id.containerMeds);
        containerInjections = findViewById(R.id.containerInjections);
        containerSymptoms = findViewById(R.id.containerSymptoms);

        btnExportFinal = findViewById(R.id.btnExportFinal);

        String dateNow = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        txtReportDate.setText("Gerado em: " + dateNow);
    }

    private void loadData() {
        // 1. Dados do Perfil
        UserProfile profile = UserStorage.loadUserProfile(this);
        if (profile != null) {
            txtReportWeight.setText(String.format(Locale.getDefault(), "%.1f kg", profile.getCurrentWeight()));
            txtReportTarget.setText(String.format(Locale.getDefault(), "%.1f kg", profile.getTargetWeight()));
        }

        // 2. Medicamentos
        List<Medication> meds = MedicationStorage.loadMedications(this);
        containerMeds.removeAllViews();
        if (meds.isEmpty()) {
            addEmptyRow(containerMeds, "Nenhum medicamento ativo.");
        } else {
            for (Medication m : meds) {
                addTextRow(containerMeds, "💊 " + m.getName(), m.getDose() + " - " + m.getFrequency());
            }
        }

        // 3. Injeções (Últimas 5)
        List<InjectionEntry> injections = InjectionStorage.loadInjections(this);
        containerInjections.removeAllViews();
        if (injections.isEmpty()) {
            addEmptyRow(containerInjections, "Nenhuma injeção registrada.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            int count = 0;
            // Itera de trás para frente para pegar as mais recentes
            for (int i = injections.size() - 1; i >= 0; i--) {
                if (count >= 5) break;
                InjectionEntry inj = injections.get(i);
                String line1 = sdf.format(new Date(inj.getTimestamp())) + " - " + getLocationLabel(inj.getLocationCode());
                String line2 = inj.getMedicationName();
                addTextRow(containerInjections, line1, line2);
                count++;
            }
        }

        // 4. Sintomas (Últimos 5)
        List<SymptomEntry> symptoms = SymptomStorage.loadSymptoms(this);
        containerSymptoms.removeAllViews();
        if (symptoms.isEmpty()) {
            addEmptyRow(containerSymptoms, "Sem registros recentes.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            int count = 0;
            for (int i = symptoms.size() - 1; i >= 0; i--) {
                if (count >= 5) break;
                SymptomEntry s = symptoms.get(i);
                String line1 = sdf.format(new Date(s.getTimestamp()));
                String line2 = String.format(Locale.getDefault(), "N: %d | F: %d | S: %d", s.getNausea(), s.getFatigue(), s.getSatiety());
                if (s.getNotes() != null && !s.getNotes().isEmpty()) {
                    line2 += "\n📝 " + s.getNotes();
                }
                addTextRow(containerSymptoms, line1, line2);
                count++;
            }
        }
    }

    private void addTextRow(LinearLayout parent, String title, String subtitle) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 0, 0, 16);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(getColor(R.color.ozem_text_primary));
        tvTitle.setTextSize(14);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextColor(getColor(R.color.ozem_text_secondary));
        tvSub.setTextSize(13);

        layout.addView(tvTitle);
        layout.addView(tvSub);
        parent.addView(layout);
    }

    private void addEmptyRow(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(getColor(R.color.ozem_text_secondary));
        tv.setTextSize(13);
        tv.setPadding(0,0,0,8);
        parent.addView(tv);
    }

    private String getLocationLabel(String code) {
        if ("abdomen".equals(code)) return "Abdômen";
        if ("thigh".equals(code)) return "Coxa";
        if ("arm".equals(code)) return "Braço";
        return code;
    }

    private String getNotesKeyForName(String name) {
        String keyName = (name == null) ? "" : name.trim().toLowerCase().replace(" ", "_");
        return "notes_" + keyName;
    }

    private void exportarResumo() {
        StringBuilder sb = new StringBuilder();
        String dateNow = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        sb.append("RELATÓRIO COMPLETO - OZEM+\n");
        sb.append("Gerado em: ").append(dateNow).append("\n");
        sb.append("================================\n\n");

        sb.append("[ MEDICAMENTOS ATUAIS ]\n");
        List<Medication> medications = MedicationStorage.loadMedications(this);
        if (medications.isEmpty()) {
            sb.append("Nenhum medicamento cadastrado.\n");
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            for (Medication med : medications) {
                sb.append("• ").append(med.getName()).append("\n");
                sb.append("  Dose: ").append(med.getDose()).append(" | Freq: ").append(med.getFrequency()).append("\n");
                String notesKey = getNotesKeyForName(med.getName());
                String notes = prefs.getString(notesKey, "");
                if (!notes.isEmpty()) {
                    sb.append("  Nota: ").append(notes).append("\n");
                }
                sb.append("\n");
            }
        }
        sb.append("--------------------------------\n\n");

        sb.append("[ EVOLUÇÃO DE PESO ]\n");
        List<WeightEntry> weights = WeightStorage.loadWeights(this);
        if (weights.isEmpty()) {
            sb.append("Nenhum registro de peso.\n");
        } else {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (WeightEntry w : weights) {
                sb.append(sdfDate.format(new Date(w.getTimestamp())))
                        .append(" - ")
                        .append(String.format(Locale.getDefault(), "%.1f kg", w.getWeight()))
                        .append("\n");
            }
        }
        sb.append("\n--------------------------------\n\n");

        sb.append("[ REGISTRO DE INJEÇÕES ]\n");
        List<InjectionEntry> injections = InjectionStorage.loadInjections(this);
        if (injections.isEmpty()) {
            sb.append("Nenhuma injeção registrada.\n");
        } else {
            SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            for (InjectionEntry inj : injections) {
                sb.append(sdfFull.format(new Date(inj.getTimestamp())))
                        .append(" - ")
                        .append(inj.getMedicationName())
                        .append(" (Local: ").append(getLocationLabel(inj.getLocationCode())).append(")")
                        .append("\n");
            }
        }
        sb.append("\n--------------------------------\n\n");

        sb.append("[ DIÁRIO DE SINTOMAS ]\n");
        List<SymptomEntry> symptoms = SymptomStorage.loadSymptoms(this);
        if (symptoms.isEmpty()) {
            sb.append("Nenhum registro de sintomas.\n");
        } else {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (SymptomEntry s : symptoms) {
                sb.append(sdfDate.format(new Date(s.getTimestamp()))).append(":\n");
                sb.append(String.format(Locale.getDefault(), "  Náusea: %d | Fadiga: %d | Saciedade: %d\n", s.getNausea(), s.getFatigue(), s.getSatiety()));
                if (s.getNotes() != null && !s.getNotes().trim().isEmpty()) {
                    sb.append("  Obs: ").append(s.getNotes().trim()).append("\n");
                }
                sb.append("\n");
            }
        }

        String relatorioFinal = sb.toString();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Meu Relatório Ozem+");
        sendIntent.putExtra(Intent.EXTRA_TEXT, relatorioFinal);

        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.export_share_title));
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, getString(R.string.export_share_no_app), Toast.LENGTH_SHORT).show();
        }
    }
}