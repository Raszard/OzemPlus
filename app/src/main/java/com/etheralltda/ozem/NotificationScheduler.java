package com.etheralltda.ozem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class NotificationScheduler {

    public static void scheduleMedication(Context context, Medication med) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("med_name", med.getName());
        intent.putExtra("med_dose", med.getDose());

        // Usamos o hashCode do nome para criar um ID único para este alarme
        int notificationId = med.getName().hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Calcular o tempo do alarme
        long alarmTime = calculateNextTime(med);

        // Agendar (setExactAndAllowWhileIdle garante que toque mesmo se o celular estiver em modo soneca)
        if (alarmManager != null) {
            // Cancela anterior se houver (para edições)
            alarmManager.cancel(pendingIntent);

            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                );
                Log.d("OzemScheduler", "Alarme agendado para: " + alarmTime);
            } catch (SecurityException e) {
                e.printStackTrace(); // Lidar com permissão no Android 12+ se necessário
            }
        }
    }

    private static long calculateNextTime(Medication med) {
        Calendar now = Calendar.getInstance();
        Calendar alarm = Calendar.getInstance();

        // Extrair hora e minuto da string salva (ex: "... às 08:00") ou do objeto se tivéssemos salvo separado
        // Vamos parsear a string nextDoseDate que salvamos: "Toda Segunda às 08:00"
        int hour = 8; // Padrão
        int minute = 0;

        try {
            String txt = med.getNextDate();
            if (txt.contains(" às ")) {
                String timePart = txt.substring(txt.indexOf(" às ") + 4).trim();
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

        if (med.getFrequency().equals("Diariamente")) {
            // Se o horário já passou hoje, agendar para amanhã
            if (alarm.before(now)) {
                alarm.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else if (med.getFrequency().equals("Semanalmente")) {
            alarm.set(Calendar.DAY_OF_WEEK, med.getDayOfWeek());
            // Se o dia/hora já passou nesta semana, agendar para próxima
            if (alarm.before(now)) {
                alarm.add(Calendar.WEEK_OF_YEAR, 1);
            }
        } else if (med.getFrequency().equals("Mensalmente")) {
            // Tenta pegar o dia do mês da string "Todo dia X..."
            int dayOfMonth = 1;
            try {
                String txt = med.getNextDate(); // "Todo dia 15 às..."
                if (txt.contains("Todo dia ")) {
                    String sub = txt.substring(9, txt.indexOf(" às"));
                    dayOfMonth = Integer.parseInt(sub.trim());
                }
            } catch (Exception e) {}

            // Ajusta o dia. Cuidado com meses que não tem dia 31 (o Calendar ajusta auto, mas é bom saber)
            alarm.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if (alarm.before(now)) {
                alarm.add(Calendar.MONTH, 1);
            }
        }

        return alarm.getTimeInMillis();
    }

    // Método para cancelar alarme (usado ao excluir)
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