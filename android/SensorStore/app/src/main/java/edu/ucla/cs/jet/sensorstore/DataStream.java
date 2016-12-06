package edu.ucla.cs.jet.sensorstore;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jnoor on 11/10/16.
 */

public class DataStream {
    static final int pagesize = 8 * 1024 * 1024;
    static final long logsize = 512 * 1024 * 1024;

    private File logfile;
    private RandomAccessFile logRAF;
    private boolean logOpen;
    private long log_offset;

    private byte [] buffer;
    private int buffer_offset;

    private IndexStream index;

    public DataStream() {
        index = new IndexStream();

        buffer = new byte[pagesize];
        buffer_offset = 0;

        File sdCard = Environment.getExternalStorageDirectory();
        sdCard.mkdirs();
        File directory = new File(sdCard.getAbsolutePath(), "SensorStore");
        directory.mkdirs();

        logfile = new File(directory, "log");
        logOpen = false;
        try {
            logRAF = new RandomAccessFile(logfile, "rwd");
            logOpen = true;
        } catch (Exception e) {
        }

    }

    public long offset() {
        return log_offset + buffer_offset;
    }

    //write the value to the buffer
    //written as: (topic)(valuesize)(value)
    //returns: offset => offset into the log file
    public void write(int topic, byte [] value) {

        int size_to_write =  4 + 4 + value.length;
        long offset = log_offset + buffer_offset;

        if (size_to_write > pagesize) {
            throw new Error("Trying to write value larger than the pagesize!");
        }
        if (buffer_offset + size_to_write >= pagesize) {
            flushBuffer();
        }

        byte[] topicBuf = new byte[] {
                (byte)(topic >>> 24),
                (byte)(topic >>> 16),
                (byte)(topic >>> 8),
                (byte)(topic)
        };
//        byte[] topicBuf = ByteBuffer.allocate(4).putInt(topic).array();
        System.arraycopy(topicBuf, 0, buffer, buffer_offset, 4);
        buffer_offset += 4;

        byte[] lenBuf = new byte[] {
                (byte)(value.length >>> 24),
                (byte)(value.length >>> 16),
                (byte)(value.length >>> 8),
                (byte)(value.length)
        };
//        byte[] lenBuf = ByteBuffer.allocate(4).putInt(value.length).array();
        System.arraycopy(lenBuf, 0, buffer, buffer_offset, 4);
        buffer_offset += 4;

        System.arraycopy(value, 0, buffer, buffer_offset, value.length);
        buffer_offset += value.length;

        index.write(topic, offset, size_to_write);
    }

    //read offset start and end
    //TODO: what to do when read crosses circular log threshold point?
    //TODO: test and make sure everything is working
    public Iterator<DataEntry> read(long start, long end) {

        List<DataEntry> result = new ArrayList<DataEntry>();
        long offset = index.read(start);

        Log.d("DataStream", "Beginning read at offset " + offset);

        if (offset < index.getThreshold() && end > index.getThreshold()) {
            end = index.getThreshold() - 1;
        }

        while (offset < end) {

            byte [] topicbuf = logRead(offset, 4);
            byte [] lenbuf = logRead(offset + 4, 4);

            int topic = ByteBuffer.wrap(topicbuf).getInt();
            int length = ByteBuffer.wrap(lenbuf).getInt();

            byte [] value = logRead(offset + 8, length);

            result.add(new DataEntry(topic, value));

            offset = offset + 4 + 4 + length;
        }

        return result.iterator();
    }

    public void close() {
        flushBuffer();
        index.close();
        try {
            logRAF.close();
            logOpen = false;
        } catch (Exception e) {
        }
    }

    public void clear() {
        index.clear();

        Arrays.fill(buffer, (byte) 0);
        buffer_offset = 0;

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
            logRAF = new RandomAccessFile(logfile, "rwd");
            logOpen = true;
        } catch (Exception e) {}

        log_offset = 0;
    }

    //Flush in-memory buffer
    //This "saves state"
    private void flushBuffer() {
        index.flushIndexBuffer();

        try {
            if (!logOpen) {
                Log.i("SensorStore", "loading RAF");
                logRAF = new RandomAccessFile(logfile, "rwd");
                logOpen = true;
            }
            if (logRAF.getFilePointer() != log_offset) {
                Log.i("SensorStore", "Seeking!!");
                logRAF.seek(log_offset);
            }
            logRAF.write(buffer, 0, buffer_offset);
        } catch (Exception e) {
            Log.e("DataStream", e.getLocalizedMessage());
        }

        log_offset = log_offset + buffer_offset;

        if (log_offset > logsize) {
            log_offset = 0;
            index.swapRuns();
        }

        buffer_offset = 0;

    }

    //Reads from file "file" at offset "offset" for "length" bytes
    //Returns a byte array containing the contents of the file at that position
    //Returns null on failure
    @Nullable
    private byte[] logRead(long offset, int length) {
        try {
            if (!logOpen) {
                logRAF = new RandomAccessFile(logfile, "rwd");
                logOpen = true;
            }
            byte[] buf = new byte[length];
            logRAF.seek(offset);
            logRAF.read(buf);
            return buf;
        } catch (Exception e) {
            return null;
        }
    }
}
