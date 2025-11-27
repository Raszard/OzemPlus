package com.etheralltda.ozem;

public class WeightEntry {

    private long timestamp; // millis desde 1970
    private float weight;   // em kg

    public WeightEntry(long timestamp, float weight) {
        this.timestamp = timestamp;
        this.weight = weight;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getWeight() {
        return weight;
    }
}
