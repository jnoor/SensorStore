package edu.ucla.cs.jet.sensorstore;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static edu.ucla.cs.jet.sensorstore.DataStream.pagesize;

/**
 * Created by jnoor on 11/16/16.
 */

public class IndexStream {

    private File last_run_index_file;
    private File current_run_index_file;

    private File index_threshold_file;
    private long index_threshold;

    private File index_offset_file;
    private long index_offset;

    private byte [] buffer;
    private int buffer_offset;

    //TODO: Read from the index file (binary search, both index files)

    public IndexStream() {
        buffer = new byte[(int) (pagesize * 1.5 + 1)];
        buffer_offset = 0;

        File sdCard = Environment.getExternalStorageDirectory();
        sdCard.mkdirs();
        File directory = new File(sdCard.getAbsolutePath(), "SensorStore");
        directory.mkdirs();

        last_run_index_file = new File(directory, "indexLastRun");
        current_run_index_file = new File(directory, "indexCurrentRun");
        index_offset_file = new File(directory, "indexOffset");
        index_threshold_file = new File(directory, "indexThreshold");

        loadIndexOffset();
        loadIndexThreshold();
    }

    //write the index entry to the buffer
    //written as: (topic)(logoffset)
    //returns: offset => offset into the index file
    public long write(int topic, long logoffset, int sizewritten) {
        long offset = index_offset + buffer_offset;

        System.arraycopy(ByteBuffer.allocate(4).putInt(topic).array(), 0, buffer, buffer_offset, 4);
        buffer_offset += 4;

        System.arraycopy(ByteBuffer.allocate(8).putLong(logoffset).array(), 0, buffer, buffer_offset, 4);
        buffer_offset += 8;

        index_threshold = logoffset + sizewritten;

        return offset;
    }

    public void close() {
        flushIndexBuffer();
    }

    //This swaps the current index run for the last index run
    public void swapRuns() {
        flushIndexBuffer();

        File sdCard = Environment.getExternalStorageDirectory();
        sdCard.mkdirs();
        File directory = new File(sdCard.getAbsolutePath(), "SensorStore");
        directory.mkdirs();

        new File(directory, "indexCurrentRun").renameTo(new File(directory, "indexLastRun"));
        index_threshold = 0;
        index_offset = 0;
        writeIndexOffset();
        writeIndexThreshold();
        //TODO: test index swapping runs
    }

    //Flush in-memory buffer
    //This "saves state"
    public void flushIndexBuffer() {
        if (buffer_offset == 0) {
            return;
        }
        Log.d("IndexStream", "Flushing buffer");

        int buffersize = buffer_offset;

        writeBufferWithLengthToPositionInFile(buffer, buffersize, index_offset, current_run_index_file);

        //reset in-memory buffer
        Arrays.fill(buffer, (byte) 0);
        buffer_offset = 0;

        //update and save index offset
        index_offset = index_offset + buffersize;
        writeIndexOffset();
        writeIndexThreshold();
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
            Log.e("IndexStream", e.getLocalizedMessage());
        }
    }

    private void loadIndexThreshold() {
        String val = readStringFromFile(index_threshold_file);
        try {
            index_threshold = Long.valueOf(val);
        } catch (Exception e) {
            index_threshold = 0;
        }
    }

    private void writeIndexThreshold() {
        writeStringToFile(String.valueOf(index_threshold), index_threshold_file);
    }

    //Loads the index offset from the index_offset_file
    private void loadIndexOffset() {
        String val = readStringFromFile(index_offset_file);
        try {
            index_offset = Long.valueOf(val);
        } catch (Exception e) {
            index_offset = 0;
        }
    }

    //Saves the index offset to the index_offset_file
    private void writeIndexOffset() {
        writeStringToFile(String.valueOf(index_offset), index_offset_file);
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
