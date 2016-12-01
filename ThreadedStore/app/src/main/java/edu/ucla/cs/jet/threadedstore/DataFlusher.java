package edu.ucla.cs.jet.threadedstore;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jnoor on 11/30/16.
 */

public class DataFlusher extends Thread {

    private File logfile;
    private RandomAccessFile logRAF;
    private boolean logOpen;

    private long log_offset;

    private BlockingQueue<DataEntry> queue;

    public DataFlusher(BlockingQueue<DataEntry> q) {
        this.queue = q;
        log_offset = 0;

        File sdCard = Environment.getExternalStorageDirectory();
        sdCard.mkdirs();
        File directory = new File(sdCard.getAbsolutePath(), "ThreadedStore");
        directory.mkdirs();

        logfile = new File(directory, "log");
        logOpen = false;
        try {
            logRAF = new RandomAccessFile(logfile, "rw");
            logOpen = true;
        } catch (Exception e) {
        }
    }

    public void run() {
        while(true) {
            try {
                if (queue == null) {
                    Log.d("ISSUE", "Queue is null!!!");
                } else {
                    DataEntry data = queue.take();
                    flush(data.value, data.topic);
                }
            } catch (Exception e) {
                Log.d("ERROR", e.getLocalizedMessage());
            }
        }
    }

    public long offset() {
        return log_offset;
    }

    public synchronized void clear() {
        try {
            logRAF.close();
            logOpen = false;
        } catch (Exception e) {}

        //empty out log file
        try {
            FileOutputStream los = new FileOutputStream(logfile, false);
            los.write(new byte[0]);
            los.close();
        } catch (Exception e) {
        }

        try {
            logRAF = new RandomAccessFile(logfile, "rw");
            logOpen = true;
        } catch (Exception e) {}

        log_offset = 0;
    }

    //Flush in-memory buffer
    //This "saves state"
    public synchronized void flush(byte [] buffer, int length) {
        try {
            if (!logOpen) {
                logRAF = new RandomAccessFile(logfile, "rw");
                logOpen = true;
            }
            if(logRAF.getFilePointer() != log_offset) {
                logRAF.seek(log_offset);
            }
            logRAF.write(buffer, 0, length);
        } catch (Exception e) {
            Log.e("DataStream", e.getLocalizedMessage());
        }

        log_offset = log_offset + length;
    }
}