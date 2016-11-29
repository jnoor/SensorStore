package edu.ucla.cs.jet.sensorstore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Iterator;

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

        ds.write(12, "Yo\n");
        ds.write(12, "Testees\n");
        ds.close();

        Log.i("Offset", Long.toString(ds.offset()));

        Iterator<DataEntry> it = ds.read(0, ds.offset());
        while (it.hasNext()) {
            DataEntry datum = it.next();
            Log.i("read ", " " + datum.topic + ":" + new String(datum.value));
        }
    }
}
