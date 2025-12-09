package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigMedicationActivity extends AppCompatActivity {

    private TextView btnBack, txtScreenTitle;
    private EditText edtMedName, edtMedDose;
    private ChipGroup chipGroupFrequency, chipGroupWeekDay;
    private LinearLayout layoutWeeklyConfig, layoutMonthlyConfig;
    private NumberPicker npDayOfMonth;
    private TimePicker timePicker;
    private Button btnSaveMed;

    private int editIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_medication);

        initViews();
        setupListeners();
        checkEditMode();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtScreenTitle = findViewById(R.id.txtScreenTitle);

        edtMedName = findViewById(R.id.edtMedName);
        edtMedDose = findViewById(R.id.edtMedDose);

        chipGroupFrequency = findViewById(R.id.chipGroupFrequency);

        layoutWeeklyConfig = findViewById(R.id.layoutWeeklyConfig);
        chipGroupWeekDay = findViewById(R.id.chipGroupWeekDay);

        layoutMonthlyConfig = findViewById(R.id.layoutMonthlyConfig);
        npDayOfMonth = findViewById(R.id.npDayOfMonth);
        npDayOfMonth.setMinValue(1);
        npDayOfMonth.setMaxValue(31);

        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        btnSaveMed = findViewById(R.id.btnSaveMed);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        chipGroupFrequency.setOnCheckedStateChangeListener((group, checkedIds) -> {
            layoutWeeklyConfig.setVisibility(View.GONE);
            layoutMonthlyConfig.setVisibility(View.GONE);

            if (checkedIds.contains(R.id.chipWeekly)) {
                layoutWeeklyConfig.setVisibility(View.VISIBLE);
            } else if (checkedIds.contains(R.id.chipMonthly)) {
                layoutMonthlyConfig.setVisibility(View.VISIBLE);
            }
        });

        btnSaveMed.setOnClickListener(v -> saveMedication());
    }

    private void checkEditMode() {
        editIndex = getIntent().getIntExtra("edit_index", -1);
        if (editIndex != -1) {
            txtScreenTitle.setText(R.string.config_edit_title);
            btnSaveMed.setText(R.string.config_btn_update);

            List<Medication> meds = MedicationStorage.loadMedications(this);
            if (editIndex < meds.size()) {
                Medication med = meds.get(editIndex);
                edtMedName.setText(med.getName());
                edtMedDose.setText(med.getDose());

                // --- Lógica de Localização na Edição ---

                String freqDaily = getString(R.string.quiz_freq_daily);
                String freqWeekly = getString(R.string.quiz_freq_weekly);
                String freqMonthly = getString(R.string.quiz_freq_monthly);

                // Usa o Utils para "traduzir" o que está salvo para o idioma atual
                String localizedFreq = MedicationUtils.getLocalizedFrequency(this, med.getFrequency());

                if (localizedFreq.equals(freqDaily)) {
                    chipGroupFrequency.check(R.id.chipDaily);
                }
                else if (localizedFreq.equals(freqWeekly)) {
                    chipGroupFrequency.check(R.id.chipWeekly);
                    switch (med.getDayOfWeek()) {
                        case Calendar.MONDAY: chipGroupWeekDay.check(R.id.chipMon); break;
                        case Calendar.TUESDAY: chipGroupWeekDay.check(R.id.chipTue); break;
                        case Calendar.WEDNESDAY: chipGroupWeekDay.check(R.id.chipWed); break;
                        case Calendar.THURSDAY: chipGroupWeekDay.check(R.id.chipThu); break;
                        case Calendar.FRIDAY: chipGroupWeekDay.check(R.id.chipFri); break;
                        case Calendar.SATURDAY: chipGroupWeekDay.check(R.id.chipSat); break;
                        case Calendar.SUNDAY: chipGroupWeekDay.check(R.id.chipSun); break;
                    }
                }
                else if (localizedFreq.equals(freqMonthly)) {
                    chipGroupFrequency.check(R.id.chipMonthly);
                    // Extrai o dia usando Regex para não depender do idioma do texto
                    int day = extractDayFromText(med.getNextDate());
                    npDayOfMonth.setValue(day);
                }

                // Extrai a hora usando Regex (funciona em qualquer idioma)
                int[] time = extractTimeFromText(med.getNextDate());
                timePicker.setHour(time[0]);
                timePicker.setMinute(time[1]);
            }
        }
    }

    // Método auxiliar seguro para extrair Hora e Minuto
    private int[] extractTimeFromText(String text) {
        if (text == null) return new int[]{8, 0};
        Pattern p = Pattern.compile("(\\d{2}):(\\d{2})");
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                int h = Integer.parseInt(m.group(1));
                int min = Integer.parseInt(m.group(2));
                return new int[]{h, min};
            } catch (Exception e) {}
        }
        return new int[]{8, 0}; // Padrão
    }

    // Método auxiliar seguro para extrair Dia do Mês
    private int extractDayFromText(String text) {
        if (text == null) return 1;
        // Procura número solto entre 1 e 31
        Pattern p = Pattern.compile("\\b([1-9]|[12][0-9]|3[01])\\b");
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (Exception e) {}
        }
        return 1;
    }

    private void saveMedication() {
        String name = edtMedName.getText().toString().trim();
        String dose = edtMedDose.getText().toString().trim();

        if (name.isEmpty() || dose.isEmpty()) {
            Toast.makeText(this, getString(R.string.config_error_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int freqId = chipGroupFrequency.getCheckedChipId();
        if (freqId == -1) {
            Toast.makeText(this, getString(R.string.config_error_freq), Toast.LENGTH_SHORT).show();
            return;
        }

        String frequency;
        String nextDateTxt;
        int dayOfWeek = Calendar.MONDAY;

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        if (freqId == R.id.chipDaily) {
            frequency = getString(R.string.quiz_freq_daily);
            nextDateTxt = String.format(getString(R.string.schedule_format_daily), timeStr);
        } else if (freqId == R.id.chipMonthly) {
            frequency = getString(R.string.quiz_freq_monthly);
            int day = npDayOfMonth.getValue();
            nextDateTxt = String.format(Locale.getDefault(), getString(R.string.schedule_format_monthly), day, timeStr);
        } else {
            frequency = getString(R.string.quiz_freq_weekly);
            int dayId = chipGroupWeekDay.getCheckedChipId();
            if (dayId == -1) {
                Toast.makeText(this, getString(R.string.config_error_day), Toast.LENGTH_SHORT).show();
                return;
            }

            String diaStr = getString(R.string.day_generic);
            if (dayId == R.id.chipMon) { dayOfWeek = Calendar.MONDAY; diaStr = getString(R.string.day_segunda); }
            else if (dayId == R.id.chipTue) { dayOfWeek = Calendar.TUESDAY; diaStr = getString(R.string.day_terca); }
            else if (dayId == R.id.chipWed) { dayOfWeek = Calendar.WEDNESDAY; diaStr = getString(R.string.day_quarta); }
            else if (dayId == R.id.chipThu) { dayOfWeek = Calendar.THURSDAY; diaStr = getString(R.string.day_quinta); }
            else if (dayId == R.id.chipFri) { dayOfWeek = Calendar.FRIDAY; diaStr = getString(R.string.day_sexta); }
            else if (dayId == R.id.chipSat) { dayOfWeek = Calendar.SATURDAY; diaStr = getString(R.string.day_sabado); }
            else if (dayId == R.id.chipSun) { dayOfWeek = Calendar.SUNDAY; diaStr = getString(R.string.day_domingo); }

            nextDateTxt = String.format(getString(R.string.schedule_format_weekly), diaStr, timeStr);
        }

        Medication newMed = new Medication(name, dose, frequency, nextDateTxt, dayOfWeek);

        List<Medication> meds = MedicationStorage.loadMedications(this);

        if (editIndex != -1 && editIndex < meds.size()) {
            // EDITAR
            NotificationScheduler.cancelMedication(this, meds.get(editIndex));
            meds.set(editIndex, newMed);
            Toast.makeText(this, getString(R.string.config_msg_updated), Toast.LENGTH_SHORT).show();
        } else {
            // CRIAR NOVO
            meds.add(newMed);
            Toast.makeText(this, getString(R.string.config_msg_saved), Toast.LENGTH_SHORT).show();
        }

        MedicationStorage.saveMedications(this, meds);
        NotificationScheduler.scheduleMedication(this, newMed);

        finish();
    }
}