package com.example.functiontesterfpv3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class dbHelper extends SQLiteOpenHelper {
    private static final String db_name = "UnwindApp.db";
    private static final int db_ver = 2;
    public dbHelper(@Nullable Context context) {
        super(context, db_name, null, db_ver);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query1 = "CREATE TABLE Moods (" +
                "MoodId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MoodName TEXT UNIQUE NOT NULL)";

        String query2 = "CREATE TABLE Users (" +
                "UserId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name TEXT NOT NULL, " +
                "Password TEXT NOT NULL, " +
                "MoodId INTEGER, " +
                "UNIQUE (Name, Password))";

        String query3 = "CREATE TABLE Quotes (" +
                "QuoteId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "QuoteText TEXT NOT NULL, " +
                "QuoteAuthor TEXT, " +
                "MoodId INTEGER NOT NULL, " +
                "FOREIGN KEY (MoodId) REFERENCES Moods(MoodId))";

        String query4 = "CREATE TABLE Journals (" +
                "JournalId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Date TEXT NOT NULL, " +
                "PositiveEvent TEXT, " +
                "Challenge TEXT, " +
                "MoodInfluence TEXT, " +
                "Lesson TEXT, " +
                "UserId INTEGER NOT NULL, " +
                "FOREIGN KEY (UserId) REFERENCES Users(UserId))";

        db.execSQL(query1);
        db.execSQL(query2);
        db.execSQL(query3);
        db.execSQL(query4);
        insertInitialData(db);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Journals");
        db.execSQL("DROP TABLE IF EXISTS Quotes");
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Moods");
        onCreate(db);
    }
    public boolean userExists(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM Users WHERE Name = ? AND Password = ?",
                new String[]{name, password}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
    public void registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("Name", user.getName());
        values.put("Password", user.getPassword());

        long result = db.insert("Users", null, values);
        db.close();
    }
    public int getUserId(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;

        Cursor cursor = db.rawQuery(
                "SELECT UserId FROM Users WHERE Name = ? AND Password = ?",
                new String[]{name, password}
        );

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();
        return userId;
    }
    public Cursor getUserById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT Name, MoodId FROM Users WHERE UserId = ?",
                new String[]{String.valueOf(userId)}
        );
    }
    public void updateUserMood(long userId, int moodId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("MoodId", moodId);

        db.update(
                "Users",
                values,
                "UserId = ?",
                new String[]{String.valueOf(userId)}
        );

        db.close();
    }
    private void insertQuote(SQLiteDatabase db, String text, String author, int moodId) {
        ContentValues values = new ContentValues();
        values.put("QuoteText", text);
        values.put("QuoteAuthor", author);
        values.put("MoodId", moodId);
        db.insert("Quotes", null, values);
    }
    private void insertInitialData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO Moods (MoodName) VALUES " +
                "('Angry'), ('Anxious'), ('Sad'), ('Tired'), ('Calm'), ('Happy')");
        // Angry Quotes
        insertQuote(db, "For every minute you remain angry, you give up sixty seconds of peace of mind.", "Ralph Waldo Emerson", 1);
        insertQuote(db, "Holding onto anger is like drinking poison and expecting the other person to die.", "Buddha", 1);
        // Anxious Quotes
        insertQuote(db, "Anxiety does not empty tomorrow of its sorrows, but only empties today of its strength.", "Charles Spurgeon", 2);
        insertQuote(db, "Worry often gives a small thing a big shadow.", "Swedish Proverb", 2);
        // Sad Quotes
        insertQuote(db, "Tears come from the heart and not from the brain.", "Leonardo da Vinci", 3);
        insertQuote(db, "The word ‘happiness’ would lose its meaning if it were not balanced by sadness.", "Carl Jung", 3);
        // Tired Quotes
        insertQuote(db, "Fatigue is the best pillow.", "Benjamin Franklin", 4);
        insertQuote(db, "Sometimes the most productive thing you can do is relax.", "Mark Black", 4);
        // Calm Quotes
        insertQuote(db, "Peace comes from within. Do not seek it without.", "Buddha", 5);
        insertQuote(db, "The nearer a man comes to a calm mind, the closer he is to strength.", "Marcus Aurelius", 5);
        // Happy Quotes
        insertQuote(db, "Happiness depends upon ourselves.", "Aristotle", 6);
        insertQuote(db, "The purpose of our lives is to be happy.", "Dalai Lama", 6);
    }
    public Quote getRandomQuoteByMood(int moodId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT QuoteText, QuoteAuthor FROM Quotes " +
                        "WHERE MoodId = ? ORDER BY RANDOM() LIMIT 1",
                new String[]{String.valueOf(moodId)} // removed + 1
        );
        Quote q = new Quote("Quote..", "Unknown");

        if (cursor.moveToFirst()) {
            q = new Quote(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow("QuoteText")),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow("QuoteAuthor"))
            );
        }
        cursor.close();
        db.close();
        return q;
    }
    public void addJournal(Journal journal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("Date", journal.getDate());
        values.put("PositiveEvent", journal.getPositiveEvent());
        values.put("Challenge", journal.getChallenges());
        values.put("MoodInfluence", journal.getMoodInfluence());
        values.put("Lesson", journal.getLesson());
        values.put("UserId", journal.getUserId());

        db.insert("Journals", null, values);
    }
    public ArrayList<Journal> getAllJournals(long userId) {
        ArrayList<Journal> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Journals WHERE UserId = ?",
                new String[]{String.valueOf(userId)}
        );
        if (cursor.moveToFirst()) {
            do {
                list.add(new Journal(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    public void deleteJournal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("Journals", "JournalId = ?", new String[]{String.valueOf(id)});
    }
}
