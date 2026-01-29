package com.taguinod.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class dbhelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "MyMoodDatabase.db";
    private static final int DB_VER = 1;

    private static final String TABLE_USERS = "Users";
    private static final String COLUMN_USER_ID = "UserID";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_MOOD_ID = "MoodID";

    public dbhelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create MoodTable
        String queryMood = "CREATE TABLE MoodTable (" +
                "MoodId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MoodName TEXT UNIQUE NOT NULL)";
        db.execSQL(queryMood);

        // Create Users table
        String queryUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_MOOD_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_MOOD_ID + ") REFERENCES MoodTable(MoodId))";
        db.execSQL(queryUsers);

        // Create QuoteTable
        String queryQuote = "CREATE TABLE QuoteTable (" +
                "QuoteID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "QuoteText TEXT NOT NULL, " +
                "QuoteAuthor TEXT, " +
                "MoodID INTEGER NOT NULL, " +
                "FOREIGN KEY (MoodID) REFERENCES MoodTable(MoodID))";
        db.execSQL(queryQuote);

        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS MoodTable");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS QuoteTable");
        onCreate(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        // Insert moods
        db.execSQL("INSERT INTO MoodTable (MoodName) VALUES ('Angry'), ('Anxious'), ('Sad'), ('Tired'), ('Calm'), ('Happy')");

        // Insert quotes
        db.execSQL(
                "INSERT INTO QuoteTable (QuoteText, QuoteAuthor, MoodID) VALUES " +
                        "('For every minute you remain angry, you give up sixty seconds of peace of mind.', 'Ralph Waldo Emerson', 1), " +
                        "('Holding onto anger is like drinking poison and expecting the other person to die.', 'Buddha', 1), " +
                        "('Anxiety does not empty tomorrow of its sorrows, but only empties today of its strength.', 'Charles Spurgeon', 2), " +
                        "('Worry often gives a small thing a big shadow.', 'Swedish Proverb', 2), " +
                        "('Tears come from the heart and not from the brain.', 'Leonardo da Vinci', 3), " +
                        "('The word ‘happiness’ would lose its meaning if it were not balanced by sadness.', 'Carl Jung', 3), " +
                        "('Fatigue is the best pillow.', 'Benjamin Franklin', 4), " +
                        "('Sometimes the most productive thing you can do is relax.', 'Mark Black', 4), " +
                        "('Peace comes from within. Do not seek it without.', 'Buddha', 5), " +
                        "('The nearer a man comes to a calm mind, the closer he is to strength.', 'Marcus Aurelius', 5), " +
                        "('Happiness depends upon ourselves.', 'Aristotle', 6), " +
                        "('The purpose of our lives is to be happy.', 'Dalai Lama', 6)"
        );
    }

    // Insert or update a user (prevents duplicates by name)
    public long insertOrUpdateUser(Users user) {
        SQLiteDatabase db = getWritableDatabase();

        // Check if user exists by name
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_NAME + " = ?",
                new String[]{user.getName()},
                null, null, null
        );

        long id;
        if (cursor.moveToFirst()) {
            // User exists → update
            int existingUserId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            user = new Users(existingUserId, user.getName(), user.getMoodId());
            updateUser(user);
            id = existingUserId;
        } else {
            // User does not exist → insert
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, user.getName());
            values.put(COLUMN_MOOD_ID, user.getMoodId());
            id = db.insert(TABLE_USERS, null, values);
        }

        cursor.close();
        db.close();
        return id;
    }

    // Update existing user by ID
    public int updateUser(Users user) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_MOOD_ID, user.getMoodId());

        int rowsAffected = db.update(
                TABLE_USERS,
                values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getUserId())}
        );

        db.close();
        return rowsAffected;
    }

    // Get user by ID
    public Users getUserById(long id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Users user = null;
        if (cursor.moveToFirst()) {
            user = new Users(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MOOD_ID))
            );
        }

        cursor.close();
        db.close();
        return user;
    }

    // Get a random quote by mood
    public Quotes getRandomQuoteByMood(int moodId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT QuoteText, QuoteAuthor FROM QuoteTable " +
                        "WHERE MoodID = ? ORDER BY RANDOM() LIMIT 1",
                new String[]{String.valueOf(moodId)}
        );

        Quotes quote = new Quotes("Quote..", "Unknown");
        if (cursor.moveToFirst()) {
            quote = new Quotes(
                    cursor.getString(cursor.getColumnIndexOrThrow("QuoteText")),
                    cursor.getString(cursor.getColumnIndexOrThrow("QuoteAuthor"))
            );
        }

        cursor.close();
        db.close();
        return quote;
    }
}

