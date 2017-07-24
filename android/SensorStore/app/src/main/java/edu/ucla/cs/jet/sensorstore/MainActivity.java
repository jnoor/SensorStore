package edu.ucla.cs.jet.sensorstore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;
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

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            runBenchmark();
        }
    }

    public void runBenchmark() {

        for (int bytesPerWrite = 16; bytesPerWrite < 1024 * 1024 * 2; bytesPerWrite *= 2) {
            Log.i("------","--------------------------");
            Log.i("BYTES PER WRITE", String.valueOf(bytesPerWrite));
            Log.i("------","--------------------------");
            long totalBytes = 12 * 10000000;

            if(bytesPerWrite < 10) {
                //account for the massive amount of time it takes for this shit to run
                totalBytes /= 10;
            }

            final long numWrites = totalBytes / bytesPerWrite;
            long bytesWritten = runPebblesTest(numWrites, bytesPerWrite);
            runSQLiteTest(numWrites, bytesPerWrite);
            runSQLiteClusterTest(numWrites, bytesPerWrite);
            runOptimalTest(bytesWritten);
        }
    }

    private long runPebblesTest(long numWrites, int bytesPerWrite) {
        //SensorStore time
        SensorStore ds = new SensorStore();
        ds.clear();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            Log.d("TimeException", e.getLocalizedMessage());
        }

        byte [] xyz = new byte[bytesPerWrite];

        long starttime = System.nanoTime();

        for (int i=0; i<numWrites; i++) {
            ds.write(0, xyz);
        }

        ds.close();

        long endtime = System.nanoTime();

        double latency = (((double)(endtime - starttime)));
        double throughput = ((double)ds.offset()) / latency;

        Log.i("mKafka time", String.valueOf(endtime - starttime));
        Log.i("mKafka throughput", String.valueOf(throughput*1000) + " MBps");
        Log.i("mKafka offset", Long.toString(ds.offset()));

        return ds.offset();

    }

    private void runSQLiteTest(long numWrites, int bytesPerWrite) {
        //SQLite time
        MyDB db = new MyDB(getApplicationContext(), "DBTEST", DataStream.pagesize);
        db.clear();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            Log.d("TimeException", e.getLocalizedMessage());
        }

        String value = new String(new char[bytesPerWrite]);


        long start = System.nanoTime();

        //equal buffer size
        for (int i=0; i<numWrites; i++) {
            db.addRecord(0, value);
        }
        db.close();

        long end = System.nanoTime();

        double SQLlatency = (((double)(end - start)));
        double SQLthpt = ((double) db.getBytesWritten()) / SQLlatency;

        Log.i("SQLite time", String.valueOf(end - start));
        Log.i("SQLite throughput", String.valueOf(SQLthpt * 1000) + " MBps");
        Log.i("SQLite offset", Long.toString(db.getBytesWritten()));

    }

    private void runSQLiteClusterTest(long numWrites, int bytesPerWrite) {
        //SQLite time

        MyDB db1 = new MyDB(getApplicationContext(), "DB1", DataStream.pagesize / 4);
        MyDB db2 = new MyDB(getApplicationContext(), "DB2", DataStream.pagesize / 4);
        MyDB db3 = new MyDB(getApplicationContext(), "DB3", DataStream.pagesize / 4);
        MyDB db4 = new MyDB(getApplicationContext(), "DB4", DataStream.pagesize / 4);

        db1.clear();
        db2.clear();
        db3.clear();
        db4.clear();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            Log.d("TimeException", e.getLocalizedMessage());
        }

        String value = new String(new char[bytesPerWrite]);

        long start = System.nanoTime();

        //equal buffer size
        for (int i=0; i<numWrites; i++) {
            db1.addRecord(0, value);
            i++;
            db2.addRecord(0, value);
            i++;
            db3.addRecord(0, value);
            i++;
            db4.addRecord(0, value);
        }

        db1.close();
        db2.close();
        db3.close();
        db4.close();

        long end = System.nanoTime();

        double SQLlatency = (((double)(end - start)));
        double SQLthpt = ((double) db1.getBytesWritten() + db2.getBytesWritten() + db3.getBytesWritten() + db4.getBytesWritten()) / SQLlatency;

        Log.i("SQLiteCluster time", String.valueOf(end - start));
        Log.i("SQLiteCluster thruput", String.valueOf(SQLthpt * 1000) + " MBps");
        Log.i("SQLiteCluster offset", Long.toString(db1.getBytesWritten() + db2.getBytesWritten() + db3.getBytesWritten() + db4.getBytesWritten()));
    }

    private void runOptimalTest(long numBytes) {
        //Optimal SSD time
        try {
            TimeUnit.SECONDS.sleep(3);

            File sdCard = Environment.getExternalStorageDirectory();
            sdCard.mkdirs();
            File directory = new File(sdCard.getAbsolutePath(), "SensorStore");
            directory.mkdirs();

            File logfile = new File(directory, "optimal");
            RandomAccessFile logRAF = new RandomAccessFile(logfile, "rwd");

//            long numBytes = ds.offset();

            int flushSize = Math.min((int) numBytes, 32 * 1024 * 1024);

            long startOptimal = System.nanoTime();

            //heap buffer
            byte [] buf = new byte[flushSize];

            //direct byte buffer
//            byte [] buf = ByteBuffer.allocateDirect(flushSize).array();
            for(long i=0; i<numBytes; i+=flushSize) {
                logRAF.write(buf, 0, flushSize);
            }
            logRAF.close();
            long endOptimal = System.nanoTime();

            double latencyOpt = ((double) (endOptimal - startOptimal));
            double throughputOpt = ((double) numBytes) / latencyOpt;

            Log.i("optiml time", String.valueOf(endOptimal - startOptimal));
            Log.i("optiml throughput", String.valueOf(throughputOpt*1000) + " MBps");
            Log.i("optiml offset", Long.toString(numBytes));

        } catch (Exception e) {
            Log.e("DataStream", e.getLocalizedMessage());
        }
    }

    private void runOptimalSQLite() {
        //Optimal SQLite time
//        MyDB db = new MyDB(getApplicationContext());
//        db.clear();
//
//        try {
//            TimeUnit.SECONDS.sleep(3);
//        } catch (Exception e) {
//            Log.d("TimeException", e.getLocalizedMessage());
//        }
//
//        long startO = System.nanoTime();
//
//        db.beginTransaction();
//        for(int i=0; i<num; i++) {
//            db.addRecordOptimal(0, value);
//        }
//        db.commitTransaction();
//
//        long endO = System.nanoTime();
//
//        double SQLlatencyO = (((double)(endO - startO)));
//        double SQLthptO = ((double) db.getBytesWritten()) / SQLlatencyO;
//
//        Log.i("SQLite time", String.valueOf(endO - startO));
//        Log.i("SQLite throughput", String.valueOf(SQLthptO * 1000) + " MBps");
//        Log.i("SQLite offset", Long.toString(db.getBytesWritten()));
    }

}
