package com.cheng.ebps_edge;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cheng.ebps_edge.Models.Microservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ListView mListView;
    List<Microservice> mMicroserviceList;
    MicroserviceListAdapter mAdapter;

    HTTPClient mHTTPClient;

    final String gatewayUrl = "http://192.168.1.164:8081/ebps";
    String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.microservice_listView);
        mMicroserviceList = new ArrayList<>();

        mAdapter = new MicroserviceListAdapter(getApplicationContext(), R.layout.item_microservice, mMicroserviceList);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String className = mMicroserviceList.get(i).className;

                try {
                    Class microservice = Class.forName("com.cheng.ebps_edge.Microservices." + className);
                    Intent intent = new Intent(getApplicationContext(), microservice);
                    intent.putExtra("gatewayUrl", gatewayUrl);
                    intent.putExtra("taskName", mMicroserviceList.get(i).taskName);
                    intent.putExtra("userId", userId);

                    startActivityForResult(intent, 0);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.FEATURE_CAMERA_ANY};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        mHTTPClient = new HTTPClient();

        new RefreshAsyncTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            new RefreshAsyncTask().execute();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onClickGenerateKey(View view) {
        userId = UUID.randomUUID().toString();

        Toast toast = Toast.makeText(getBaseContext(), userId, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onClickRefresh(View view) {
        new RefreshAsyncTask().execute();

        Toast toast = Toast.makeText(getBaseContext(), "Refreshed", Toast.LENGTH_SHORT);
        toast.show();
    }

    private class RefreshAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            mMicroserviceList.clear();
            Map<String, String> params = new HashMap<>();
            params.put("action", "list");
            params.put("device", "mobile_phone");
            params.put("user_id", userId);

            // Response format: microserviceName|className,
            String response = "";

            try {
                response = mHTTPClient.get(gatewayUrl, params);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.trim().equals("")) {
                return;
            }

            mMicroserviceList.clear();
            String[] microservices = response.trim().split(",");

            for (String microservice : microservices) {
                String[] pair = microservice.split("\\|");
                mMicroserviceList.add(new Microservice(pair[0], pair[1]));
            }

            mAdapter.notifyDataSetChanged();
        }
    }

}
