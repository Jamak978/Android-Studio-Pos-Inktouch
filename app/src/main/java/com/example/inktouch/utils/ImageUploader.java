package com.example.inktouch.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageUploader {
    
    private static final String SUPABASE_URL = "https://qdeetwuxlqmijwkbolof.supabase.co";
    private static final String STORAGE_BUCKET = "products";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFkZWV0d3V4bHFtaWp3a2JvbG9mIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk4OTI0ODgsImV4cCI6MjA3NTQ2ODQ4OH0.4MGt8YGs4kbg2HhhywuEjGqpeLMH3W5Iu3_Tk7ZkVtw";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, String accessToken, UploadCallback callback) {
        new Thread(() -> {
            try {
                // Get file from URI
                File file = getFileFromUri(context, imageUri);
                if (file == null || !file.exists()) {
                    callback.onError("File not found");
                    return;
                }

                // Generate unique filename
                String fileName = "product_" + System.currentTimeMillis() + ".jpg";
                
                // Create request body
                RequestBody requestBody = RequestBody.create(
                    MediaType.parse("image/*"),
                    file
                );

                // Build multipart request
                MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "file",
                    fileName,
                    requestBody
                );

                // Create OkHttp client
                OkHttpClient client = new OkHttpClient();

                // Build request
                Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/storage/v1/object/" + STORAGE_BUCKET + "/" + fileName)
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(requestBody)
                    .build();

                // Execute request
                Response response = client.newCall(request).execute();
                
                if (response.isSuccessful()) {
                    // Build public URL
                    String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + STORAGE_BUCKET + "/" + fileName;
                    callback.onSuccess(publicUrl);
                } else {
                    callback.onError("Upload failed: " + response.message());
                }
                
                response.close();
                
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private static File getFileFromUri(Context context, Uri uri) {
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                return new File(filePath);
            }
            
            // Fallback: try to get file directly from URI
            return new File(uri.getPath());
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
