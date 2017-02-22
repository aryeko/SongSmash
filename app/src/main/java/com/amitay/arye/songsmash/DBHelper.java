package com.amitay.arye.songsmash;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/**
 * Created by A&A on 2/21/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DbConstants.Songs.TABLE_NAME + "(" +
                    DbConstants.Songs._ID +            " INTEGER PRIMARY KEY," +
                    DbConstants.Songs.SONG_NAME +      " TEXT," +
                    DbConstants.Songs.LIKED +          " TEXT" +
                    ");";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DbConstants.Songs.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SongSmash.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
