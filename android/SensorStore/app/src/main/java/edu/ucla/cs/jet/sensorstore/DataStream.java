package edu.ucla.cs.jet.sensorstore;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
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
    static final int pagesize = 512 * 1024;
    static final long logsize = 512 * 1024 * 1024;

    private File logfile;
    private File log_offset_file;
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
        log_offset_file = new File(directory, "offset");

        loadOffset();
    }

    public long offset() {
        return log_offset + buffer_offset;
    }

    //write the value to the buffer
    //written as: (topic)(valuesize)(value)
    //returns: offset => offset into the log file
    public long write(int topic, byte [] value) {

        int size_to_write =  4 + 4 + value.length;
        long offset = log_offset + buffer_offset;

        if (size_to_write > pagesize) {
            throw new Error("Trying to write value larger than the pagesize!");
        }
        if (buffer_offset + size_to_write >= pagesize) {
            flushBuffer();
        }

        System.arraycopy(ByteBuffer.allocate(4).putInt(topic).array(), 0, buffer, buffer_offset, 4);
        buffer_offset += 4;

        System.arraycopy(ByteBuffer.allocate(4).putInt(value.length).array(), 0, buffer, buffer_offset, 4);
        buffer_offset += 4;

        System.arraycopy(value, 0, buffer, buffer_offset, value.length);
        buffer_offset += value.length;

        index.write(topic, offset, 4 + 4 + value.length);

        return offset;
    }

    //read offset start and end
    //TODO: what to do when read crosses circular log threshold point?
    public Iterator<DataEntry> read(long start, long end) {

        List<DataEntry> result = new ArrayList<DataEntry>();
        long offset = index.read(start);

        Log.d("DataStream", "Beginning read at offset " + offset);

        if (offset < index.getThreshold() && end > index.getThreshold()) {
            end = index.getThreshold() - 1;
        }

        while (offset < end) {

            byte [] topicbuf = readFromFileAtPositionOfSize(logfile, offset, 4);
            byte [] lenbuf = readFromFileAtPositionOfSize(logfile, offset + 4, 4);

            int topic = ByteBuffer.wrap(topicbuf).getInt();
            int length = ByteBuffer.wrap(lenbuf).getInt();

            byte [] value = readFromFileAtPositionOfSize(logfile, offset + 8, length);

            result.add(new DataEntry(topic, value));

            offset = offset + 4 + 4 + length;
        }

        return result.iterator();
    }

    public void close() {
        flushBuffer();
        index.close();
    }

    public void clear() {
        index.clear();

        Arrays.fill(buffer, (byte) 0);
        buffer_offset = 0;

        //empty out log file
        try {
            FileOutputStream los = new FileOutputStream(logfile, false);
            los.write(new byte[0]);
            los.close();
        } catch (Exception e) {

        }

        log_offset = 0;
        writeLogOffset();
    }

    //Flush in-memory buffer
    //This "saves state"
    private void flushBuffer() {
        Log.d("DataStream", "Flushing buffer");

        index.flushIndexBuffer();

        int buffersize = buffer_offset;

        writeBufferWithLengthToPositionInFile(buffer, buffersize, log_offset, logfile);

        //reset in-memory buffer
        Arrays.fill(buffer, (byte) 0);
        buffer_offset = 0;

        //update and save log offset
        log_offset = log_offset + buffersize;
        if (log_offset > logsize) {
            log_offset = 0;
            index.swapRuns();
        }
        writeLogOffset();
    }

    //Reads from file "file" at offset "offset" for "length" bytes
    //Returns a byte array containing the contents of the file at that position
    //Returns null on failure
    @Nullable
    private byte[] readFromFileAtPositionOfSize(File file, long offset, int length) {
        try {
            RandomAccessFile f = new RandomAccessFile(file, "r");
            byte[] buf = new byte[length];
            f.seek(offset);
            f.read(buf);
            f.close();
            return buf;
        } catch (Exception e) {
            return null;
        }
    }

    //Writes "length" bytes of buffer "buf" to file "file" at offset "offset"
    private void writeBufferWithLengthToPositionInFile(byte [] buf, int length, long offset, File file) {
        try {
            RandomAccessFile f = new RandomAccessFile(file, "rw");
            f.seek(offset);
            f.write(buf, 0, length);
            f.close();
        } catch (Exception e) {
            Log.e("DataStream", e.getLocalizedMessage());
        }
    }

    //Loads the log offset from the log_offset_file
    private void loadOffset() {
        String val = readStringFromFile(log_offset_file);
        try {
            log_offset = Long.valueOf(val);
        } catch (Exception e) {
            log_offset = 0;
        }
    }

    //Saves the log offset to the log_offset_file
    private void writeLogOffset() {
        writeStringToFile(String.valueOf(log_offset), log_offset_file);
    }

    //This will write a String to the entire file
    private void writeStringToFile(String data, File file) {
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data.getBytes());
            stream.close();
        } catch (Exception e) {
            Log.e("DataStream", e.getLocalizedMessage());
        }
    }

    //This will read an entire file into a String and return it
    private String readStringFromFile(File file) {
        try {
            byte[] bytes = new byte[(int) file.length()];

            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();

            return new String(bytes);
        } catch (Exception e) {
            return "";
        }
    }
}
