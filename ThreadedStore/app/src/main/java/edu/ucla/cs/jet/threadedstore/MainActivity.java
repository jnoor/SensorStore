package edu.ucla.cs.jet.threadedstore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        DataStore ds = new DataStore();

        ds.clear();

        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (Exception e) {
            Log.d("TimeException", e.getLocalizedMessage());
        }

        byte [] xyz = new byte[12];
        int num = 1000000;

        long start = System.nanoTime();

        for(int i=0; i<num; i++) {
            ds.write(23, xyz);
        }

        ds.close();

        long end = System.nanoTime();

        double latency_ns = ((double) end - start);
        double throughput_MB = 16 * num / latency_ns * 1000;

        Log.i("mKafka time", String.valueOf(latency_ns));
        Log.i("mKafka throughput", String.valueOf(throughput_MB) + " MBps");
        Log.i("offset", String.valueOf(ds.offset()));


        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            Log.d("TimeException", e.getLocalizedMessage());
        }

        Log.i("offset", String.valueOf(ds.offset()));

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            Log.d("TimeException", e.getLocalizedMessage());
        }

        Log.i("offset", String.valueOf(ds.offset()));
    }
}
