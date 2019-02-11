package com.cheng.ebps_edge.Microservices;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cheng.ebps_edge.HTTPClient;
import com.cheng.ebps_edge.R;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.HOGDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.opencv.imgcodecs.Imgcodecs.imread;

public class RecognizeAvailableSpaceActivity extends AppCompatActivity {

    String gatewayUrl;
    String userId;
    String taskName;
    String microserviceName = "RecognizeAvailableSpace";

    EditText mInputPrice;

    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_available_space);

        gatewayUrl = Objects.requireNonNull(getIntent().getExtras()).getString("gatewayUrl");
        userId = getIntent().getExtras().getString("userId");
        taskName = getIntent().getExtras().getString("taskName");

        mInputPrice = findViewById(R.id.input_price);
    }

    public void onClickSubmit(View view) {
        new SubmitPriceAsyncTask().execute();
    }

    public void onClickBack(View view) {
        finish();
    }

    private class SubmitPriceAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            HTTPClient httpClient = new HTTPClient();

            Map<String, String> params = new HashMap<>();
            String response = "Error";

            params.put("action", "request");
            params.put("task_name", taskName);
            params.put("microservice_name", microserviceName);
            params.put("user_id", userId);
            params.put("price", mInputPrice.getText().toString());

            try {
                response = httpClient.getFile(getApplicationContext(), gatewayUrl, params);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String fileName) {
//            Toast toast = Toast.makeText(getBaseContext(), fileName, Toast.LENGTH_SHORT);
//            toast.show();

            HOGDescriptor hog = new HOGDescriptor();
            MatOfFloat descriptors = HOGDescriptor.getDefaultPeopleDetector();
            hog.setSVMDetector(descriptors);

            Mat mat = imread(fileName);
            final MatOfRect foundPersons = new MatOfRect();
            final MatOfDouble foundWeights = new MatOfDouble();
            final Size winStride = new Size(8, 8);
            final Size padding = new Size(32, 32);

            hog.detectMultiScale(mat, foundPersons, foundWeights, 0.0,
                    winStride, padding, 1.05, 2.0, false);

            Log.d("People Found", "" + foundPersons.rows());
        }
    }

    private class SubmitRecognitionResultAsyncTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... integers) {
            HTTPClient httpClient = new HTTPClient();

            Map<String, String> params = new HashMap<>();
            String response = "Error";

            params.put("action", "return");
            params.put("task_name", taskName);
            params.put("microservice_name", microserviceName);
            params.put("user_id", userId);

            String data;

            if (integers[0] == 0) {
                data = "Empty";
            } else if (integers[0] < 5) {
                data = "Low";
            } else if (integers[0] < 10) {
                data = "Medium";
            } else if (integers[0] < 15) {
                data = "High";
            } else {
                data = "Full";
            }

            params.put("data", data);

            try {
                response = httpClient.getFile(getApplicationContext(), gatewayUrl, params);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }
    }
}
