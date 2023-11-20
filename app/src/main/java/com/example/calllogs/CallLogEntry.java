package com.example.calllogs;

public class CallLogEntry {
    private String number;
    private String name;
    private String type;
    private String date;

    public CallLogEntry(String number, String name, String type, String date) {
        this.number = number;
        this.name = name;
        this.type = type;
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Getters and setters
}