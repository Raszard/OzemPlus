package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {

    private EditText edtName;
    private EditText edtCurrentWeight;
    private EditText edtTargetWeight;
    private RadioGroup rgGoal;
    private RadioButton rbGoalWeight;
    private RadioButton rbGoalGlycemic;
    private RadioButton rbGoalBoth;
    private Spinner spActivityLevel;
    private Spinner spWaterGoal;
    private Button btnQuizContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se já fez o onboarding, pula direto pro dashboard
        if (UserStorage.isOnboardingDone(this)) {
            abrirMainActivity();
            return;
        }

        setContentView(R.layout.activity_quiz);

        edtName = findViewById(R.id.edtName);
        edtCurrentWeight = findViewById(R.id.edtCurrentWeight);
        edtTargetWeight = findViewById(R.id.edtTargetWeight);
        rgGoal = findViewById(R.id.rgGoal);
        rbGoalWeight = findViewById(R.id.rbGoalWeight);
        rbGoalGlycemic = findViewById(R.id.rbGoalGlycemic);
        rbGoalBoth = findViewById(R.id.rbGoalBoth);
        spActivityLevel = findViewById(R.id.spActivityLevel);
        spWaterGoal = findViewById(R.id.spWaterGoal);
        btnQuizContinue = findViewById(R.id.btnQuizContinue);

        configurarSpinners();

        btnQuizContinue.setOnClickListener(v -> salvarEContinuar());
    }

    private void configurarSpinners() {
        String[] activityOptions = new String[]{
                "Baixo (quase não me movimento)",
                "Moderado (caminho um pouco por dia)",
                "Alto (faço exercícios regularmente)"
        };
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                activityOptions
        );
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spActivityLevel.setAdapter(activityAdapter);

        String[] waterOptions = new String[]{
                "1,0 L por dia",
                "1,5 L por dia",
                "2,0 L por dia",
                "2,5 L por dia",
                "3,0 L ou mais por dia"
        };
        ArrayAdapter<String> waterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                waterOptions
        );
        waterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWaterGoal.setAdapter(waterAdapter);
    }

    private void salvarEContinuar() {
        String name = edtName.getText().toString().trim();
        String currentStr = edtCurrentWeight.getText().toString().trim();
        String targetStr = edtTargetWeight.getText().toString().trim();

        if (name.isEmpty() || currentStr.isEmpty() || targetStr.isEmpty()) {
            Toast.makeText(this, "Preencha nome, peso atual e peso objetivo.", Toast.LENGTH_SHORT).show();
            return;
        }

        float currentWeight;
        float targetWeight;
        try {
            currentWeight = Float.parseFloat(currentStr.replace(",", "."));
            targetWeight = Float.parseFloat(targetStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Peso inválido. Use números, ex: 85.0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentWeight <= 0 || targetWeight <= 0) {
            Toast.makeText(this, "Pesos devem ser maiores que zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        String goalType;
        int checkedId = rgGoal.getCheckedRadioButtonId();
        if (checkedId == rbGoalWeight.getId()) {
            goalType = "Perda de peso";
        } else if (checkedId == rbGoalGlycemic.getId()) {
            goalType = "Controle glicêmico";
        } else if (checkedId == rbGoalBoth.getId()) {
            goalType = "Ambos";
        } else {
            Toast.makeText(this, "Selecione um objetivo principal.", Toast.LENGTH_SHORT).show();
            return;
        }

        String activityLevel = (String) spActivityLevel.getSelectedItem();
        String waterOption = (String) spWaterGoal.getSelectedItem();
        float waterGoalLiters = 2.0f;
        if (waterOption.startsWith("1,0")) waterGoalLiters = 1.0f;
        else if (waterOption.startsWith("1,5")) waterGoalLiters = 1.5f;
        else if (waterOption.startsWith("2,0")) waterGoalLiters = 2.0f;
        else if (waterOption.startsWith("2,5")) waterGoalLiters = 2.5f;
        else if (waterOption.startsWith("3")) waterGoalLiters = 3.0f;

        // Versão free por padrão
        UserProfile profile = new UserProfile(
                name,
                currentWeight,
                targetWeight,
                goalType,
                activityLevel,
                waterGoalLiters,
                false
        );

        UserStorage.saveUserProfile(this, profile);
        UserStorage.setOnboardingDone(this, true);

        abrirMainActivity();
    }

    private void abrirMainActivity() {
        Intent intent = new Intent(QuizActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
