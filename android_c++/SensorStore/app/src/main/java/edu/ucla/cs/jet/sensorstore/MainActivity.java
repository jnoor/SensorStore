package edu.ucla.cs.jet.sensorstore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    @Override
    protected void onResume() {
        super.onResume();

        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/SensorStore");
        directory.mkdirs();

        File logfile = new File(directory, "clog");
        File indexfile = new File(directory, "cindex");

        SensorStoreSetup(logfile.getAbsolutePath(), indexfile.getAbsolutePath());

        long num = 1000000;
        long starttime = System.currentTimeMillis();

        for(int i=0; i<num; i++) {
            SensorStoreWrite(23, "123456789012");
        }

        SensorStoreClose();

        long endtime = System.currentTimeMillis();

        double latency = (((double)(endtime - starttime)) / 1000);
        double throughput = 16 * num / latency;

        Log.i("mKafka time", String.valueOf(endtime - starttime));
        Log.i("mKafka throughput", String.valueOf(throughput/1000000) + " MBps");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native void SensorStoreSetup(String logfile, String indexfile);
    public native void SensorStoreWrite(int topic, String value);
    public native void SensorStoreClose();
}
