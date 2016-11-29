package edu.ucla.cs.jet.sensorstore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

        SensorStore ds = new SensorStore();

        ds.clear();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            Log.d("TimeException", e.getLocalizedMessage());
        }

        Log.i("info:", "4B overhead in log, 12B overhead in index");

        byte [] xyz = new byte[12];
        int num = 1000000;
        long starttime = System.currentTimeMillis();

        for (int i=0; i<num; i++) {
            ds.write(0, xyz);
        }

//        ds.close();

        long endtime = System.currentTimeMillis();

        double latency = (((double)(endtime - starttime)) / 1000);
        double throughput = 16 * num / latency;

        Log.i("mKafka time", String.valueOf(endtime - starttime));
        Log.i("mKafka throughput", String.valueOf(throughput/1000000) + " MBps");

//        MyDB db = new MyDB(getApplicationContext());
//
//        long start = System.currentTimeMillis();
//
//        for (int i=0; i<num; i++) {
//            db.createRecords(256, 1337, 7331, 13);
//        }
//
//        long end = System.currentTimeMillis();
//
//        double SQLlatency = (((double)(end - start)) / 1000);
//        double SQLthpt = 16 * num / SQLlatency;
//
//        Log.i("SQLite time", String.valueOf(end - start));
//        Log.i("SQLite throughput", String.valueOf(SQLthpt) + " Bps");

//        ds.write(12, "Yo\n");
//        ds.write(12, "Testees\n");

        Log.i("Offset", Long.toString(ds.offset()));
//
//        Iterator<DataEntry> it = ds.read(0, ds.offset());
//        while (it.hasNext()) {
//            DataEntry datum = it.next();
//            Log.i("read ", " " + datum.topic + ":" + new String(datum.value));
//        }
    }
}
