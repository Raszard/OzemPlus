package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "glp1_prefs";
    private static final String KEY_REMINDER_MED_NAME = "reminder_med_name";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";

    private Spinner spReminderMed;
    private TimePicker timePickerReminder;
    private Switch switchEnableReminder;
    private Button btnSaveReminder;

    private List<Medication> medications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        spReminderMed = findViewById(R.id.spReminderMed);
        timePickerReminder = findViewById(R.id.timePickerReminder);
        switchEnableReminder = findViewById(R.id.switchEnableReminder);
        btnSaveReminder = findViewById(R.id.btnSaveReminder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        carregarMedicamentos();
        carregarConfiguracaoExistente();

        btnSaveReminder.setOnClickListener(v -> salvarConfiguracao());
    }

    private void carregarMedicamentos() {
        medications.clear();
        medications.addAll(MedicationStorage.loadMedications(this));

        if (medications.isEmpty()) {
            Toast.makeText(this, getString(R.string.injection_no_meds), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        List<String> names = new ArrayList<>();
        for (Medication m : medications) {
            String n = m.getName();
            if (n == null || n.trim().isEmpty()) n = "Medicamento";
            names.add(n);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                names
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReminderMed.setAdapter(adapter);
    }

    private void carregarConfiguracaoExistente() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String medName = prefs.getString(KEY_REMINDER_MED_NAME, null);
        int hour = prefs.getInt(KEY_REMINDER_HOUR, 8);
        int minute = prefs.getInt(KEY_REMINDER_MINUTE, 0);
        boolean enabled = prefs.getBoolean(KEY_REMINDER_ENABLED, false);

        // Seleciona medicamento salvo, se existir
        if (medName != null) {
            int index = -1;
            for (int i = 0; i < medications.size(); i++) {
                String n = medications.get(i).getName();
                if (n != null && n.equals(medName)) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                spReminderMed.setSelection(index);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerReminder.setHour(hour);
            timePickerReminder.setMinute(minute);
        } else {
            timePickerReminder.setCurrentHour(hour);
            timePickerReminder.setCurrentMinute(minute);
        }

        switchEnableReminder.setChecked(enabled);
    }

    private void salvarConfiguracao() {
        int pos = spReminderMed.getSelectedItemPosition();
        if (pos < 0 || pos >= medications.size()) {
            Toast.makeText(this, getString(R.string.toast_injection_med_required), Toast.LENGTH_SHORT).show();
            return;
        }

        Medication med = medications.get(pos);
        String medName = med.getName();
        if (medName == null || medName.trim().isEmpty()) {
            medName = "Medicamento";
        }

        int hour, minute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePickerReminder.getHour();
            minute = timePickerReminder.getMinute();
        } else {
            hour = timePickerReminder.getCurrentHour();
            minute = timePickerReminder.getCurrentMinute();
        }

        boolean enabled = switchEnableReminder.isChecked();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_REMINDER_MED_NAME, medName)
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .putBoolean(KEY_REMINDER_ENABLED, enabled)
                .apply();

        if (enabled) {
            agendarAlarmeSemanal(medName, hour, minute);
            Toast.makeText(this, getString(R.string.toast_reminder_saved), Toast.LENGTH_SHORT).show();
        } else {
            cancelarAlarme();
            Toast.makeText(this, getString(R.string.toast_reminder_disabled), Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void agendarAlarmeSemanal(String medName, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("medName", medName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerAt = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();
        if (triggerAt <= now) {
            // se o horário de hoje já passou, agenda para a próxima semana
            triggerAt += 7L * 24 * 60 * 60 * 1000L;
        }

        long interval = 7L * 24 * 60 * 60 * 1000L; // 7 dias

        // inexact repeating = não precisa de SCHEDULE_EXACT_ALARM
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                interval,
                pendingIntent
        );
    }

    private void cancelarAlarme() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
