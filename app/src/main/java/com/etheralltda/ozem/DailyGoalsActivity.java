package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;

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
    private Button btnSaveTargets;
    private Button btnAddWater200;
    private Button btnAddWater50;
    private Button btnAdd5Min;
    private Button btnAdd10Min;
    private Switch switchDailyReminder;
    private TimePicker timePickerDailyReminder;
    private Button btnSaveDailyReminder;

    private LinearProgressIndicator progressWater;
    private LinearProgressIndicator progressExercise;

    private int waterGoal = 2000;
    private int exerciseGoal = 20;
    private int currentWater = 0;
    private int currentExercise = 0;
    private String todayKey;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_goals);

        edtWaterGoal = findViewById(R.id.edtWaterGoal);
        edtExerciseGoal = findViewById(R.id.edtExerciseGoal);
        txtWaterProgress = findViewById(R.id.txtWaterProgress);
        txtExerciseProgress = findViewById(R.id.txtExerciseProgress);
        btnSaveTargets = findViewById(R.id.btnSaveTargets);
        btnAddWater200 = findViewById(R.id.btnAddWater200);
        btnAddWater50 = findViewById(R.id.btnAddWater50);
        btnAdd5Min = findViewById(R.id.btnAdd5Min);
        btnAdd10Min = findViewById(R.id.btnAdd10Min);
        switchDailyReminder = findViewById(R.id.switchDailyReminder);
        timePickerDailyReminder = findViewById(R.id.timePickerDailyReminder);
        btnSaveDailyReminder = findViewById(R.id.btnSaveDailyReminder);
        progressWater = findViewById(R.id.progressWater);
        progressExercise = findViewById(R.id.progressExercise);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        todayKey = new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                .format(new Date());

        carregarMetas();
        carregarProgresso();
        carregarLembrete();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSaveTargets.setOnClickListener(v -> salvarMetas());

        btnAddWater200.setOnClickListener(v -> incrementarAgua(200));
        btnAddWater50.setOnClickListener(v -> incrementarAgua(50));
        btnAdd5Min.setOnClickListener(v -> incrementarExercicio(5));
        btnAdd10Min.setOnClickListener(v -> incrementarExercicio(10));

        btnSaveDailyReminder.setOnClickListener(v -> salvarLembrete());
    }

    private void carregarMetas() {
        waterGoal = prefs.getInt(KEY_DAILY_WATER_GOAL, 2000);
        exerciseGoal = prefs.getInt(KEY_DAILY_EXERCISE_GOAL, 20);

        edtWaterGoal.setText(String.valueOf(waterGoal));
        edtExerciseGoal.setText(String.valueOf(exerciseGoal));

        atualizarTextoProgresso();
    }

    private void salvarMetas() {
        waterGoal = parseIntOrDefault(edtWaterGoal, 2000);
        exerciseGoal = parseIntOrDefault(edtExerciseGoal, 20);

        prefs.edit()
                .putInt(KEY_DAILY_WATER_GOAL, waterGoal)
                .putInt(KEY_DAILY_EXERCISE_GOAL, exerciseGoal)
                .apply();

        atualizarTextoProgresso();

        Toast.makeText(this,
                getString(R.string.toast_daily_goals_saved),
                Toast.LENGTH_SHORT).show();
    }

    private void carregarProgresso() {
        String waterKey = KEY_DAILY_WATER_PREFIX + todayKey;
        String exerciseKey = KEY_DAILY_EXERCISE_PREFIX + todayKey;

        currentWater = prefs.getInt(waterKey, 0);
        currentExercise = prefs.getInt(exerciseKey, 0);

        atualizarTextoProgresso();
    }

    private void salvarProgresso() {
        String waterKey = KEY_DAILY_WATER_PREFIX + todayKey;
        String exerciseKey = KEY_DAILY_EXERCISE_PREFIX + todayKey;

        prefs.edit()
                .putInt(waterKey, currentWater)
                .putInt(exerciseKey, currentExercise)
                .apply();

        Toast.makeText(this,
                getString(R.string.toast_daily_progress_saved),
                Toast.LENGTH_SHORT).show();
    }

    private void incrementarAgua(int amount) {
        currentWater += amount;
        atualizarTextoProgresso();
        salvarProgresso();
    }

    private void incrementarExercicio(int minutes) {
        currentExercise += minutes;
        atualizarTextoProgresso();
        salvarProgresso();
    }

    private void atualizarTextoProgresso() {
        String waterText = String.format(
                Locale.getDefault(),
                getString(R.string.daily_goals_water_progress),
                currentWater,
                waterGoal
        );
        txtWaterProgress.setText(waterText);

        String exerciseText = String.format(
                Locale.getDefault(),
                getString(R.string.daily_goals_exercise_progress),
                currentExercise,
                exerciseGoal
        );
        txtExerciseProgress.setText(exerciseText);
        txtWaterProgress.setText(waterText);
        txtExerciseProgress.setText(exerciseText);

        // NOVO: Atualizar barras visuais (assumindo que você declarou as views e fez findViewById)

        if (waterGoal > 0) {
            int p = (int) ((currentWater / (float) waterGoal) * 100);
            progressWater.setProgress(Math.min(p, 100));
        }
        if (exerciseGoal > 0) {
            int p = (int) ((currentExercise / (float) exerciseGoal) * 100);
            progressExercise.setProgress(Math.min(p, 100));
        }
    }

    private void carregarLembrete() {
        int hour = prefs.getInt(KEY_DAILY_REMINDER_HOUR, 9);
        int minute = prefs.getInt(KEY_DAILY_REMINDER_MINUTE, 0);
        boolean enabled = prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerDailyReminder.setHour(hour);
            timePickerDailyReminder.setMinute(minute);
        } else {
            timePickerDailyReminder.setCurrentHour(hour);
            timePickerDailyReminder.setCurrentMinute(minute);
        }

        switchDailyReminder.setChecked(enabled);
    }

    private void salvarLembrete() {
        int hour, minute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePickerDailyReminder.getHour();
            minute = timePickerDailyReminder.getMinute();
        } else {
            hour = timePickerDailyReminder.getCurrentHour();
            minute = timePickerDailyReminder.getCurrentMinute();
        }

        boolean enabled = switchDailyReminder.isChecked();

        prefs.edit()
                .putInt(KEY_DAILY_REMINDER_HOUR, hour)
                .putInt(KEY_DAILY_REMINDER_MINUTE, minute)
                .putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled)
                .apply();

        if (enabled) {
            agendarLembreteDiario(hour, minute);
            Toast.makeText(this,
                    getString(R.string.toast_daily_reminder_saved),
                    Toast.LENGTH_SHORT).show();
        } else {
            cancelarLembreteDiario();
            Toast.makeText(this,
                    getString(R.string.toast_daily_reminder_disabled),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void agendarLembreteDiario(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, DailyGoalsReminderReceiver.class);
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
            // se o horário de hoje já passou, agenda para amanhã
            triggerAt += 24L * 60 * 60 * 1000L;
        }

        long interval = 24L * 60 * 60 * 1000L; // 1 dia

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                interval,
                pendingIntent
        );
    }

    private void cancelarLembreteDiario() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, DailyGoalsReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    private int parseIntOrDefault(EditText editText, int defaultValue) {
        String s = editText.getText().toString().trim();
        if (s.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
