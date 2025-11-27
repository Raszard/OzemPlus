package com.etheralltda.ozem;

public class InjectionEntry {

    private long timestamp;         // millis
    private String medicationName;  // nome exibido
    private String locationCode;    // "abdomen", "thigh", "arm"

    public InjectionEntry(long timestamp, String medicationName, String locationCode) {
        this.timestamp = timestamp;
        this.medicationName = medicationName;
        this.locationCode = locationCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public String getLocationCode() {
        return locationCode;
    }
}
