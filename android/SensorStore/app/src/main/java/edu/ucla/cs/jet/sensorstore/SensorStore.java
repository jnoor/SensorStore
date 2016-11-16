package edu.ucla.cs.jet.sensorstore;

/**
 * Created by jnoor on 11/10/16.
 */


//IMPORTANT: >= API23: MAKE SURE PERMISSIONS ARE GRANTED TO READ/WRITE EXTERNAL STORAGE
public class SensorStore {

    DataStream ds;

    public SensorStore() {
        ds = new DataStream();
    }

    public long write(int topic, String value) {
        return ds.write(topic, value.getBytes());
    }
    public void close() {
        ds.close();
    }
    public long offset() {
        return ds.offset();
    }
    public String readAll() {
        return ds.readall();
    }
    public byte[] read(long start, long end) { return ds.read(start, end);}
}
