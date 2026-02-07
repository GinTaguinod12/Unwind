import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.mixedfeaturesdraft.Models.Task;
import com.example.mixedfeaturesdraft.Models.User;

import java.util.ArrayList;
import java.util.List;

public class dbHelper extends SQLiteOpenHelper {

    private static final String db_name = "UnwindApp.db";
    private static final int db_ver = 1;

    public dbHelper(@Nullable Context context) {
        super(context, db_name, null, db_ver);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { // ADDITIONAL QUERY FOR TASK TABLE
      
        String query5 = "CREATE TABLE Tasks(" +
                "TaskId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TaskName TEXT NOT NULL, " +
                "Status INTEGER, " +
                "UserId INTEGER NOT NULL, " +
                "FOREIGN KEY (UserId) REFERENCES Users(UserId))";

        db.execSQL(query5);
      
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { // ORDERED TABLE DROPPING
        db.execSQL("DROP TABLE IF EXISTS Tasks"); // <-- Dinagdag ko this one
        db.execSQL("DROP TABLE IF EXISTS Journals");
        db.execSQL("DROP TABLE IF EXISTS Quotes");
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Moods");
        onCreate(db);
    }

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
