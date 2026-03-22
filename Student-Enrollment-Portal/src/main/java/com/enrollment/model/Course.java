package com.enrollment.model;

public class Course {

    private int id;
    private String title;
    private int credits;
    private int max_capacity;

    public Course(int id, String title, int credits, int max_capacity) {
        this.id = id;
        this.title = title;
        this.credits = credits;
        this.max_capacity = max_capacity;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getCredits() {
        return credits;
    }

    public int getMax_capacity() {
        return max_capacity;
    }
}
