package com.etheralltda.ozem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class NotificationScheduler {

    public static void scheduleMedication(Context context, Medication med) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("med_name", med.getName());
        intent.putExtra("med_dose", med.getDose());

        int notificationId = med.getName().hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long alarmTime = calculateNextTime(context, med); // Agora passa Context

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);

            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                );
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private static long calculateNextTime(Context context, Medication med) {
        Calendar now = Calendar.getInstance();
        Calendar alarm = Calendar.getInstance();

        int hour = 8;
        int minute = 0;

        try {
            String txt = med.getNextDate();
            // REFATORADO: Usando string resource para separador
            String separatorAt = context.getString(R.string.separator_at);
            if (txt.contains(separatorAt)) {
                String timePart = txt.substring(txt.indexOf(separatorAt) + separatorAt.length()).trim();
                String[] parts = timePart.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        alarm.set(Calendar.HOUR_OF_DAY, hour);
        alarm.set(Calendar.MINUTE, minute);
        alarm.set(Calendar.SECOND, 0);

        // REFATORADO: Comparando com resources
        String daily = context.getString(R.string.quiz_freq_daily);
        String weekly = context.getString(R.string.quiz_freq_weekly);
        String monthly = context.getString(R.string.quiz_freq_monthly);

        if (med.getFrequency().equals(daily)) {
            if (alarm.before(now)) {
                alarm.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else if (med.getFrequency().equals(weekly)) {
            alarm.set(Calendar.DAY_OF_WEEK, med.getDayOfWeek());
            if (alarm.before(now)) {
                alarm.add(Calendar.WEEK_OF_YEAR, 1);
            }
        } else if (med.getFrequency().equals(monthly)) {
            int dayOfMonth = 1;
            try {
                String txt = med.getNextDate();
                String sepDay = context.getString(R.string.separator_every_day);
                String sepAt = context.getString(R.string.separator_at);

                if (txt.contains(sepDay)) {
                    // Extrai o dia entre "Todo dia " e " às"
                    int start = txt.indexOf(sepDay) + sepDay.length();
                    int end = txt.indexOf(sepAt);
                    if (end > start) {
                        String sub = txt.substring(start, end);
                        dayOfMonth = Integer.parseInt(sub.trim());
                    }
                }
            } catch (Exception e) {}

            alarm.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if (alarm.before(now)) {
                alarm.add(Calendar.MONTH, 1);
            }
        }

        return alarm.getTimeInMillis();
    }

    public static void cancelMedication(Context context, Medication med) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        int notificationId = med.getName().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}