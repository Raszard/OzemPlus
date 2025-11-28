package com.etheralltda.ozem;

public class Medication {

    private String name;
    private String dose;
    private String frequency;
    private String nextDate; // Texto livre (ex: "Segunda cedo")
    private int dayOfWeek;   // NOVO: 1 (Dom) a 7 (Sáb) - Compatível com Calendar

    // Construtor atualizado
    public Medication(String name, String dose, String frequency, String nextDate, int dayOfWeek) {
        this.name = name;
        this.dose = dose;
        this.frequency = frequency;
        this.nextDate = nextDate;
        this.dayOfWeek = dayOfWeek;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDose() { return dose; }
    public void setDose(String dose) { this.dose = dose; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getNextDate() { return nextDate; }
    public void setNextDate(String nextDate) { this.nextDate = nextDate; }

    // Getter e Setter novos
    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
}