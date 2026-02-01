package com.example.functiontesterfpv3;

public class Quote {
    private int quoteId;
    private String quoteText;
    private String author;
    private int moodId;

    public Quote(String quoteText, String author) {
        this.quoteText = quoteText;
        this.author = author;
    }

    public Quote(int quoteId, String quoteText, String author, int moodId) {
        this.quoteId = quoteId;
        this.quoteText = quoteText;
        this.author = author;
        this.moodId = moodId;
    }

    public String getQuoteText() { return quoteText; }
    public String getAuthor() { return author; }
}
