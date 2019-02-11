package com.cheng.ebps_edge.Microservices;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cheng.ebps_edge.HTTPClient;
import com.cheng.ebps_edge.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class TakePhotoActivity extends AppCompatActivity {

    String gatewayUrl;
    String gatewayFileUploadUrl;
    String userId;
    String taskName;
    String microserviceName = "TakePhoto";

    ImageView mImageView;
    Uri file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        gatewayUrl = Objects.requireNonNull(getIntent().getExtras()).getString("gatewayUrl");
        gatewayFileUploadUrl = "http://192.168.1.164:8081/fileupload";
        userId = getIntent().getExtras().getString("userId");
        taskName = getIntent().getExtras().getString("taskName");

        mImageView = findViewById(R.id.image);
    }

    public void onClickTakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = FileProvider.getUriForFile(getApplicationContext(),
                getApplicationContext().getApplicationContext().getPackageName() + ".com.cheng.ebps_edge.Microservices.provider",
                getOutputMediaFile());
        Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);

        startActivityForResult(intent, 100);
    }

    public void onClickSubmitPhoto(View view) {
        new FileUploadAsyncTask().execute();
        finish();
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Data");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                mImageView.setImageURI(file);
            }
        }
    }

    private class FileUploadAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            HTTPClient httpClient = new HTTPClient();
            String response = "Error";

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), file);
                response = httpClient.postImage(gatewayFileUploadUrl, file.getLastPathSegment(), bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast toast = Toast.makeText(getBaseContext(), s.trim(), Toast.LENGTH_SHORT);
            toast.show();
            new ReturnImageAsyncTask().execute();
        }
    }

    private class ReturnImageAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            HTTPClient httpClient = new HTTPClient();
            Map<String, String> params = new HashMap<>();
            String response = "Error";

            params.put("action", "return");
            params.put("user_id", userId);
            params.put("task_name", taskName);
            params.put("microservice_name", microserviceName);
            params.put("file_name", file.getLastPathSegment());

            try {
                response = httpClient.get(gatewayUrl, params);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast toast = Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT);
            toast.show();

            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
            finish();
        }
    }

}
