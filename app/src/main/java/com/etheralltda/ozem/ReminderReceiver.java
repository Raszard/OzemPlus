package com.etheralltda.ozem;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.List;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "medication_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("med_name");
        String medDose = intent.getStringExtra("med_dose");

        if (medName == null) medName = "Medicamento";

        // 1. Exibir a Notificação (Código que você já tinha)
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lembretes de Medicamento",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificações para aplicar o medicamento");
            notificationManager.createNotificationChannel(channel);
        }

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Hora da Aplicação: " + medName)
                .setContentText("Lembrete para tomar sua dose de " + medDose)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(medName.hashCode(), builder.build());

        // --- CORREÇÃO: REAGENDAR PARA A PRÓXIMA VEZ (LOOP) ---
        // Carrega a lista para achar o medicamento completo (precisamos da frequência, dia, etc)
        List<Medication> medList = MedicationStorage.loadMedications(context);

        for (Medication med : medList) {
            if (med.getName().equals(medName)) {
                // Ao chamar scheduleMedication agora (que já passou do horário),
                // o NotificationScheduler automaticamente calcula a próxima data (amanhã/próxima semana)
                NotificationScheduler.scheduleMedication(context, med);
                break;
            }
        }
        // ----------------------------------------------------
    }
}