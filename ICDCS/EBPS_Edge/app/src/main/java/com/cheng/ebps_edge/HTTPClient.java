package com.cheng.ebps_edge;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class HTTPClient {

    private OkHttpClient httpClient;

    public HTTPClient() {
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        this.httpClient = new OkHttpClient.Builder()
//                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String get(String url, Map<String, String> params) throws IOException {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();

        if (params != null) {
            for (String key : params.keySet()) {
                httpBuilder.addQueryParameter(key, params.get(key));
            }
        }

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();

        Response response = httpClient.newCall(request).execute();

        return response.body().string();
    }

    public String getFile(Context context, String url, Map<String, String> params) throws IOException {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();

        if (params != null) {
            for (String key : params.keySet()) {
                httpBuilder.addQueryParameter(key, params.get(key));
            }
        }

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();

        Response response = httpClient.newCall(request).execute();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = context.getFilesDir().getAbsolutePath() + File.separator + "IMG_" + timeStamp + ".jpg";
        Log.d("File path", context.getFilesDir().getAbsolutePath());
        File file = new File(fileName);

        BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.writeAll(response.body().source());
        sink.close();

        return fileName;
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

    public String postImage(String url, String fileName, Bitmap image) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        MultipartBody.Builder body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

//        for (String key : params.keySet()) {
//            body.addFormDataPart(key, params.get(key));
//        }

        body.addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("image/*"), byteArray));

        Request request = new Request.Builder()
                .url(url)
                .post(body.build())
                .build();

        Response response = httpClient.newCall(request).execute();

        return response.body().string();
    }
}