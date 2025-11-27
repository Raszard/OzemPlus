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

public class InjectionStorage {

    private static final String PREFS_NAME = "glp1_prefs";
    private static final String KEY_INJECTION_HISTORY = "injection_history";

    public static List<InjectionEntry> loadInjections(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_INJECTION_HISTORY, null);
        List<InjectionEntry> list = new ArrayList<>();

        if (json == null || json.isEmpty()) {
            return list;
        }

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                long ts = obj.optLong("timestamp", 0L);
                String medName = obj.optString("medicationName", "");
                String locationCode = obj.optString("locationCode", "");
                if (ts > 0 && locationCode != null && !locationCode.isEmpty()) {
                    list.add(new InjectionEntry(ts, medName, locationCode));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(list, new Comparator<InjectionEntry>() {
            @Override
            public int compare(InjectionEntry o1, InjectionEntry o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });

        return list;
    }

    public static void saveInjections(Context context, List<InjectionEntry> list) {
        if (list == null) list = new ArrayList<>();
        JSONArray arr = new JSONArray();
        for (InjectionEntry e : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("timestamp", e.getTimestamp());
                obj.put("medicationName", e.getMedicationName());
                obj.put("locationCode", e.getLocationCode());
                arr.put(obj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_INJECTION_HISTORY, arr.toString()).apply();
    }

    public static void addInjection(Context context, InjectionEntry entry) {
        List<InjectionEntry> list = loadInjections(context);
        list.add(entry);
        Collections.sort(list, new Comparator<InjectionEntry>() {
            @Override
            public int compare(InjectionEntry o1, InjectionEntry o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });
        saveInjections(context, list);
    }

    public static InjectionEntry getLastInjectionForMed(Context context, String medicationName) {
        if (medicationName == null) return null;
        List<InjectionEntry> list = loadInjections(context);
        for (int i = list.size() - 1; i >= 0; i--) {
            InjectionEntry e = list.get(i);
            if (medicationName.equals(e.getMedicationName())) {
                return e;
            }
        }
        return null;
    }
}
