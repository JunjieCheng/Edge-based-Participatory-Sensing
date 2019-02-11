package com.cheng.tourguide;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    private Toolbar mTopToolbar;
    private WifiManager mWifiManager;

    private List mWifiList;
    private List<ScanResult> mWifiIdList;
    private ImageView mMapImage;

    private HTTPClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);

        mMapImage = findViewById(R.id.map);

        httpClient = new HTTPClient();

        BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
//                invalidateOptionsMenu();
            }
        };

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Check WiFi permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            mWifiManager.startScan();
            mWifiList = mWifiManager.getScanResults();
        }

        menu.clear();

        if (!mWifiList.isEmpty()) {
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_wifi_white_24dp);
            mTopToolbar.setOverflowIcon(drawable);
        } else {
            menu.add(Menu.NONE, 0, 0, "No WiFi available");
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_signal_wifi_off_white_24dp);
            mTopToolbar.setOverflowIcon(drawable);
        }

        Map<String, ScanResult> wifiMap = new HashMap<>();

        for (Object wifi : mWifiList) {
            if (((ScanResult)wifi).SSID.isEmpty()) {
                continue;
            }

            if (wifiMap.containsKey(((ScanResult)wifi).SSID)
                    && wifiMap.get(((ScanResult)wifi).SSID).level < ((ScanResult)wifi).level) {
                wifiMap.put(((ScanResult)wifi).SSID, (ScanResult) wifi);
            }
            else if (!wifiMap.containsKey(((ScanResult)wifi).SSID)) {
                wifiMap.put(((ScanResult)wifi).SSID, (ScanResult) wifi);
            }
        }

        Set<ScanResult> wifiSet = new TreeSet<ScanResult>((a, b) -> b.level - a.level);
        int id = 1;

        for (String wifi : wifiMap.keySet()) {
            wifiSet.add(wifiMap.get(wifi));
        }

        for (ScanResult wifi : wifiSet) {
            menu.add(Menu.NONE, id, id, wifi.SSID);
            id++;
//            Log.d("Wifi", ((ScanResult) wifi).SSID);
//            Log.d("Size", "" + menu.size());
        }

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.wifi) {
//            Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickQuery(View view) {
        new QueryAsyncTask().execute("http://192.168.1.164:8890/gateway/route.php", "route");
    }

    private class QueryAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String response = "";

            try {
                response = httpClient.post(strings[0], strings[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("map124")) {
                mMapImage.setImageResource(R.drawable.map124);
            }
            else if (s.equals("map142")) {
                mMapImage.setImageResource(R.drawable.map142);
            }
            else if (s.equals("map214")) {
                mMapImage.setImageResource(R.drawable.map214);
            }
            else if (s.equals("map241")) {
                mMapImage.setImageResource(R.drawable.map241);
            }
            else if (s.equals("map412")) {
                mMapImage.setImageResource(R.drawable.map412);
            }
            else if (s.equals("map421")) {
                mMapImage.setImageResource(R.drawable.map421);
            }
        }
    }

}
