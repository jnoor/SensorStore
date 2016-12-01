package edu.ucla.cs.jet.threadedstore;

/**
 * Created by jnoor on 11/30/16.
 */

public class DataEntry {
    public int topic;
    public byte[] value;

    public DataEntry(int topic, byte[] value) {
        this.topic = topic;
        this.value = value;
    }
}