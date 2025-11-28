package com.etheralltda.ozem;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MedicationStorage {

    private static final String PREFS_NAME = "glp1_prefs";
    private static final String KEY_MED_LIST = "med_list";

    public static List<Medication> loadMedications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_MED_LIST, null);
        List<Medication> list = new ArrayList<>();

        if (json == null || json.isEmpty()) {
            return list;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.optString("name", "");
                String dose = obj.optString("dose", "");
                String frequency = obj.optString("frequency", "");
                String nextDate = obj.optString("nextDate", "");
                // Lê o dia da semana, padrão 0 se não existir
                int dayOfWeek = obj.optInt("dayOfWeek", 0);

                list.add(new Medication(name, dose, frequency, nextDate, dayOfWeek));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void saveMedications(Context context, List<Medication> list) {
        JSONArray array = new JSONArray();
        for (Medication med : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", med.getName());
                obj.put("dose", med.getDose());
                obj.put("frequency", med.getFrequency());
                obj.put("nextDate", med.getNextDate());
                // Salva o dia
                obj.put("dayOfWeek", med.getDayOfWeek());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_MED_LIST, array.toString()).apply();
    }
}