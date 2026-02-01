package com.example.functiontesterfpv3;

public class Journal {
    private int journalId;
    private String date;
    private String positiveEvent;
    private String challenges;
    private String moodInfluence;
    private String lesson;
    private long userId;

    public Journal(int journalId, String date, String positiveEvent, String challenges, String moodInfluence, String lesson, long userId) {
        this.journalId = journalId;
        this.date = date;
        this.positiveEvent = positiveEvent;
        this.challenges = challenges;
        this.moodInfluence = moodInfluence;
        this.lesson = lesson;
        this.userId = userId;
    }
    public Journal(String date, String positiveEvent, String challenges, String moodInfluence, String lesson, long userId) {
        this.date = date;
        this.positiveEvent = positiveEvent;
        this.challenges = challenges;
        this.moodInfluence = moodInfluence;
        this.lesson = lesson;
        this.userId = userId;
    }

    public int getJournalId() {return journalId;}
    public String getDate() {return date;}
    public String getPositiveEvent() {return positiveEvent;}
    public String getChallenges() {return challenges;}
    public String getMoodInfluence() {return moodInfluence;}
    public String getLesson() {return lesson;}
    public long getUserId() {return userId;}
}
