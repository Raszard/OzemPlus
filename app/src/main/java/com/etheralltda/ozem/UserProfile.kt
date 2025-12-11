package com.etheralltda.ozem;

public class UserProfile {

    private String name;
    private float currentWeight;
    private float targetWeight;
    private float height; // Altura em metros (ex: 1.75)
    private String goalType;      // emagrecimento, glicemia, ambos
    private String activityLevel; // baixo, moderado, alto
    private float waterGoalLiters;
    private boolean premium;      // free/premium

    public UserProfile() { }

    public UserProfile(String name,
                       float currentWeight,
                       float targetWeight,
                       float height, // Novo parâmetro
                       String goalType,
                       String activityLevel,
                       float waterGoalLiters,
                       boolean premium) {
        this.name = name;
        this.currentWeight = currentWeight;
        this.targetWeight = targetWeight;
        this.height = height;
        this.goalType = goalType;
        this.activityLevel = activityLevel;
        this.waterGoalLiters = waterGoalLiters;
        this.premium = premium;
    }

    public String getName() { return name; }
    public float getCurrentWeight() { return currentWeight; }
    public float getTargetWeight() { return targetWeight; }
    public float getHeight() { return height; } // Getter da altura
    public String getGoalType() { return goalType; }
    public String getActivityLevel() { return activityLevel; }
    public float getWaterGoalLiters() { return waterGoalLiters; }
    public boolean isPremium() { return premium; }

    public void setName(String name) { this.name = name; }
    public void setCurrentWeight(float currentWeight) { this.currentWeight = currentWeight; }
    public void setTargetWeight(float targetWeight) { this.targetWeight = targetWeight; }
    public void setHeight(float height) { this.height = height; } // Setter da altura
    public void setGoalType(String goalType) { this.goalType = goalType; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public void setWaterGoalLiters(float waterGoalLiters) { this.waterGoalLiters = waterGoalLiters; }
    public void setPremium(boolean premium) { this.premium = premium; }
}