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

                String freqDaily = getString(R.string.quiz_freq_daily);
                String freqWeekly = getString(R.string.quiz_freq_weekly);
                String freqMonthly = getString(R.string.quiz_freq_monthly);

                if (med.getFrequency().equals(freqDaily)) {
                    chipGroupFrequency.check(R.id.chipDaily);
                } else if (med.getFrequency().equals(freqWeekly)) {
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
                } else if (med.getFrequency().equals(freqMonthly)) {
                    chipGroupFrequency.check(R.id.chipMonthly);
                    try {
                        String txt = med.getNextDate();
                        String separatorEveryDay = getString(R.string.separator_every_day);
                        String separatorAt = getString(R.string.separator_at);
                        if (txt.contains(separatorEveryDay)) {
                            String dayStr = txt.substring(separatorEveryDay.length(), txt.indexOf(separatorAt));
                            npDayOfMonth.setValue(Integer.parseInt(dayStr.trim()));
                        }
                    } catch (Exception e) {}
                }

                try {
                    String txt = med.getNextDate();
                    String separatorAt = getString(R.string.separator_at);
                    if (txt.contains(separatorAt)) {
                        String timePart = txt.substring(txt.indexOf(separatorAt) + separatorAt.length()).trim();
                        String[] parts = timePart.split(":");
                        timePicker.setHour(Integer.parseInt(parts[0]));
                        timePicker.setMinute(Integer.parseInt(parts[1]));
                    }
                } catch (Exception e) {}
            }
        }
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