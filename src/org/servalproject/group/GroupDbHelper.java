package org.servalproject.group;

import android.util.Log;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class GroupDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Group.db";

    private static SQLiteDatabase db;

    public GroupDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if(db == null || !db.isOpen()) {
            db = new GroupDbHelper(context).getWritableDatabase();
        }
        return db;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GroupDAO.MESSAGES_CREATE_TABLE);
        db.execSQL(GroupDAO.MEMBERS_CREATE_TABLE);
        Log.d("GroupDbHelper","create db");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GroupDAO.MESSAGES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GroupDAO.MEMBERS_TABLE_NAME);
        onCreate(db);
        Log.d("GroupDbHelper","upgrade db");
    }

}
