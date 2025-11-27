package com.etheralltda.ozem;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SymptomStorage {

    private static final String PREFS_NAME = "glp1_prefs";
    private static final String KEY_SYMPTOM_HISTORY = "symptoms_history";

    public static List<SymptomEntry> loadSymptoms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SYMPTOM_HISTORY, null);
        List<SymptomEntry> list = new ArrayList<>();

        if (json == null || json.isEmpty()) {
            return list;
        }

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                long ts = obj.optLong("timestamp", 0L);
                int nausea = obj.optInt("nausea", 0);
                int fatigue = obj.optInt("fatigue", 0);
                int satiety = obj.optInt("satiety", 0);
                String notes = obj.optString("notes", "");

                if (ts > 0) {
                    list.add(new SymptomEntry(ts, nausea, fatigue, satiety, notes));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(list, new Comparator<SymptomEntry>() {
            @Override
            public int compare(SymptomEntry o1, SymptomEntry o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });

        return list;
    }

    public static void saveSymptoms(Context context, List<SymptomEntry> list) {
        if (list == null) list = new ArrayList<>();
        JSONArray arr = new JSONArray();
        for (SymptomEntry e : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("timestamp", e.getTimestamp());
                obj.put("nausea", e.getNausea());
                obj.put("fatigue", e.getFatigue());
                obj.put("satiety", e.getSatiety());
                obj.put("notes", e.getNotes());
                arr.put(obj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SYMPTOM_HISTORY, arr.toString()).apply();
    }

    public static void addSymptom(Context context, SymptomEntry entry) {
        List<SymptomEntry> list = loadSymptoms(context);
        list.add(entry);
        Collections.sort(list, new Comparator<SymptomEntry>() {
            @Override
            public int compare(SymptomEntry o1, SymptomEntry o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });
        saveSymptoms(context, list);
    }
}
