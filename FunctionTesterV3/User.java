package com.example.functiontesterfpv3;

public class User {
    private int userId;
    private String name;
    private int moodId;
    private String password;

    public User(String name, int moodId) {
        this.name = name;
        this.moodId = moodId;
    }
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public User(int userId, String name, int moodId) {
        this.userId = userId;
        this.name = name;
        this.moodId = moodId;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public int getMoodId() { return moodId; }
}
