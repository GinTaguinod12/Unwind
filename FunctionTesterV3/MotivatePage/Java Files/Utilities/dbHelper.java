package com.example.mixedfeaturesdraft.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.mixedfeaturesdraft.Models.Journal;
import com.example.mixedfeaturesdraft.Models.Quote;
import com.example.mixedfeaturesdraft.Models.Task;
import com.example.mixedfeaturesdraft.Models.User;

import java.util.ArrayList;
import java.util.List;

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

        String query5 = "CREATE TABLE Tasks(" +
                "TaskId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TaskName TEXT NOT NULL, " +
                "Status INTEGER, " +
                "UserId INTEGER NOT NULL, " +
                "FOREIGN KEY (UserId) REFERENCES Users(UserId))";

        db.execSQL(query1);
        db.execSQL(query2);
        db.execSQL(query3);
        db.execSQL(query4);
        db.execSQL(query5);
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Tasks");
        db.execSQL("DROP TABLE IF EXISTS Journals");
        db.execSQL("DROP TABLE IF EXISTS Quotes");
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Moods");
        onCreate(db);
    }

    // User Requirements DB Functions
    public boolean userExists(User user) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM Users WHERE Name = ? AND Password = ?",
                new String[]{user.getName(), user.getPassword()}
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

        db.insert("Users", null, values);
        db.close();
    }
    public long getUserId(User user) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;

        Cursor cursor = db.rawQuery(
                "SELECT UserId FROM Users WHERE Name = ? AND Password = ?",
                new String[]{user.getName(), user.getPassword()}
        );

        if (cursor.moveToFirst()) {
            userId = cursor.getLong(0);
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

    // Mood-Quote Data Initializer & Randomizer
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
        insertQuote(db, "Anger is never without a reason, but seldom with a good one.", "Benjamin Franklin", 1);
        insertQuote(db, "Anger begins with folly and ends with regret.", "Pythagoras", 1);
        insertQuote(db, "He who angers you conquers you.", "Elizabeth Kenny", 1);
        // Anxious Quotes
        insertQuote(db, "Anxiety does not empty tomorrow of its sorrows, but only empties today of its strength.", "Charles Spurgeon", 2);
        insertQuote(db, "Worry often gives a small thing a big shadow.", "Swedish Proverb", 2);
        insertQuote(db, "Anxiety does not empty tomorrow of its sorrows.", "Corrie ten Boom", 2);
        insertQuote(db, "Worry is interest paid on trouble before it is due.", "William R. Inge", 2);
        insertQuote(db, "Nothing diminishes anxiety faster than action.", "Walter Anderson", 2);
        // Sad Quotes
        insertQuote(db, "Tears come from the heart and not from the brain.", "Leonardo da Vinci", 3);
        insertQuote(db, "The word ‘happiness’ would lose its meaning if it were not balanced by sadness.", "Carl Jung", 3);
        insertQuote(db, "Tears are words the heart can’t say.", "Gerard Way", 3);
        insertQuote(db, "Sadness flies away on the wings of time.", "Jean de La Fontaine", 3);
        insertQuote(db, "Every life has a measure of sorrow.", "Charles Dickens", 3);
        // Tired Quotes
        insertQuote(db, "Fatigue is the best pillow.", "Benjamin Franklin", 4);
        insertQuote(db, "Sometimes the most productive thing you can do is relax.", "Mark Black", 4);
        insertQuote(db, "Fatigue makes cowards of us all.", "Vince Lombardi", 4);
        insertQuote(db, "Rest when you are weary.", "Norman Vincent Peale", 4);
        insertQuote(db, "Sometimes rest is the most productive choice.", "Mark Black", 4);
        // Calm Quotes
        insertQuote(db, "Peace comes from within. Do not seek it without.", "Buddha", 5);
        insertQuote(db, "The nearer a man comes to a calm mind, the closer he is to strength.", "Marcus Aurelius", 5);
        insertQuote(db, "Calmness is the cradle of power.", "Josiah Gilbert Holland", 5);
        insertQuote(db, "A calm mind brings inner strength.", "Dalai Lama", 5);
        insertQuote(db, "Silence is a source of great strength.", "Lao Tzu", 5);
        // Happy Quotes
        insertQuote(db, "Happiness depends upon ourselves.", "Aristotle", 6);
        insertQuote(db, "The purpose of our lives is to be happy.", "Dalai Lama", 6);
        insertQuote(db, "Happiness is a choice.", "Jim Rohn", 6);
        insertQuote(db, "Joy is the simplest form of gratitude.", "Karl Barth", 6);
        insertQuote(db, "The power of finding beauty in the humblest things makes home happy and life lovely.", "Louisa May Alcott", 6);
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

    // Journal Page DB Functions
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
                new String[]{String.valueOf(userId)});
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

    public Journal getJournalById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM Journals WHERE JournalId = ?",
                new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Journal journal = new Journal(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getLong(6)
            );
            cursor.close();
            return journal;
        }
        cursor.close();
        return null;
    }

    // Motivation Page DB Functions
    public void insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("TaskName", task.getTask());
        values.put("Status", 0);
        values.put("UserId", task.getUserId());

        db.insert("Tasks", null, values);
        db.close();
    }
    public void updateTask(int taskId, String task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TaskName", task);

        db.update("Tasks", values, "TaskId = ?",
                new String[]{String.valueOf(taskId)});
    }
    public void updateStatus(int taskId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Status", status);

        db.update("Tasks", values, "TaskId = ?",
                new String[]{String.valueOf(taskId)});
    }
    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("Tasks", "TaskId = ?",
                new String[]{String.valueOf(taskId)});
    }
    public List<Task> getAllTasks(long userId) {
        ArrayList<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Tasks WHERE UserId = ?",
                new String[]{String.valueOf(userId)}
        );
        if (cursor.moveToFirst()) {
            do {
                list.add(new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
