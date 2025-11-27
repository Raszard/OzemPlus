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

public class WeightStorage {

    private static final String PREFS_NAME = "glp1_prefs";
    private static final String KEY_WEIGHT_HISTORY = "weight_history";

    public static List<WeightEntry> loadWeights(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_WEIGHT_HISTORY, null);
        List<WeightEntry> list = new ArrayList<>();

        if (json == null || json.isEmpty()) {
            return list;
        }

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                long ts = obj.optLong("timestamp", 0L);
                double w = obj.optDouble("weight", 0.0);
                if (ts > 0 && w > 0) {
                    list.add(new WeightEntry(ts, (float) w));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Ordena por data (mais antigo primeiro)
        Collections.sort(list, new Comparator<WeightEntry>() {
            @Override
            public int compare(WeightEntry o1, WeightEntry o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });

        return list;
    }

    public static void saveWeights(Context context, List<WeightEntry> list) {
        if (list == null) list = new ArrayList<>();
        JSONArray arr = new JSONArray();
        for (WeightEntry e : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("timestamp", e.getTimestamp());
                obj.put("weight", e.getWeight());
                arr.put(obj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_WEIGHT_HISTORY, arr.toString()).apply();
    }

    public static void addWeight(Context context, WeightEntry entry) {
        List<WeightEntry> list = loadWeights(context);
        list.add(entry);
        // Ordena de novo
        Collections.sort(list, new Comparator<WeightEntry>() {
            @Override
            public int compare(WeightEntry o1, WeightEntry o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });
        saveWeights(context, list);
    }
}
