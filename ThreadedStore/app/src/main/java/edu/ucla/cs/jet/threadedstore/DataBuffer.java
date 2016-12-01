package edu.ucla.cs.jet.threadedstore;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

/**
 * Created by jnoor on 11/30/16.
 */

public class DataBuffer extends Thread {

    static final int pagesize = 512 * 1024;

    private byte [] buffer;
    private int buffer_offset;

    private BlockingQueue<DataEntry> flusher;
    private BlockingQueue<DataEntry> queue;

    public DataBuffer(BlockingQueue<DataEntry> flushqueue, BlockingQueue<DataEntry> q) {
        buffer = new byte[pagesize];
        buffer_offset = 0;
        this.flusher = flushqueue;
        this.queue = q;
        if (queue == null) {
            Log.d("WTF", "Queue is null?!?!?!?");
        }
    }

    public void run() {
        while(true) {
            try {
                if (queue == null) {
                    Log.d("ISSUE", "Queue is null!!!");
                } else {
                    DataEntry data = queue.take();
                    write(data.topic, data.value);
                }
            } catch (Exception e) {
                Log.d("ERROR", e.getLocalizedMessage());
            }
        }
    }

    //write the value to the buffer
    //written as: (topic)(valuesize)(value)
    //returns: offset => offset into the log file
    public void write(int topic, byte [] value) {

        if (buffer_offset + 4 + 4 + value.length >= pagesize) {
            flush();
        }

        System.arraycopy(intToByteArray(topic), 0, buffer, buffer_offset, 4);
        buffer_offset += 4;

        System.arraycopy(intToByteArray(value.length), 0, buffer, buffer_offset, 4);
        buffer_offset += 4;

        System.arraycopy(value, 0, buffer, buffer_offset, value.length);
        buffer_offset += value.length;

    }

    private byte [] intToByteArray(int value) {
        return new byte[] {
                (byte) (value),
                (byte) (value >>> 8),
                (byte) (value >>> 16),
                (byte) (value >>> 24)
        };
    }

    public void close() {
        flush();
    }

    public void clear() {
        buffer_offset = 0;
    }

    //Flush in-memory buffer
    //This "saves state"
    private void flush() {
        flusher.add(new DataEntry(buffer_offset, buffer));
        buffer = new byte[pagesize];
        buffer_offset = 0;
    }
}
