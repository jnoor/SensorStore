package edu.ucla.cs.jet.sensorstore;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static edu.ucla.cs.jet.sensorstore.DataStream.pagesize;

/**
 * Created by jnoor on 11/16/16.
 */

//TODO: Only one RandomAccessFile per file, try "rwd" or "rws" and see if that allows simulataneous
//TODO: read/write. Otherwise, just close and reopen the file on every read. I know it sucks.

public class IndexStream {

    private File last_run_index_file;
    private File current_run_index_file;

    private RandomAccessFile currentRAF;
    private boolean curIndexOpen;

    private long index_threshold;

    private long index_offset;

    private byte [] buffer;
    private int buffer_offset;

    public IndexStream() {
        buffer = new byte[(int) (pagesize * 1.5 + 1)];
        buffer_offset = 0;

        File sdCard = Environment.getExternalStorageDirectory();
        sdCard.mkdirs();
        File directory = new File(sdCard.getAbsolutePath(), "SensorStore");
        directory.mkdirs();

        last_run_index_file = new File(directory, "indexLastRun");
        current_run_index_file = new File(directory, "indexCurrentRun");

        curIndexOpen = false;
        try {
            currentRAF = new RandomAccessFile(current_run_index_file, "rw");
            curIndexOpen = true;
        } catch (Exception e) {}

        //TODO: load index_offset, log_offset, index_threshold (= log_offset + last_data_length)
    }

    //write the index entry to the buffer
    //written as: (topic)(logoffset)
    //returns: offset => offset into the index file
    public void write(int topic, long logoffset, int sizewritten) {

        byte[] topicBuf = new byte[] {
                (byte)(topic >>> 24),
                (byte)(topic >>> 16),
                (byte)(topic >>> 8),
                (byte)(topic)
        };
//        byte [] topicBuf = ByteBuffer.allocate(4).putInt(topic).array();
        System.arraycopy(topicBuf, 0, buffer, buffer_offset, 4);
        buffer_offset += 4;

        byte[] offsetBuf = new byte[] {
                (byte)(logoffset >>> 56),
                (byte)(logoffset >>> 48),
                (byte)(logoffset >>> 40),
                (byte)(logoffset >>> 32),
                (byte)(logoffset >>> 24),
                (byte)(logoffset >>> 16),
                (byte)(logoffset >>> 8),
                (byte)(logoffset)
        };
//        byte[] offsetBuf = ByteBuffer.allocate(8).putLong(logoffset).array();
        System.arraycopy(offsetBuf, 0, buffer, buffer_offset, 8);
        buffer_offset += 8;

        index_threshold = logoffset + sizewritten;
    }

    public long getThreshold() {
        return index_threshold;
    }

    //returns the data entry offset that begins at the soonest position from logoffset
    public long read(long offset) {

        if (offset < index_threshold) {
            //index entry is in current run

            byte [] inmemoryoffset = new byte[8];
            System.arraycopy(buffer, 4, inmemoryoffset, 0, 8);
            long inmemorythreshold = ByteBuffer.wrap(inmemoryoffset).getLong();

            if (offset > inmemorythreshold) {
                //index entry begins in-memory

                for(int i = 0; i < buffer_offset; i += 12) {
                    byte [] topicbytes = new byte[4];
                    byte [] offsetbytes = new byte[8];

                    System.arraycopy(buffer, i, topicbytes, 0, 4);
                    System.arraycopy(buffer, i+4, offsetbytes, 0, 8);

                    int topic = ByteBuffer.wrap(topicbytes).getInt();
                    long offset_t = ByteBuffer.wrap(offsetbytes).getLong();

                    if (offset_t > offset) {
                        return offset_t;
                    }
                }

            } else {
                //index entry is in log-file for current run
                //binary search for start position
                return binarySearch(current_run_index_file, offset, 0, current_run_index_file.length());
            }

        } else {
            //index entry in in old run
            //binary search for start position
            return binarySearch(last_run_index_file, offset, 0, last_run_index_file.length());
        }
        return -1;
    }

    public void close() {
        flushIndexBuffer();
        if (curIndexOpen) {
            try {
                currentRAF.close();
                curIndexOpen = false;
            } catch (Exception e) {}
        }
    }

    public void clear() {
        //reset in-memory buffer
        Arrays.fill(buffer, (byte) 0);
        buffer_offset = 0;

        close();

        //empty out files
        try {
            FileOutputStream cos = new FileOutputStream(current_run_index_file, false);
            cos.write(new byte[0]);
            cos.close();

            FileOutputStream los = new FileOutputStream(last_run_index_file, false);
            los.write(new byte[0]);
            los.close();
        } catch (Exception e) {

        }

        try {
            currentRAF = new RandomAccessFile(current_run_index_file, "rw");
            curIndexOpen = true;
        } catch (Exception e) {}

        //reset pointers
        index_threshold = 0;
        index_offset = 0;

    }

    //This swaps the current index run for the last index run
    public void swapRuns() {
        close();

        File sdCard = Environment.getExternalStorageDirectory();
        sdCard.mkdirs();
        File directory = new File(sdCard.getAbsolutePath(), "SensorStore");
        directory.mkdirs();

        new File(directory, "indexCurrentRun").renameTo(new File(directory, "indexLastRun"));

        try {
            currentRAF = new RandomAccessFile(current_run_index_file, "rw");
            curIndexOpen = true;
        } catch (Exception e) {}

        index_threshold = 0;
        index_offset = 0;
        //TODO: test index swapping runs
    }

    //Flush in-memory buffer
    //This "saves state"
    public void flushIndexBuffer() {
        try {
            if (!curIndexOpen) {
                currentRAF = new RandomAccessFile(current_run_index_file, "rw");
                curIndexOpen = true;
            }
            if (currentRAF.getFilePointer() != index_offset) {
                currentRAF.seek(index_offset);
            }
            currentRAF.write(buffer, 0, buffer_offset);
        } catch (Exception e) {
            Log.e("IndexStream", e.getLocalizedMessage());
        }

        index_offset = index_offset + buffer_offset;
        buffer_offset = 0;
    }

    //returns the log offset of the first index entry beginning at offset
    //performs binary search lookup

    private long binarySearch(File file, long offset, long start, long end) {
        if (end - start < 12) {
            //empty file
            return -1;
        }
        if (end - start < 36) {
            // 1-2 entries
            long realstart = (start / 12) * 12;
            byte [] firstoffsetbytes = readFromFileAtPositionOfSize(file, realstart + 4, 8);
            long firstoffset = ByteBuffer.wrap(firstoffsetbytes).getLong();

            if (firstoffset >= offset) {
                return firstoffset;
            } else {
                byte [] secondoffsetbytes = readFromFileAtPositionOfSize(file, realstart + 12 + 4, 8);
                return ByteBuffer.wrap(secondoffsetbytes).getLong();
            }
        }

        long midpoint = (end - start) / 2;
        long readpoint = ((long) (midpoint / 12)) * 12;
        byte [] offsetbytes = readFromFileAtPositionOfSize(file, readpoint + 4, 8);
        long offset_test = ByteBuffer.wrap(offsetbytes).getLong();

        if (offset == offset_test) {
            return offset;
        } else if (offset > offset_test) {
            return binarySearch(file, offset, readpoint + 12, end);
        } else {
            return binarySearch(file, offset, start, readpoint + 12);
        }
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
}
