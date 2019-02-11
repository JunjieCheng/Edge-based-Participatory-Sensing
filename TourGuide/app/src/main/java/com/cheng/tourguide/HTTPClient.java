package com.cheng.tourguide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HTTPClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient httpClient;

    HTTPClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    public String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = httpClient.newCall(request).execute();

        return response.body().string();
    }

    public String post(String url, String task) throws IOException {

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("task", task);
        RequestBody body = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();

        return response.body().string();
    }

    public String postImage(String url, String imagePath) throws IOException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 8;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", RequestBody.create(MediaType.parse("image/*"), byteArray))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();

        Log.d("Post Image", response.body().string());

//        return response.body().string();
        return "";
    }
}