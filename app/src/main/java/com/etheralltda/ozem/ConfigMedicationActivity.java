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
    private ChipGroup chipGroupFrequency;

    private LinearLayout layoutWeekPickerConfig, layoutMonthlyConfig;
    private NumberPicker npWeekDay, npDayOfMonth;

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

        layoutWeekPickerConfig = findViewById(R.id.layoutWeekPickerConfig);
        npWeekDay = findViewById(R.id.npWeekDay);

        layoutMonthlyConfig = findViewById(R.id.layoutMonthlyConfig);
        npDayOfMonth = findViewById(R.id.npDayOfMonth);
        npDayOfMonth.setMinValue(1);
        npDayOfMonth.setMaxValue(31);

        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        btnSaveMed = findViewById(R.id.btnSaveMed);

        // Setup WeekDay Picker
        String[] daysOfWeek = new String[]{
                getString(R.string.day_domingo),
                getString(R.string.day_segunda),
                getString(R.string.day_terca),
                getString(R.string.day_quarta),
                getString(R.string.day_quinta),
                getString(R.string.day_sexta),
                getString(R.string.day_sabado)
        };
        npWeekDay.setMinValue(1);
        npWeekDay.setMaxValue(7);
        npWeekDay.setDisplayedValues(daysOfWeek);
        npWeekDay.setValue(Calendar.MONDAY);
        npWeekDay.setWrapSelectorWheel(true);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        chipGroupFrequency.setOnCheckedStateChangeListener((group, checkedIds) -> {
            layoutWeekPickerConfig.setVisibility(View.GONE);
            layoutMonthlyConfig.setVisibility(View.GONE);

            if (checkedIds.contains(R.id.chipWeekly)) {
                layoutWeekPickerConfig.setVisibility(View.VISIBLE);
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

                String localizedFreq = MedicationUtils.getLocalizedFrequency(this, med.getFrequency());

                if (localizedFreq.equals(freqDaily)) {
                    chipGroupFrequency.check(R.id.chipDaily);
                }
                else if (localizedFreq.equals(freqWeekly)) {
                    chipGroupFrequency.check(R.id.chipWeekly);
                    // Seta o valor direto no picker (1..7)
                    npWeekDay.setValue(med.getDayOfWeek());
                }
                else if (localizedFreq.equals(freqMonthly)) {
                    chipGroupFrequency.check(R.id.chipMonthly);
                    int day = extractDayFromText(med.getNextDate());
                    npDayOfMonth.setValue(day);
                }

                int[] time = extractTimeFromText(med.getNextDate());
                timePicker.setHour(time[0]);
                timePicker.setMinute(time[1]);
            }
        }
    }

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
        return new int[]{8, 0};
    }

    private int extractDayFromText(String text) {
        if (text == null) return 1;
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
            // Pega o valor do Picker
            dayOfWeek = npWeekDay.getValue();

            String diaStr = getString(R.string.day_generic);
            switch (dayOfWeek) {
                case Calendar.MONDAY: diaStr = getString(R.string.day_segunda); break;
                case Calendar.TUESDAY: diaStr = getString(R.string.day_terca); break;
                case Calendar.WEDNESDAY: diaStr = getString(R.string.day_quarta); break;
                case Calendar.THURSDAY: diaStr = getString(R.string.day_quinta); break;
                case Calendar.FRIDAY: diaStr = getString(R.string.day_sexta); break;
                case Calendar.SATURDAY: diaStr = getString(R.string.day_sabado); break;
                case Calendar.SUNDAY: diaStr = getString(R.string.day_domingo); break;
            }

            nextDateTxt = String.format(getString(R.string.schedule_format_weekly), diaStr, timeStr);
        }

        Medication newMed = new Medication(name, dose, frequency, nextDateTxt, dayOfWeek);

        List<Medication> meds = MedicationStorage.loadMedications(this);

        if (editIndex != -1 && editIndex < meds.size()) {
            NotificationScheduler.cancelMedication(this, meds.get(editIndex));
            meds.set(editIndex, newMed);
            Toast.makeText(this, getString(R.string.config_msg_updated), Toast.LENGTH_SHORT).show();
        } else {
            meds.add(newMed);
            Toast.makeText(this, getString(R.string.config_msg_saved), Toast.LENGTH_SHORT).show();
        }

        MedicationStorage.saveMedications(this, meds);
        NotificationScheduler.scheduleMedication(this, newMed);

        finish();
    }
}