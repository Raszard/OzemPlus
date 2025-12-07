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
        txtReportDate.setText(getString(R.string.report_generated_fmt, dateNow));
    }

    private void loadData() {
        // 1. Dados do Perfil
        UserProfile profile = UserStorage.loadUserProfile(this);
        if (profile != null) {
            txtReportWeight.setText(String.format(Locale.getDefault(), getString(R.string.report_weight_fmt), profile.getCurrentWeight()));
            txtReportTarget.setText(String.format(Locale.getDefault(), getString(R.string.report_weight_fmt), profile.getTargetWeight()));
        }

        // 2. Medicamentos
        List<Medication> meds = MedicationStorage.loadMedications(this);
        containerMeds.removeAllViews();
        if (meds.isEmpty()) {
            addEmptyRow(containerMeds, getString(R.string.report_empty_meds));
        } else {
            for (Medication m : meds) {
                addTextRow(containerMeds, "💊 " + m.getName(), m.getDose() + " - " + m.getFrequency());
            }
        }

        // 3. Injeções (Últimas 5)
        List<InjectionEntry> injections = InjectionStorage.loadInjections(this);
        containerInjections.removeAllViews();
        if (injections.isEmpty()) {
            addEmptyRow(containerInjections, getString(R.string.report_empty_injections));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            int count = 0;
            for (int i = injections.size() - 1; i >= 0; i--) {
                if (count >= 5) break;
                InjectionEntry inj = injections.get(i);
                String line1 = sdf.format(new Date(inj.getTimestamp())) + " - " + getLocationLabel(inj.getLocationCode());
                String line2 = inj.getMedicationName();
                addTextRow(containerInjections, line1, line2);
                count++;
            }
        }

        // 4. Sintomas (Últimas 5)
        List<SymptomEntry> symptoms = SymptomStorage.loadSymptoms(this);
        containerSymptoms.removeAllViews();
        if (symptoms.isEmpty()) {
            addEmptyRow(containerSymptoms, getString(R.string.report_empty_symptoms));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            int count = 0;
            for (int i = symptoms.size() - 1; i >= 0; i--) {
                if (count >= 5) break;
                SymptomEntry s = symptoms.get(i);
                String line1 = sdf.format(new Date(s.getTimestamp()));
                String line2 = String.format(Locale.getDefault(), getString(R.string.report_symptom_line_fmt), s.getNausea(), s.getFatigue(), s.getSatiety());
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
        if ("abdomen".equals(code)) return getString(R.string.injection_location_abdomen);
        if ("thigh".equals(code)) return getString(R.string.injection_location_thigh);
        if ("arm".equals(code)) return getString(R.string.injection_location_arm);
        return code;
    }

    private String getNotesKeyForName(String name) {
        String keyName = (name == null) ? "" : name.trim().toLowerCase().replace(" ", "_");
        return "notes_" + keyName;
    }

    private void exportarResumo() {
        StringBuilder sb = new StringBuilder();
        String dateNow = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        sb.append(getString(R.string.report_export_header, dateNow));

        sb.append(getString(R.string.report_section_meds));
        List<Medication> medications = MedicationStorage.loadMedications(this);
        if (medications.isEmpty()) {
            sb.append(getString(R.string.report_empty_meds)).append("\n");
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            for (Medication med : medications) {
                sb.append("• ").append(med.getName()).append("\n");
                sb.append(String.format(getString(R.string.report_med_line_fmt), med.getDose(), med.getFrequency()));
                String notesKey = getNotesKeyForName(med.getName());
                String notes = prefs.getString(notesKey, "");
                if (!notes.isEmpty()) {
                    sb.append(String.format(getString(R.string.report_note_fmt), notes));
                }
                sb.append("\n");
            }
        }
        sb.append("--------------------------------\n\n");

        sb.append(getString(R.string.report_section_weight));
        List<WeightEntry> weights = WeightStorage.loadWeights(this);
        if (weights.isEmpty()) {
            sb.append(getString(R.string.weight_no_data)).append("\n");
        } else {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (WeightEntry w : weights) {
                sb.append(String.format(Locale.getDefault(), getString(R.string.weight_history_export_line_fmt), sdfDate.format(new Date(w.getTimestamp())), w.getWeight()));
            }
        }
        sb.append("\n--------------------------------\n\n");

        sb.append(getString(R.string.report_section_injections));
        List<InjectionEntry> injections = InjectionStorage.loadInjections(this);
        if (injections.isEmpty()) {
            sb.append(getString(R.string.report_empty_injections)).append("\n");
        } else {
            SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            for (InjectionEntry inj : injections) {
                sb.append(String.format(getString(R.string.injection_history_export_line_fmt),
                        sdfFull.format(new Date(inj.getTimestamp())),
                        inj.getMedicationName(),
                        getLocationLabel(inj.getLocationCode())
                )).append("\n");
            }
        }
        sb.append("\n--------------------------------\n\n");

        sb.append(getString(R.string.report_section_symptoms));
        List<SymptomEntry> symptoms = SymptomStorage.loadSymptoms(this);
        if (symptoms.isEmpty()) {
            sb.append(getString(R.string.report_empty_symptoms)).append("\n");
        } else {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (SymptomEntry s : symptoms) {
                sb.append(sdfDate.format(new Date(s.getTimestamp()))).append(":\n");
                sb.append(String.format(Locale.getDefault(), getString(R.string.report_symptom_line_fmt), s.getNausea(), s.getFatigue(), s.getSatiety()));
                if (s.getNotes() != null && !s.getNotes().trim().isEmpty()) {
                    sb.append(String.format(getString(R.string.report_obs_fmt), s.getNotes().trim()));
                }
                sb.append("\n");
            }
        }

        String relatorioFinal = sb.toString();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_export_subject));
        sendIntent.putExtra(Intent.EXTRA_TEXT, relatorioFinal);

        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.export_share_title));
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, getString(R.string.export_share_no_app), Toast.LENGTH_SHORT).show();
        }
    }
}