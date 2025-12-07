package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DailyGoalsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "glp1_prefs";

    private static final String KEY_DAILY_WATER_GOAL = "daily_goal_water_ml";
    private static final String KEY_DAILY_EXERCISE_GOAL = "daily_goal_exercise_min";

    private static final String KEY_DAILY_WATER_PREFIX = "daily_progress_water_";
    private static final String KEY_DAILY_EXERCISE_PREFIX = "daily_progress_exercise_";

    private static final String KEY_DAILY_REMINDER_HOUR = "daily_reminder_hour";
    private static final String KEY_DAILY_REMINDER_MINUTE = "daily_reminder_minute";
    private static final String KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled";

    private EditText edtWaterGoal;
    private EditText edtExerciseGoal;
    private TextView txtWaterProgress;
    private TextView txtExerciseProgress;
    private TextView txtWaterPercent;
    private TextView txtExercisePercent;

    private Button btnSaveTargets;
    private Button btnAddWater200;
    private Button btnAddWater50;
    private Button btnAdd5Min;
    private Button btnAdd10Min;
    private Button btnCalcWater;

    private Switch switchDailyReminder;
    private LinearLayout containerTimePicker;
    private TextView txtSelectedTime;

    private CircularProgressIndicator progressWater;
    private CircularProgressIndicator progressExercise;

    private int waterGoal = 2000;
    private int exerciseGoal = 20;
    private int currentWater = 0;
    private int currentExercise = 0;
    private int reminderHour = 9;
    private int reminderMinute = 0;

    private String todayKey;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_goals);

        initViews();

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        todayKey = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        carregarMetas();
        carregarProgresso();
        carregarLembrete();

        setupListeners();
    }

    private void initViews() {
        edtWaterGoal = findViewById(R.id.edtWaterGoal);
        edtExerciseGoal = findViewById(R.id.edtExerciseGoal);
        txtWaterProgress = findViewById(R.id.txtWaterProgress);
        txtExerciseProgress = findViewById(R.id.txtExerciseProgress);
        txtWaterPercent = findViewById(R.id.txtWaterPercent);
        txtExercisePercent = findViewById(R.id.txtExercisePercent);

        btnSaveTargets = findViewById(R.id.btnSaveTargets);
        btnAddWater200 = findViewById(R.id.btnAddWater200);
        btnAddWater50 = findViewById(R.id.btnAddWater50);
        btnAdd5Min = findViewById(R.id.btnAdd5Min);
        btnAdd10Min = findViewById(R.id.btnAdd10Min);
        btnCalcWater = findViewById(R.id.btnCalcWater);

        switchDailyReminder = findViewById(R.id.switchDailyReminder);
        containerTimePicker = findViewById(R.id.containerTimePicker);
        txtSelectedTime = findViewById(R.id.txtSelectedTime);

        progressWater = findViewById(R.id.progressWater);
        progressExercise = findViewById(R.id.progressExercise);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSaveTargets.setOnClickListener(v -> salvarTudo());

        btnAddWater200.setOnClickListener(v -> incrementarAgua(200));
        btnAddWater50.setOnClickListener(v -> incrementarAgua(50));
        btnAdd5Min.setOnClickListener(v -> incrementarExercicio(5));
        btnAdd10Min.setOnClickListener(v -> incrementarExercicio(10));

        btnCalcWater.setOnClickListener(v -> calcularAguaIdeal());

        containerTimePicker.setOnClickListener(v -> abrirSeletorHora());
        switchDailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            containerTimePicker.setAlpha(isChecked ? 1.0f : 0.5f);
            containerTimePicker.setEnabled(isChecked);
        });
    }

    private void calcularAguaIdeal() {
        UserProfile profile = UserStorage.loadUserProfile(this);
        if (profile != null && profile.getCurrentWeight() > 0) {
            int ideal = (int) (profile.getCurrentWeight() * 35);
            edtWaterGoal.setText(String.valueOf(ideal));
            Toast.makeText(this, getString(R.string.daily_calc_success, ideal), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.daily_error_weight), Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirSeletorHora() {
        TimePickerDialog picker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    reminderHour = hourOfDay;
                    reminderMinute = minute;
                    atualizarTextoHora();
                },
                reminderHour,
                reminderMinute,
                true
        );
        picker.show();
    }

    private void atualizarTextoHora() {
        txtSelectedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute));
    }

    private void carregarMetas() {
        waterGoal = prefs.getInt(KEY_DAILY_WATER_GOAL, 2000);
        exerciseGoal = prefs.getInt(KEY_DAILY_EXERCISE_GOAL, 20);

        edtWaterGoal.setText(String.valueOf(waterGoal));
        edtExerciseGoal.setText(String.valueOf(exerciseGoal));
    }

    private void carregarProgresso() {
        String waterKey = KEY_DAILY_WATER_PREFIX + todayKey;
        String exerciseKey = KEY_DAILY_EXERCISE_PREFIX + todayKey;

        currentWater = prefs.getInt(waterKey, 0);
        currentExercise = prefs.getInt(exerciseKey, 0);

        atualizarVisuais();
    }

    private void carregarLembrete() {
        reminderHour = prefs.getInt(KEY_DAILY_REMINDER_HOUR, 9);
        reminderMinute = prefs.getInt(KEY_DAILY_REMINDER_MINUTE, 0);
        boolean enabled = prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, false);

        switchDailyReminder.setChecked(enabled);
        containerTimePicker.setEnabled(enabled);
        containerTimePicker.setAlpha(enabled ? 1.0f : 0.5f);
        atualizarTextoHora();
    }

    private void salvarTudo() {
        waterGoal = parseIntOrDefault(edtWaterGoal, 2000);
        exerciseGoal = parseIntOrDefault(edtExerciseGoal, 20);

        prefs.edit()
                .putInt(KEY_DAILY_WATER_GOAL, waterGoal)
                .putInt(KEY_DAILY_EXERCISE_GOAL, exerciseGoal)
                .apply();

        boolean enabled = switchDailyReminder.isChecked();
        prefs.edit()
                .putInt(KEY_DAILY_REMINDER_HOUR, reminderHour)
                .putInt(KEY_DAILY_REMINDER_MINUTE, reminderMinute)
                .putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled)
                .apply();

        if (enabled) {
            agendarLembreteDiario(reminderHour, reminderMinute);
        } else {
            cancelarLembreteDiario();
        }

        atualizarVisuais();
        Toast.makeText(this, getString(R.string.daily_config_saved), Toast.LENGTH_SHORT).show();
    }

    private void salvarProgresso() {
        String waterKey = KEY_DAILY_WATER_PREFIX + todayKey;
        String exerciseKey = KEY_DAILY_EXERCISE_PREFIX + todayKey;

        prefs.edit()
                .putInt(waterKey, currentWater)
                .putInt(exerciseKey, currentExercise)
                .apply();
    }

    private void incrementarAgua(int amount) {
        currentWater += amount;
        atualizarVisuais();
        salvarProgresso();
    }

    private void incrementarExercicio(int minutes) {
        currentExercise += minutes;
        atualizarVisuais();
        salvarProgresso();
    }

    private void atualizarVisuais() {
        txtWaterProgress.setText(getString(R.string.daily_ingested_fmt, currentWater, waterGoal));
        txtExerciseProgress.setText(getString(R.string.daily_exercise_done_fmt, currentExercise, exerciseGoal));

        if (waterGoal > 0) {
            int p = (int) ((currentWater / (float) waterGoal) * 100);
            progressWater.setProgress(Math.min(p, 100));
            txtWaterPercent.setText(p + "%");
        }

        if (exerciseGoal > 0) {
            int p = (int) ((currentExercise / (float) exerciseGoal) * 100);
            progressExercise.setProgress(Math.min(p, 100));
            txtExercisePercent.setText(p + "%");
        }
    }

    private void agendarLembreteDiario(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, DailyGoalsReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private void cancelarLembreteDiario() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(this, DailyGoalsReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    private int parseIntOrDefault(EditText editText, int defaultValue) {
        String s = editText.getText().toString().trim();
        if (s.isEmpty()) return defaultValue;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return defaultValue; }
    }
}