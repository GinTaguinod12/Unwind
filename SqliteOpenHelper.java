package com.taguinod.database;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteOpenHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String db_name = "MyDatabase.db";
    private static final int db_ver = 1;

    // Table Names
    private static final String TABLE_USERS = "Users";
    private static final String TABLE_QUOTES = "Quotes";
    private static final String TABLE_MOODS = "Moods";

    public SqliteOpenHelper(Context context) {
        super(context, db_name, null, db_ver);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query1 = "CREATE TABLE " + TABLE_USERS + " (" +
                "UserID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name TEXT NOT NULL)";

        String query2 = "CREATE TABLE " + TABLE_QUOTES + " (" +
                "QuoteID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "QuoteText TEXT NOT NULL, " +
                "QuoteAuthor TEXT)";

        String query3 = "CREATE TABLE " + TABLE_MOODS + " (" +
                "MoodID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MoodName TEXT NOT NULL)";

        db.execSQL(query1);
        db.execSQL(query2);
        db.execSQL(query3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOODS);
        onCreate(db);
    }

    //  INSERT USER
    public void insertUser(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Name", name);

        db.insert(TABLE_USERS, null, values);
    }

    //  INSERT QUOTE
    public void insertQuote(String quoteText, String quoteAuthor) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("QuoteText", quoteText);
        values.put("QuoteAuthor", quoteAuthor);

        db.insert(TABLE_QUOTES, null, values);
    }

    // INSERT MOOD
    public void insertMood(String moodName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("MoodName", moodName);

        db.insert(TABLE_MOODS, null, values);
    }

    //  CHECK IF TABLE IS EMPTY
    public boolean isTableEmpty(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + tableName, null);

        boolean isEmpty = true;
        if (cursor.moveToFirst()) {
            isEmpty = cursor.getInt(0) == 0;
        }

        cursor.close();
        return isEmpty;
    }
}

