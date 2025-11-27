package com.etheralltda.ozem;

public class SymptomEntry {

    private long timestamp;
    private int nausea;
    private int fatigue;
    private int satiety;
    private String notes;

    public SymptomEntry(long timestamp, int nausea, int fatigue, int satiety, String notes) {
        this.timestamp = timestamp;
        this.nausea = nausea;
        this.fatigue = fatigue;
        this.satiety = satiety;
        this.notes = notes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getNausea() {
        return nausea;
    }

    public int getFatigue() {
        return fatigue;
    }

    public int getSatiety() {
        return satiety;
    }

    public String getNotes() {
        return notes;
    }
}
