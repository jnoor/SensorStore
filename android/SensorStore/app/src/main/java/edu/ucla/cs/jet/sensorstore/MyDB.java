package edu.ucla.cs.jet.sensorstore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayDeque;

/**
 * Created by jnoor on 11/28/16.
 */

public class MyDB{

    private MyDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String _TABLE="BasicTable"; // name of table

    public final static String _ID="_id"; // id value
    public final static String _TOPIC="topic"; // topic value
    public final static String _VALUE="value"; // value

    private ArrayDeque<DBEntry> buffer;
    private final int buffer_size;
    private int buffer_offset = 0;
    private long total_bytes_written = 0;
    /**
     *
     * @param context
     */
    public MyDB(Context context, String DB_NAME, int buf_size){
        buffer = new ArrayDeque<>();
        dbHelper = new MyDatabaseHelper(context, DB_NAME);
        database = dbHelper.getWritableDatabase();
        buffer_size = buf_size;
    }

    private class DBEntry {
        long offset;
        int topic;
        String value;
    }

    private void flush() {
        database.beginTransaction();
        while (!buffer.isEmpty()) {
            DBEntry entry = buffer.pop();
            ContentValues values = new ContentValues();
            values.put(_ID, entry.offset);
            values.put(_TOPIC, entry.topic);
            values.put(_VALUE, entry.value);
            database.insert(_TABLE, null, values);
        }
        //cleanup
        database.delete(_TABLE, "_id < ?", new String[]{String.valueOf(total_bytes_written - 1024*1024)});
        database.setTransactionSuccessful();
        database.endTransaction();
        total_bytes_written += buffer_offset;
        buffer_offset = 0;
    }


    public void addRecord(int topic, String value) {
        int size = 4 + 4 + value.length();
        if (size + buffer_offset > buffer_size) {
            flush();
        }
        DBEntry entry = new DBEntry();
        entry.offset = total_bytes_written + buffer_offset;
        entry.topic = topic;
        entry.value = value;
        buffer.add(entry);
        buffer_offset += size;
    }

    public void beginTransaction() {
        database.beginTransaction();
    }

    public void addRecordOptimal(int topic, String value) {
        ContentValues values = new ContentValues();
        values.put(_ID, total_bytes_written);
        values.put(_TOPIC, topic);
        values.put(_VALUE, value);
        database.insert(_TABLE, null, values);
        total_bytes_written += 4 + 4 + value.length();
    }

    public void commitTransaction() {
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public long getBytesWritten() {
        return total_bytes_written;
    }

    public void close() {
        flush();
        database.close();
    }

    public void clear() {
        database.delete(_TABLE, null, null);
        buffer.clear();
        buffer_offset = 0;
        total_bytes_written = 0;
    }

    public Cursor selectRecords() {
        String[] cols = new String[] {_ID, _TOPIC, _VALUE};
        Cursor mCursor = database.query(true, _TABLE,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}