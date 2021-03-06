package edu.ucla.cs.jet.sensorstore;

import java.util.Iterator;

/**
 * Created by jnoor on 11/10/16.
 */


//IMPORTANT: >= API23: MAKE SURE PERMISSIONS ARE GRANTED TO READ/WRITE EXTERNAL STORAGE
public class SensorStore {

    DataStream ds;

    public SensorStore() {
        ds = new DataStream();
    }

    public void clear() { ds.clear(); }
    public void write(int topic, byte [] value) {
        ds.write(topic, value);
    }
    public void close() {
        ds.close();
    }
    public long offset() {
        return ds.offset();
    }
    public Iterator<DataEntry> read(long start, long end) { return ds.read(start, end);}
}
