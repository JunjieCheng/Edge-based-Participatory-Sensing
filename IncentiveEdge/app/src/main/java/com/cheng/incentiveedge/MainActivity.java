package com.cheng.incentiveedge;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String routerIP = "http://192.168.1.1:81/incentive/IncentiveRouter.php";

    public static final int USER_INITIATED = 0x1000;
    public static final int APP_INITIATED = 0x2000;
    public static final int SERVER_INITIATED =0x3000;

    private TextView mLog;
    private HTTPClient httpClient;
    private Random random;
    private BidAsyncTask execution;

    private double frequencyBase;
    private double priceBase;

    private long requestCount;
    private long latencySum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLog = findViewById(R.id.log);
        httpClient = new HTTPClient();
        execution = new BidAsyncTask();

        mLog.setMovementMethod(new ScrollingMovementMethod());

        random = new Random();
        frequencyBase = random.nextGaussian() * 0.2 + 0.5;
        priceBase = random.nextGaussian() * 2 + 5;

        requestCount = 0;
        latencySum = 0;

        mLog.append("Frequency: " + frequencyBase);
        mLog.append("\nPrice: " + priceBase);
        mLog.append("\n");
    }

    public void onClickStart(View view) {
        mLog.append("Execution starts\n");
        Log.d("Main", execution.getStatus() + "");

        TrafficStats.setThreadStatsTag(APP_INITIATED);

        if (execution.isCancelled()) {
            execution = new BidAsyncTask();
            execution.execute();
        } else {
            execution.execute();
        }
    }

    public void onClickStop(View view) {
        mLog.append("Execution ends\n");

        mLog.append("Request count: " + requestCount);
        mLog.append("Average latency: " + (double)latencySum / requestCount);

        if (!execution.isCancelled()) {
            execution.cancel(true);
        }
    }

    public void onClickClear(View view) {
        mLog.setText("");
    }

    public class BidAsyncTask extends AsyncTask<Void, String, Void> {

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mLog.append(values[0]);
            requestCount++;
            latencySum += Long.parseLong(values[1]);
            Editable editable = mLog.getEditableText();
            Selection.setSelection(editable, editable.length());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!execution.isCancelled()) {
                if (random.nextDouble() <= frequencyBase) {
                    Log.d("Execution", "Posting");
                    double bid = random.nextGaussian() * 1 + priceBase;
                    bid = Math.max(1.0, bid);
                    String response = "";

                    long start = System.currentTimeMillis();

                    try {
                        response = httpClient.post(routerIP, "test", bid + "");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String latency = (System.currentTimeMillis() - start) + "";

                    publishProgress(response, latency);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
    }

}
