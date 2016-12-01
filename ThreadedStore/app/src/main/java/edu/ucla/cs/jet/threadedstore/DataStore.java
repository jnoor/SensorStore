package edu.ucla.cs.jet.threadedstore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jnoor on 11/30/16.
 */

public class DataStore {

    DataFlusher flusher;
    BlockingQueue<DataEntry> flushqueue;
    DataBuffer [] buffers;
    BlockingQueue<DataEntry> [] queues;

    static final int num_threads = 2;
    
    public DataStore() {
        flushqueue = new LinkedBlockingQueue<DataEntry>();
        flusher = new DataFlusher(flushqueue);
        flusher.start();

        queues = new LinkedBlockingQueue[num_threads];
        buffers = new DataBuffer[num_threads];
        for (int i=0; i<num_threads; i++) {
            queues[i] = new LinkedBlockingQueue<DataEntry>();
            buffers[i] = new DataBuffer(flushqueue, queues[i]);
            buffers[i].start();
        }
    }

    public long offset() {
        return flusher.offset();
    }

    int cnt = 0;
    public void write(int topic, byte [] value) {
        queues[cnt++ % num_threads].add(new DataEntry(topic, value));
    }

    public void close() {
        for (DataBuffer buf : buffers) {
            buf.close();
        }
    }

    public void clear() {
        for (DataBuffer buf : buffers) {
            buf.clear();
        }
        flusher.clear();
    }
}
