package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RoutineActivity extends AppCompatActivity {

    private TextView txtCaloriesTarget, txtDeficitInfo;
    private TextView txtProteinGoal, txtCarbGoal, txtFatGoal;
    private TextView txtWaterRoutine, txtExerciseRoutine;
    private Button btnOpenTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        initViews();
        calcularRotina();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnOpenTips.setOnClickListener(v -> startActivity(new Intent(this, EducationActivity.class)));
    }

    private void initViews() {
        txtCaloriesTarget = findViewById(R.id.txtCaloriesTarget);
        txtDeficitInfo = findViewById(R.id.txtDeficitInfo);
        txtProteinGoal = findViewById(R.id.txtProteinGoal);
        txtCarbGoal = findViewById(R.id.txtCarbGoal);
        txtFatGoal = findViewById(R.id.txtFatGoal);
        txtWaterRoutine = findViewById(R.id.txtWaterRoutine);
        txtExerciseRoutine = findViewById(R.id.txtExerciseRoutine);
        btnOpenTips = findViewById(R.id.btnOpenTips);
    }

    private void calcularRotina() {
        UserProfile profile = UserStorage.loadUserProfile(this);
        SharedPreferences prefs = getSharedPreferences("glp1_prefs", MODE_PRIVATE);

        if (profile == null) {
            Toast.makeText(this, "Perfil não encontrado. Refaça o Quiz.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recuperar dados extras do Quiz
        String birthDateStr = prefs.getString("user_birthdate", "01/01/1990");
        String gender = prefs.getString("user_gender", "Outro");
        float weeklyGoalKg = prefs.getFloat("user_weekly_deficit_goal", 0.5f);

        int age = calculateAge(birthDateStr);
        float weight = profile.getCurrentWeight();
        float heightCm = profile.getHeight() * 100; // Converter m para cm

        // 1. Calcular TMB (Mifflin-St Jeor)
        double bmr;
        if (gender.equalsIgnoreCase("Masculino")) {
            bmr = (10 * weight) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            // Feminino ou Outro (Padrão Feminino por segurança calórica)
            bmr = (10 * weight) + (6.25 * heightCm) - (5 * age) - 161;
        }

        // 2. Fator de Atividade
        double activityFactor = 1.2; // Sedentário base
        String actLevel = profile.getActivityLevel();
        if (actLevel.contains("Levemente")) activityFactor = 1.375;
        else if (actLevel.contains("Moderadamente")) activityFactor = 1.55;
        else if (actLevel.contains("Muito")) activityFactor = 1.725;

        double tdee = bmr * activityFactor;

        // 3. Déficit Calórico (1kg gordura ~ 7700 kcal)
        double weeklyDeficitKcal = weeklyGoalKg * 7700;
        double dailyDeficit = weeklyDeficitKcal / 7.0;

        // Limite de segurança (não baixar de 1200 kcal)
        double targetCalories = tdee - dailyDeficit;
        if (targetCalories < 1200) targetCalories = 1200;

        // Atualizar UI Calorias
        txtCaloriesTarget.setText(String.format(Locale.getDefault(), "%.0f kcal", targetCalories));
        txtDeficitInfo.setText(String.format(Locale.getDefault(),
                "Baseado em um déficit aprox. de %.0f kcal para meta de %.1f kg/sem.", dailyDeficit, weeklyGoalKg));

        // 4. Macros (Estimativa: Prot 30%, Fat 30%, Carb 40%)
        // 1g Prot = 4kcal, 1g Carb = 4kcal, 1g Fat = 9kcal
        double proteinCals = targetCalories * 0.30;
        double fatCals = targetCalories * 0.30;
        double carbCals = targetCalories * 0.40;

        int proteinG = (int) (proteinCals / 4);
        int fatG = (int) (fatCals / 9);
        int carbG = (int) (carbCals / 4);

        // Ajuste fino de proteína: mínimo 1.2g/kg para quem usa GLP-1 para evitar perda muscular
        if (proteinG < (weight * 1.2)) {
            proteinG = (int) (weight * 1.2);
            // Recalcula o resto para caber nas calorias se possível, ou apenas sugere mais proteína
        }

        txtProteinGoal.setText(proteinG + " g");
        txtCarbGoal.setText(carbG + " g");
        txtFatGoal.setText(fatG + " g");

        // 5. Água (35ml por kg)
        double waterMl = weight * 35;
        txtWaterRoutine.setText(String.format(Locale.getDefault(), "%.1f L", waterMl / 1000));

        // 6. Exercício Sugerido
        String exerciseSuggestion = "30 min";
        if (actLevel.contains("Levemente")) exerciseSuggestion = "30-45 min";
        else if (actLevel.contains("Moderadamente")) exerciseSuggestion = "45-60 min";
        else if (actLevel.contains("Muito")) exerciseSuggestion = "60+ min";

        txtExerciseRoutine.setText(exerciseSuggestion);
    }

    private int calculateAge(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(dateStr);
            if (birthDate == null) return 30; // Fallback

            Calendar dob = Calendar.getInstance();
            dob.setTime(birthDate);
            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (Exception e) {
            return 30; // Fallback padrão
        }
    }
}