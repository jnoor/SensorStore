package edu.ucla.cs.jet.sensorstore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by jnoor on 11/28/16.
 */

public class MyDB{

    private MyDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String _TABLE="BasicTable"; // name of table

    public final static String _ID="_id"; // id value
    public final static String _TOPIC="topic"; // topic value
    public final static String _X="x"; // x value
    public final static String _Y="y"; // y value
    public final static String _Z="z"; // z value
    /**
     *
     * @param context
     */
    public MyDB(Context context){
        dbHelper = new MyDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }


    public long createRecords(int topic, int x, int y, int z) {
        ContentValues values = new ContentValues();
//        values.put(_ID, id);
        values.put(_TOPIC, topic);
        values.put(_X, x);
        values.put(_Y, y);
        values.put(_Z, z);
        return database.insert(_TABLE, null, values);
    }

    public Cursor selectRecords() {
        String[] cols = new String[] {_ID, _TOPIC, _X, _Y, _Z};
        Cursor mCursor = database.query(true, _TABLE,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}