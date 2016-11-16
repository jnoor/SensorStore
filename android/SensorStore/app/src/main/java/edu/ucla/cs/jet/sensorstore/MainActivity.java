package edu.ucla.cs.jet.sensorstore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
        ds.write(0, "Yo\n");
        ds.write(0, "Testees\n");
        ds.close();

        Log.i("READALL", ds.readAll());
        Log.i("Offset", Long.toString(ds.offset()));
        Log.i("READALL2", new String(ds.read(0, ds.offset())));
    }
}
