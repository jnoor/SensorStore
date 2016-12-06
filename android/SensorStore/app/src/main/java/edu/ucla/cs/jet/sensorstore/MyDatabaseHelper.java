package edu.ucla.cs.jet.sensorstore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jnoor on 11/28/16.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DBName";

    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table BasicTable (" +
            "_id integer not null, " +
            "topic integer not null," +
            "value varchar(255) not null," +
            "primary key (_id, topic)" +
            ");";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion){
        Log.w(MyDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS MyEmployees");
        onCreate(database);
    }
}