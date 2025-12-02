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
            txtScreenTitle.setText("Editar Medicamento");
            btnSaveMed.setText("Atualizar Medicamento");

            List<Medication> meds = MedicationStorage.loadMedications(this);
            if (editIndex < meds.size()) {
                Medication med = meds.get(editIndex);
                edtMedName.setText(med.getName());
                edtMedDose.setText(med.getDose());

                if (med.getFrequency().equals("Diariamente")) {
                    chipGroupFrequency.check(R.id.chipDaily);
                } else if (med.getFrequency().equals("Semanalmente")) {
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
                } else if (med.getFrequency().equals("Mensalmente")) {
                    chipGroupFrequency.check(R.id.chipMonthly);
                    try {
                        String txt = med.getNextDate();
                        if (txt.contains("Todo dia ")) {
                            String dayStr = txt.substring(9, txt.indexOf(" às"));
                            npDayOfMonth.setValue(Integer.parseInt(dayStr.trim()));
                        }
                    } catch (Exception e) {}
                }

                try {
                    String txt = med.getNextDate();
                    if (txt.contains(" às ")) {
                        String timePart = txt.substring(txt.indexOf(" às ") + 4).trim();
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
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int freqId = chipGroupFrequency.getCheckedChipId();
        if (freqId == -1) {
            Toast.makeText(this, "Selecione a frequência", Toast.LENGTH_SHORT).show();
            return;
        }

        String frequency;
        String nextDateTxt;
        int dayOfWeek = Calendar.MONDAY;

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        if (freqId == R.id.chipDaily) {
            frequency = "Diariamente";
            nextDateTxt = "Todos os dias às " + timeStr;
        } else if (freqId == R.id.chipMonthly) {
            frequency = "Mensalmente";
            int day = npDayOfMonth.getValue();
            nextDateTxt = "Todo dia " + day + " às " + timeStr;
        } else {
            frequency = "Semanalmente";
            int dayId = chipGroupWeekDay.getCheckedChipId();
            if (dayId == -1) {
                Toast.makeText(this, "Selecione o dia da semana", Toast.LENGTH_SHORT).show();
                return;
            }

            String diaStr = "Dia";
            if (dayId == R.id.chipMon) { dayOfWeek = Calendar.MONDAY; diaStr = "Segunda"; }
            else if (dayId == R.id.chipTue) { dayOfWeek = Calendar.TUESDAY; diaStr = "Terça"; }
            else if (dayId == R.id.chipWed) { dayOfWeek = Calendar.WEDNESDAY; diaStr = "Quarta"; }
            else if (dayId == R.id.chipThu) { dayOfWeek = Calendar.THURSDAY; diaStr = "Quinta"; }
            else if (dayId == R.id.chipFri) { dayOfWeek = Calendar.FRIDAY; diaStr = "Sexta"; }
            else if (dayId == R.id.chipSat) { dayOfWeek = Calendar.SATURDAY; diaStr = "Sábado"; }
            else if (dayId == R.id.chipSun) { dayOfWeek = Calendar.SUNDAY; diaStr = "Domingo"; }

            nextDateTxt = "Toda " + diaStr + " às " + timeStr;
        }

        Medication newMed = new Medication(name, dose, frequency, nextDateTxt, dayOfWeek);

        List<Medication> meds = MedicationStorage.loadMedications(this);

        if (editIndex != -1 && editIndex < meds.size()) {
            // EDITAR: Cancelar anterior e atualizar
            NotificationScheduler.cancelMedication(this, meds.get(editIndex));

            meds.set(editIndex, newMed);
            Toast.makeText(this, "Medicamento atualizado!", Toast.LENGTH_SHORT).show();
        } else {
            // CRIAR NOVO
            meds.add(newMed);
            Toast.makeText(this, "Medicamento salvo!", Toast.LENGTH_SHORT).show();
        }

        MedicationStorage.saveMedications(this, meds);

        // AGENDAR O NOVO ALARME
        NotificationScheduler.scheduleMedication(this, newMed);

        finish();
    }
}