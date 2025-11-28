package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ConfigMedicationActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "glp1_prefs";
    private static final String KEY_REMINDER_MED_NAME = "reminder_med_name";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    private static final String KEY_REMINDER_DAY = "reminder_day_of_week";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";

    private EditText edtMedName, edtMedDose, edtMedFrequency;
    private Button btnSaveMedication;
    private Switch switchReminder;
    private ChipGroup chipGroupDays;
    private LinearLayout containerTimePicker;
    private TextView txtSelectedTime;

    private List<Medication> medications;
    private boolean isEdit = false;
    private String originalName = null;
    private int selectedHour = 8;
    private int selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_medication);

        initViews();
        setupListeners();

        medications = MedicationStorage.loadMedications(this);
        originalName = getIntent().getStringExtra("medName");

        if (originalName != null && !originalName.trim().isEmpty()) {
            isEdit = true;
            preencherCamposParaEdicao(originalName);
        } else {
            // Novo: Padrão Segunda às 08:00
            atualizarTextoHora();
            selecionarChipPeloDia(Calendar.MONDAY);
        }

        carregarEstadoSwitchLembrete();
    }

    private void initViews() {
        edtMedName = findViewById(R.id.edtMedName);
        edtMedDose = findViewById(R.id.edtMedDose);
        edtMedFrequency = findViewById(R.id.edtMedFrequency);
        // Removemos o edtMedNextDate pois agora é gerado automaticamente

        btnSaveMedication = findViewById(R.id.btnSaveMedication);
        switchReminder = findViewById(R.id.switchReminder);
        chipGroupDays = findViewById(R.id.chipGroupDays);
        containerTimePicker = findViewById(R.id.containerTimePicker);
        txtSelectedTime = findViewById(R.id.txtSelectedTime);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSaveMedication.setOnClickListener(v -> salvarMedicamento());
        containerTimePicker.setOnClickListener(v -> abrirSeletorHora());
    }

    private void preencherCamposParaEdicao(String medName) {
        for (Medication m : medications) {
            if (m.getName() != null && m.getName().equals(medName)) {
                edtMedName.setText(m.getName());
                edtMedDose.setText(m.getDose());
                edtMedFrequency.setText(m.getFrequency());

                // Preenche o dia e hora com base no objeto salvo
                if (m.getDayOfWeek() > 0) {
                    selecionarChipPeloDia(m.getDayOfWeek());
                }

                // Tenta extrair a hora da string antiga ou usa padrão
                // Idealmente, você salvaria a hora no objeto Medication também,
                // mas vamos manter simples por enquanto.
                break;
            }
        }
    }

    private void carregarEstadoSwitchLembrete() {
        // Verifica apenas se o switch deve estar ligado
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_REMINDER_ENABLED, false);
        String savedMedName = prefs.getString(KEY_REMINDER_MED_NAME, "");

        // Se estamos editando o remédio que tem o lembrete ativo
        if (isEdit && originalName != null && originalName.equals(savedMedName)) {
            switchReminder.setChecked(enabled);
            // Recupera hora do lembrete para exibir
            selectedHour = prefs.getInt(KEY_REMINDER_HOUR, 8);
            selectedMinute = prefs.getInt(KEY_REMINDER_MINUTE, 0);
            atualizarTextoHora();
        } else {
            switchReminder.setChecked(false);
        }
    }

    private void abrirSeletorHora() {
        new TimePickerDialog(this, (view, h, m) -> {
            selectedHour = h;
            selectedMinute = m;
            atualizarTextoHora();
        }, selectedHour, selectedMinute, true).show();
    }

    private void atualizarTextoHora() {
        txtSelectedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
    }

    private void selecionarChipPeloDia(int calendarDay) {
        int chipId = R.id.chipMon;
        switch (calendarDay) {
            case Calendar.MONDAY: chipId = R.id.chipMon; break;
            case Calendar.TUESDAY: chipId = R.id.chipTue; break;
            case Calendar.WEDNESDAY: chipId = R.id.chipWed; break;
            case Calendar.THURSDAY: chipId = R.id.chipThu; break;
            case Calendar.FRIDAY: chipId = R.id.chipFri; break;
            case Calendar.SATURDAY: chipId = R.id.chipSat; break;
            case Calendar.SUNDAY: chipId = R.id.chipSun; break;
        }
        chipGroupDays.check(chipId);
    }

    private int getDiaSelecionado() {
        int id = chipGroupDays.getCheckedChipId();
        if (id == R.id.chipMon) return Calendar.MONDAY;
        if (id == R.id.chipTue) return Calendar.TUESDAY;
        if (id == R.id.chipWed) return Calendar.WEDNESDAY;
        if (id == R.id.chipThu) return Calendar.THURSDAY;
        if (id == R.id.chipFri) return Calendar.FRIDAY;
        if (id == R.id.chipSat) return Calendar.SATURDAY;
        if (id == R.id.chipSun) return Calendar.SUNDAY;
        return Calendar.MONDAY;
    }

    private String getNomeDia(int day) {
        switch (day) {
            case Calendar.MONDAY: return "Segunda";
            case Calendar.TUESDAY: return "Terça";
            case Calendar.WEDNESDAY: return "Quarta";
            case Calendar.THURSDAY: return "Quinta";
            case Calendar.FRIDAY: return "Sexta";
            case Calendar.SATURDAY: return "Sábado";
            case Calendar.SUNDAY: return "Domingo";
            default: return "Dia";
        }
    }

    private void salvarMedicamento() {
        String name = edtMedName.getText().toString().trim();
        String dose = edtMedDose.getText().toString().trim();
        String freq = edtMedFrequency.getText().toString().trim();

        // 1. GERAR TEXTO AUTOMÁTICO DE PRÓXIMA DATA
        int dayOfWeek = getDiaSelecionado();
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        String dayName = getNomeDia(dayOfWeek);

        // Ex: "Toda Terça às 08:00"
        String nextDateString = "Toda " + dayName + " às " + timeStr;

        if (name.isEmpty()) {
            Toast.makeText(this, "Informe o nome.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. ATUALIZAR LISTA
        if (isEdit && originalName != null) {
            boolean found = false;
            for (Medication m : medications) {
                if (m.getName().equals(originalName)) {
                    m.setName(name);
                    m.setDose(dose);
                    m.setFrequency(freq);
                    m.setNextDate(nextDateString); // Salva o texto gerado
                    m.setDayOfWeek(dayOfWeek);
                    found = true; break;
                }
            }
            if (!found) medications.add(new Medication(name, dose, freq, nextDateString, dayOfWeek));
        } else {
            medications.add(new Medication(name, dose, freq, nextDateString, dayOfWeek));
        }

        MedicationStorage.saveMedications(this, medications);

        // 3. CONFIGURAR LEMBRETE
        configurarLembrete(name, dayOfWeek);

        Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void configurarLembrete(String medName, int dayOfWeek) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean enabled = switchReminder.isChecked();

        if (enabled) {
            prefs.edit()
                    .putString(KEY_REMINDER_MED_NAME, medName)
                    .putInt(KEY_REMINDER_HOUR, selectedHour)
                    .putInt(KEY_REMINDER_MINUTE, selectedMinute)
                    .putInt(KEY_REMINDER_DAY, dayOfWeek)
                    .putBoolean(KEY_REMINDER_ENABLED, true)
                    .apply();
            agendarAlarme(medName, dayOfWeek, selectedHour, selectedMinute);
        } else {
            // Se desativou e era o lembrete deste remédio, cancela
            String savedMedName = prefs.getString(KEY_REMINDER_MED_NAME, "");
            if (savedMedName.equals(medName)) {
                prefs.edit().putBoolean(KEY_REMINDER_ENABLED, false).apply();
                cancelarAlarme();
            }
        }
    }

    private void agendarAlarme(String medName, int dayOfWeek, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("medName", medName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    private void cancelarAlarme() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}