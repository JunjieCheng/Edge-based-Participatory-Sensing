package com.cheng.ebps_edge.Microservices;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.ebps_edge.HTTPClient;
import com.cheng.ebps_edge.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SubmitSensingTargetActivity extends AppCompatActivity {

    String gatewayUrl;
    String userId;
    String taskName;
    String microserviceName = "SubmitSensingTarget";

    TextView mTaskName;
    EditText mInputTarget;
    EditText mInputPrice;
    Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_sensing_target);

        gatewayUrl = Objects.requireNonNull(getIntent().getExtras()).getString("gatewayUrl");
        userId = getIntent().getExtras().getString("userId");
        taskName = getIntent().getExtras().getString("taskName");

        mTaskName = findViewById(R.id.task_name);
        mInputTarget = findViewById(R.id.input_target);
        mInputPrice = findViewById(R.id.input_price);
        mSubmit = findViewById(R.id.submit_button);

        mTaskName.setText(taskName);
    }

    public void onClickSubmit(View view) {
        new SubmitAsyncTask().execute();
    }

    public void onClickBack(View view) {
        finish();
    }

    private class SubmitAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            HTTPClient httpClient = new HTTPClient();
            Map<String, String> params = new HashMap<>();
            String response = "Error";

            params.put("action", "return");
            params.put("task_name", taskName);
            params.put("microservice_name", microserviceName);
            params.put("user_id", userId);
            params.put("data", mInputTarget.getText().toString());
            params.put("price", mInputPrice.getText().toString());

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
