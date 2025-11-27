package com.etheralltda.ozem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            restaurarLembreteInjecao(context);
            restaurarLembreteDiario(context);
        }
    }

    private void restaurarLembreteInjecao(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("glp1_prefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("reminder_enabled", false);

        if (!enabled) return;

        String medName = prefs.getString("reminder_med_name", "Medicamento");
        int hour = prefs.getInt("reminder_hour", 8);
        int minute = prefs.getInt("reminder_minute", 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent i = new Intent(context, ReminderReceiver.class);
        i.putExtra("medName", medName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long triggerAt = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();
        if (triggerAt <= now) {
            triggerAt += 7L * 24 * 60 * 60 * 1000L; // Próxima semana
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                7L * 24 * 60 * 60 * 1000L,
                pendingIntent
        );
    }

    private void restaurarLembreteDiario(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("glp1_prefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("daily_reminder_enabled", false);

        if (!enabled) return;

        int hour = prefs.getInt("daily_reminder_hour", 9);
        int minute = prefs.getInt("daily_reminder_minute", 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent i = new Intent(context, DailyGoalsReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long triggerAt = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();
        if (triggerAt <= now) {
            triggerAt += 24L * 60 * 60 * 1000L; // Amanhã
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}