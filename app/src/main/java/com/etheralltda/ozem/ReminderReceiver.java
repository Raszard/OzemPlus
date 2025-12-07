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

        if (medName == null) medName = context.getString(R.string.injection_med_default);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.reminder_notification_channel),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.reminder_notification_desc));
            notificationManager.createNotificationChannel(channel);
        }

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.reminder_notification_title, medName))
                .setContentText(context.getString(R.string.reminder_notification_text, medDose))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(medName.hashCode(), builder.build());

        List<Medication> medList = MedicationStorage.loadMedications(context);

        for (Medication med : medList) {
            if (med.getName().equals(medName)) {
                NotificationScheduler.scheduleMedication(context, med);
                break;
            }
        }
    }
}