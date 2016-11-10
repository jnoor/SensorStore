package edu.ucla.cs.jet.sensorstore;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/SensorStore");
        directory.mkdirs();

        File logfile = new File(directory, "log");
        File indexfile = new File(directory, "index");
        File offsetfile = new File(directory, "offset");

        SensorStoreSetup(logfile.getAbsolutePath(), indexfile.getAbsolutePath(), offsetfile.getAbsolutePath());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native void SensorStoreSetup(String logfile, String indexfile, String offsetfile);
}
