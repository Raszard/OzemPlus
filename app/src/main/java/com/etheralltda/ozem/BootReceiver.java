package com.etheralltda.ozem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            // --- CORREÇÃO: Reagendar Medicamentos da Lista ---
            List<Medication> medList = MedicationStorage.loadMedications(context);

            for (Medication med : medList) {
                // Isso vai recalcular o próximo alarme para cada remédio e agendar
                NotificationScheduler.scheduleMedication(context, med);
            }

            // --- Reagendar Metas Diárias (Se necessário) ---
            // (Mantenha sua lógica de DailyGoalsReminderReceiver aqui se ela usar SharedPreferences separadas)
            // restaurarLembreteDiario(context);
        }
    }

    // ... mantenha o método restaurarLembreteDiario se ele for usado para a tela de Metas Diárias ...
}