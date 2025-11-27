package com.etheralltda.ozem;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class UserStorage {

    private static final String PREFS_NAME = "glp1_prefs";
    private static final String KEY_USER_PROFILE = "user_profile";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_IS_PREMIUM = "is_premium";
    private static final String KEY_PROFILE_PHOTO = "profile_photo_uri";

    public static void savePhotoUri(Context context, String uriString) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PROFILE_PHOTO, uriString).apply();
    }

    public static String loadPhotoUri(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PROFILE_PHOTO, null);
    }

    public static void saveUserProfile(Context context, UserProfile profile) {
        if (profile == null) return;

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", profile.getName());
            obj.put("currentWeight", profile.getCurrentWeight());
            obj.put("targetWeight", profile.getTargetWeight());
            obj.put("height", profile.getHeight()); // Salva a altura
            obj.put("goalType", profile.getGoalType());
            obj.put("activityLevel", profile.getActivityLevel());
            obj.put("waterGoalLiters", profile.getWaterGoalLiters());
            obj.put("premium", profile.isPremium());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USER_PROFILE, obj.toString())
                .putBoolean(KEY_IS_PREMIUM, profile.isPremium())
                .apply();
    }

    public static UserProfile loadUserProfile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_USER_PROFILE, null);
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            JSONObject obj = new JSONObject(json);
            String name = obj.optString("name", "");
            float currentWeight = (float) obj.optDouble("currentWeight", 0.0);
            float targetWeight = (float) obj.optDouble("targetWeight", 0.0);
            float height = (float) obj.optDouble("height", 0.0); // Carrega a altura
            String goalType = obj.optString("goalType", "");
            String activityLevel = obj.optString("activityLevel", "");
            float waterGoal = (float) obj.optDouble("waterGoalLiters", 0.0);
            boolean premium = obj.optBoolean("premium", false);

            return new UserProfile(name, currentWeight, targetWeight, height, goalType,
                    activityLevel, waterGoal, premium);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isOnboardingDone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public static void setOnboardingDone(Context context, boolean done) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }

    public static boolean isPremium(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_PREMIUM, false);
    }

    public static void setPremium(Context context, boolean premium) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_PREMIUM, premium).apply();

        UserProfile profile = loadUserProfile(context);
        if (profile != null) {
            profile.setPremium(premium);
            saveUserProfile(context, profile);
        }
    }
}