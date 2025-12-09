package com.etheralltda.ozem;

import android.content.Context;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicationUtils {

    public static String getLocalizedFrequency(Context context, String savedFrequency) {
        if (savedFrequency == null) return "";

        String lower = savedFrequency.toLowerCase();

        // Detecta Diário (PT: Diariamente, EN: Daily, ES: Diariamente)
        if (lower.contains("diari") || lower.contains("daily") || lower.contains("todos")) {
            return context.getString(R.string.quiz_freq_daily);
        }
        // Detecta Semanal (PT: Semanalmente, EN: Weekly, ES: Semanalmente)
        else if (lower.contains("seman") || lower.contains("weekly") || lower.contains("toda")) {
            return context.getString(R.string.quiz_freq_weekly);
        }
        // Detecta Mensal (PT: Mensalmente, EN: Monthly, ES: Mensualmente)
        else if (lower.contains("mensal") || lower.contains("mensual") || lower.contains("monthly") || lower.contains("todo dia")) {
            return context.getString(R.string.quiz_freq_monthly);
        }

        return savedFrequency;
    }

    public static String getLocalizedNextDate(Context context, Medication med) {
        String localizedFreq = getLocalizedFrequency(context, med.getFrequency());
        String time = extractTime(med.getNextDate());

        if (localizedFreq.equals(context.getString(R.string.quiz_freq_daily))) {
            return String.format(context.getString(R.string.schedule_format_daily), time);
        }
        else if (localizedFreq.equals(context.getString(R.string.quiz_freq_weekly))) {
            String dayName = getDayName(context, med.getDayOfWeek());
            return String.format(context.getString(R.string.schedule_format_weekly), dayName, time);
        }
        else if (localizedFreq.equals(context.getString(R.string.quiz_freq_monthly))) {
            int dayOfMonth = extractDayOfMonth(med.getNextDate());
            return String.format(context.getString(R.string.schedule_format_monthly), dayOfMonth, time);
        }

        return med.getNextDate();
    }

    public static String getDayName(Context context, int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return context.getString(R.string.day_domingo);
            case Calendar.MONDAY: return context.getString(R.string.day_segunda);
            case Calendar.TUESDAY: return context.getString(R.string.day_terca);
            case Calendar.WEDNESDAY: return context.getString(R.string.day_quarta);
            case Calendar.THURSDAY: return context.getString(R.string.day_quinta);
            case Calendar.FRIDAY: return context.getString(R.string.day_sexta);
            case Calendar.SATURDAY: return context.getString(R.string.day_sabado);
            default: return context.getString(R.string.day_generic);
        }
    }

    private static String extractTime(String text) {
        if (text == null) return "08:00";
        Pattern p = Pattern.compile("(\\d{2}:\\d{2})");
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1);
        return "08:00";
    }

    private static int extractDayOfMonth(String text) {
        if (text == null) return 1;
        Pattern p = Pattern.compile("\\b([1-9]|[12][0-9]|3[01])\\b");
        Matcher m = p.matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception e) {}
        }
        return 1;
    }
}