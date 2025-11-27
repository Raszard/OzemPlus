package com.etheralltda.ozem; // Verifique se o pacote está correto

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PhotoStorage {
    private static final String PREFS_NAME = "ozem_photos";
    private static final String KEY_PHOTO_LIST = "photo_gallery_list";

    // Classe interna para representar uma foto
    public static class PhotoEntry {
        private String uriString;
        private long timestamp;

        public PhotoEntry(String uriString, long timestamp) {
            this.uriString = uriString;
            this.timestamp = timestamp;
        }

        public String getUriString() { return uriString; }
        public long getTimestamp() { return timestamp; }
    }

    public static void savePhotoEntry(Context context, PhotoEntry newEntry) {
        List<PhotoEntry> currentList = loadPhotos(context);
        currentList.add(newEntry);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(currentList);
        prefs.edit().putString(KEY_PHOTO_LIST, json).apply();
    }

    public static List<PhotoEntry> loadPhotos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PHOTO_LIST, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<PhotoEntry>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Helper para salvar o Bitmap da câmera em um arquivo real
    public static String saveBitmapToFile(Context context, Bitmap bitmap) {
        File directory = new File(context.getFilesDir(), "progress_photos");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String fileName = "IMG_" + UUID.randomUUID().toString() + ".jpg";
        File file = new File(directory, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return Uri.fromFile(file).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}