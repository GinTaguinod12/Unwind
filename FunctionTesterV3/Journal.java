package com.taguinod.unwind;

import java.io.Serializable;

public class Journal implements Serializable {
    private int journalId;
    private String date;
    private String positiveEvent; // Maps to Question 1
    private String challenges;    // Maps to Question 2
    private String moodInfluence; // Maps to Question 3
    private String lesson;        // Maps to Question 4 & 5 (Combined)
    private long userId;

    // Constructor for fetching from Database (with ID)
    public Journal(int journalId, String date, String positiveEvent, String challenges, String moodInfluence, String lesson, long userId) {
        this.journalId = journalId;
        this.date = date;
        this.positiveEvent = positiveEvent;
        this.challenges = challenges;
        this.moodInfluence = moodInfluence;
        this.lesson = lesson;
        this.userId = userId;
    }

    // Constructor for creating new entries (without ID)
    public Journal(String date, String positiveEvent, String challenges, String moodInfluence, String lesson, long userId) {
        this.date = date;
        this.positiveEvent = positiveEvent;
        this.challenges = challenges;
        this.moodInfluence = moodInfluence;
        this.lesson = lesson;
        this.userId = userId;
    }

    // --- Standard Getters ---
    public int getJournalId() { return journalId; }
    public String getDate() { return date; }
    public String getPositiveEvent() { return positiveEvent; }
    public String getChallenges() { return challenges; }
    public String getMoodInfluence() { return moodInfluence; }
    public String getLesson() { return lesson; }
    public long getUserId() { return userId; }

    // --- Standard Setters (Added these for you) ---
    public void setJournalId(int journalId) { this.journalId = journalId; }
    public void setDate(String date) { this.date = date; }
    public void setPositiveEvent(String positiveEvent) { this.positiveEvent = positiveEvent; }
    public void setChallenges(String challenges) { this.challenges = challenges; }
    public void setMoodInfluence(String moodInfluence) { this.moodInfluence = moodInfluence; }
    public void setLesson(String lesson) { this.lesson = lesson; }
    public void setUserId(long userId) { this.userId = userId; }

    // --- Convenience Aliases (For your 5-Question UI) ---
    public String getAns1() { return positiveEvent; }
    public String getAns2() { return challenges; }
    public String getAns3() { return moodInfluence; }

    // Since Q4 and Q5 are stored together in 'lesson',
    // these return the full text to ensure nothing is lost.
    public String getAns4() { return lesson; }
    public String getAns5() { return ""; } // Handled via getAns4 in this mapping
}
