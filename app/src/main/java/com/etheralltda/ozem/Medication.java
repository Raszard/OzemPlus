package com.etheralltda.ozem; // ajuste para o seu package

public class Medication {

    private String name;
    private String dose;
    private String frequency;
    private String nextDate;

    public Medication(String name, String dose, String frequency, String nextDate) {
        this.name = name;
        this.dose = dose;
        this.frequency = frequency;
        this.nextDate = nextDate;
    }

    public String getName() { return name; }
    public String getDose() { return dose; }
    public String getFrequency() { return frequency; }
    public String getNextDate() { return nextDate; }

    // >>> SETTERS que faltavam <<<
    public void setName(String name) {
        this.name = name;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setNextDate(String nextDate) {
        this.nextDate = nextDate;
    }
}

