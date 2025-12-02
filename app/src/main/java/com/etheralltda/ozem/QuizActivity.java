package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private LinearLayout layoutTopBar, layoutBottomBar;
    private TextView btnQuizBack, txtStepIndicator;
    private LinearProgressIndicator quizProgress;
    private Button btnStartWelcome, btnQuizContinue;

    // --- CAMPOS PART 1 ---
    private ChipGroup chipGroupUses;
    private ChipGroup chipGroupMeds;
    private Chip chipOtherMed;
    private TextInputLayout layoutOtherMedName;
    private EditText edtOtherMedName;
    private Slider sliderDosage;
    private TextView txtDosageValue;
    private TextView txtDosageNote;
    private ChipGroup chipGroupFrequency;

    // --- CAMPOS PART 2 ---
    private ChipGroup chipGroupGender;
    private DatePicker datePickerBirth;
    private MaterialButtonToggleGroup toggleUnits;

    private LinearLayout layoutMetricPickers;
    private NumberPicker npHeightMetric, npWeightMetric;

    private LinearLayout layoutImperialPickers;
    private NumberPicker npHeightFt, npHeightIn, npWeightImperial;

    private Slider sliderWeeklyGoal;
    private TextView txtWeeklyGoalValue;
    private ChipGroup chipGroupActivity;

    // --- CAMPOS PART 3 ---
    private LinearLayout layoutFreqWeeklyConfig;
    private ChipGroup chipGroupWeekDay;

    private LinearLayout layoutFreqMonthlyConfig;
    private NumberPicker npDayOfMonth;

    private TimePicker timePicker;

    private EditText edtFinalName, edtEmail;

    // --- DADOS COLETADOS ---
    private boolean usesGlp1;
    private String selectedMedName = "Medicamento";
    private String selectedDose = "0.25 mg";
    private String selectedFrequency = "Semanalmente";
    private String selectedGender;
    private String birthDate;
    private boolean isMetric = true;
    private float finalHeightM = 0;
    private float finalWeightKg = 0;
    private float weeklyGoalKg = 0.5f;
    private String selectedActivityLevel = "Moderado";

    // Dados de Lembrete
    private int selectedDayOfWeek = Calendar.MONDAY;
    private int selectedDayOfMonth = 1;
    private int selectedHour = 8;
    private int selectedMinute = 0;

    private String userName;
    private String userEmail;

    private int currentStep = 0;
    private static final int FINAL_STEP_INDEX = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserStorage.isOnboardingDone(this)) {
            abrirApp();
            return;
        }
        setContentView(R.layout.activity_quiz);
        initViews();
        setupPickers();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        layoutTopBar = findViewById(R.id.layoutTopBar);
        layoutBottomBar = findViewById(R.id.layoutBottomBar);
        btnQuizBack = findViewById(R.id.btnQuizBack);
        quizProgress = findViewById(R.id.quizProgress);
        txtStepIndicator = findViewById(R.id.txtStepIndicator);
        btnStartWelcome = findViewById(R.id.btnStartWelcome);
        btnQuizContinue = findViewById(R.id.btnQuizContinue);

        chipGroupUses = findViewById(R.id.chipGroupUses);
        chipGroupMeds = findViewById(R.id.chipGroupMeds);
        chipOtherMed = findViewById(R.id.chipOtherMed);
        layoutOtherMedName = findViewById(R.id.layoutOtherMedName);
        edtOtherMedName = findViewById(R.id.edtOtherMedName);
        sliderDosage = findViewById(R.id.sliderDosage);
        txtDosageValue = findViewById(R.id.txtDosageValue);
        txtDosageNote = findViewById(R.id.txtDosageNote);
        chipGroupFrequency = findViewById(R.id.chipGroupFrequency);

        chipGroupGender = findViewById(R.id.chipGroupGender);
        datePickerBirth = findViewById(R.id.datePickerBirth);
        toggleUnits = findViewById(R.id.toggleUnits);

        layoutMetricPickers = findViewById(R.id.layoutMetricPickers);
        npHeightMetric = findViewById(R.id.npHeightMetric);
        npWeightMetric = findViewById(R.id.npWeightMetric);
        layoutImperialPickers = findViewById(R.id.layoutImperialPickers);
        npHeightFt = findViewById(R.id.npHeightFt);
        npHeightIn = findViewById(R.id.npHeightIn);
        npWeightImperial = findViewById(R.id.npWeightImperial);

        sliderWeeklyGoal = findViewById(R.id.sliderWeeklyGoal);
        txtWeeklyGoalValue = findViewById(R.id.txtWeeklyGoalValue);
        chipGroupActivity = findViewById(R.id.chipGroupActivity);

        // Novos campos Step 10
        layoutFreqWeeklyConfig = findViewById(R.id.layoutFreqWeeklyConfig);
        chipGroupWeekDay = findViewById(R.id.chipGroupWeekDay);
        layoutFreqMonthlyConfig = findViewById(R.id.layoutFreqMonthlyConfig);
        npDayOfMonth = findViewById(R.id.npDayOfMonth);
        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        edtFinalName = findViewById(R.id.edtFinalName);
        edtEmail = findViewById(R.id.edtEmail);
    }

    private void setupPickers() {
        npHeightMetric.setMinValue(100); npHeightMetric.setMaxValue(250); npHeightMetric.setValue(170);
        npWeightMetric.setMinValue(30); npWeightMetric.setMaxValue(300); npWeightMetric.setValue(70);

        npHeightFt.setMinValue(3); npHeightFt.setMaxValue(8); npHeightFt.setValue(5);
        npHeightIn.setMinValue(0); npHeightIn.setMaxValue(11); npHeightIn.setValue(7);
        npWeightImperial.setMinValue(60); npWeightImperial.setMaxValue(660); npWeightImperial.setValue(154);

        npDayOfMonth.setMinValue(1);
        npDayOfMonth.setMaxValue(31);
        npDayOfMonth.setValue(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    private void setupListeners() {
        btnStartWelcome.setOnClickListener(v -> nextStep());
        btnQuizContinue.setOnClickListener(v -> nextStep());
        btnQuizBack.setOnClickListener(v -> prevStep());

        chipGroupMeds.setOnCheckedStateChangeListener((group, checkedIds) -> {
            boolean isOther = checkedIds.contains(R.id.chipOtherMed);
            layoutOtherMedName.setVisibility(isOther ? View.VISIBLE : View.GONE);
            if(checkedIds.contains(R.id.chipMounjaro)) {
                sliderDosage.setValueTo(15.0f); sliderDosage.setStepSize(2.5f); sliderDosage.setValue(2.5f);
                txtDosageNote.setText("Doses comuns: 2.5, 5, 7.5, 10, 12.5, 15 mg");
            } else {
                sliderDosage.setValueTo(5.0f); sliderDosage.setStepSize(0.25f);
                if(sliderDosage.getValue()>5) sliderDosage.setValue(0.25f);
                txtDosageNote.setText("Ajuste fino (limite 5.0 mg)");
            }
        });

        sliderDosage.addOnChangeListener((slider, value, fromUser) -> {
            if (value == (long) value) txtDosageValue.setText(String.format(Locale.getDefault(), "%d mg", (long) value));
            else txtDosageValue.setText(String.format(Locale.getDefault(), "%.2f mg", value));
        });

        toggleUnits.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnMetric) {
                    isMetric = true; layoutMetricPickers.setVisibility(View.VISIBLE); layoutImperialPickers.setVisibility(View.GONE);
                } else {
                    isMetric = false; layoutMetricPickers.setVisibility(View.GONE); layoutImperialPickers.setVisibility(View.VISIBLE);
                }
            }
        });

        sliderWeeklyGoal.addOnChangeListener((slider, value, fromUser) -> {
            if (!isMetric) {
                float lbs = value * 2.20462f;
                txtWeeklyGoalValue.setText(String.format(Locale.getDefault(), "%.1f lbs / semana", lbs));
            } else {
                txtWeeklyGoalValue.setText(String.format(Locale.getDefault(), "%.1f kg / semana", value));
            }
        });
    }

    private void updateStep10Visibility() {
        layoutFreqWeeklyConfig.setVisibility(View.GONE);
        layoutFreqMonthlyConfig.setVisibility(View.GONE);

        if (selectedFrequency.equals("Semanalmente")) {
            layoutFreqWeeklyConfig.setVisibility(View.VISIBLE);
        } else if (selectedFrequency.equals("Mensalmente")) {
            layoutFreqMonthlyConfig.setVisibility(View.VISIBLE);
        }
    }

    private void nextStep() {
        if (currentStep == 0) {
            currentStep = 1;
            viewFlipper.setDisplayedChild(1);
            updateUI();
            return;
        }

        if (!validarEtapaAtual()) return;

        if (currentStep == FINAL_STEP_INDEX) {
            salvarFinal();
        } else {
            currentStep++;
            viewFlipper.showNext();
            updateUI();
        }
    }

    private void prevStep() {
        if (currentStep > 1) {
            currentStep--;
            viewFlipper.showPrevious();
            updateUI();
        } else if (currentStep == 1) {
            currentStep = 0;
            viewFlipper.setDisplayedChild(0);
            updateUI();
        }
    }

    private void updateUI() {
        if (currentStep == 0) {
            layoutTopBar.setVisibility(View.GONE);
            layoutBottomBar.setVisibility(View.GONE);
        } else {
            layoutTopBar.setVisibility(View.VISIBLE);
            layoutBottomBar.setVisibility(View.VISIBLE);

            quizProgress.setMax(FINAL_STEP_INDEX);
            quizProgress.setProgress(currentStep);
            txtStepIndicator.setText(currentStep + "/" + FINAL_STEP_INDEX);

            if (currentStep == FINAL_STEP_INDEX) {
                btnQuizContinue.setText("Finalizar");
            } else {
                btnQuizContinue.setText("Próximo");
            }

            if (currentStep == 10) {
                updateStep10Visibility();
            }
        }
    }

    private boolean validarEtapaAtual() {
        switch (currentStep) {
            case 1:
                if (chipGroupUses.getCheckedChipId() == -1) return erro("Selecione uma opção");
                usesGlp1 = (chipGroupUses.getCheckedChipId() == R.id.chipYesGlp1);
                return true;
            case 2:
                if (chipGroupMeds.getCheckedChipId() == -1) return erro("Selecione o medicamento");
                if (chipGroupMeds.getCheckedChipId() == R.id.chipOtherMed) {
                    if (edtOtherMedName.getText().toString().trim().isEmpty()) { layoutOtherMedName.setError("Nome obrigatório"); return false; }
                    selectedMedName = edtOtherMedName.getText().toString().trim();
                } else {
                    Chip chip = findViewById(chipGroupMeds.getCheckedChipId());
                    String txt = chip.getText().toString();
                    if (txt.contains("(")) selectedMedName = txt.substring(0, txt.indexOf("(")).trim();
                    else selectedMedName = txt;
                }
                return true;
            case 3:
                if (sliderDosage.getValue() <= 0) return false;
                selectedDose = txtDosageValue.getText().toString();
                return true;
            case 4:
                int idFreq = chipGroupFrequency.getCheckedChipId();
                if (idFreq == -1) return erro("Selecione a frequência");
                if (idFreq == R.id.chipDaily) selectedFrequency = "Diariamente";
                else if (idFreq == R.id.chipWeekly) selectedFrequency = "Semanalmente";
                else selectedFrequency = "Mensalmente";
                return true;
            case 5:
                if (chipGroupGender.getCheckedChipId() == -1) return erro("Selecione o sexo");
                selectedGender = ((Chip)findViewById(chipGroupGender.getCheckedChipId())).getText().toString();
                return true;
            case 6:
                int d = datePickerBirth.getDayOfMonth();
                int m = datePickerBirth.getMonth() + 1;
                int y = datePickerBirth.getYear();
                birthDate = d + "/" + m + "/" + y;
                return true;
            case 7:
                if (isMetric) {
                    finalHeightM = npHeightMetric.getValue() / 100f;
                    finalWeightKg = npWeightMetric.getValue();
                } else {
                    float ft = npHeightFt.getValue();
                    float in = npHeightIn.getValue();
                    float lbs = npWeightImperial.getValue();
                    finalHeightM = ((ft * 30.48f) + (in * 2.54f)) / 100f;
                    finalWeightKg = lbs * 0.453592f;
                }
                return true;
            case 8:
                weeklyGoalKg = sliderWeeklyGoal.getValue();
                return true;
            case 9:
                if (chipGroupActivity.getCheckedChipId() == -1) return erro("Selecione a atividade");
                String act = ((Chip)findViewById(chipGroupActivity.getCheckedChipId())).getText().toString();
                if (act.contains("\n")) selectedActivityLevel = act.substring(0, act.indexOf("\n"));
                else selectedActivityLevel = act;
                return true;
            case 10:
                selectedHour = timePicker.getHour();
                selectedMinute = timePicker.getMinute();

                if (selectedFrequency.equals("Semanalmente")) {
                    int idDay = chipGroupWeekDay.getCheckedChipId();
                    if (idDay == -1) return erro("Selecione um dia da semana");

                    if (idDay == R.id.chipMon) selectedDayOfWeek = Calendar.MONDAY;
                    else if (idDay == R.id.chipTue) selectedDayOfWeek = Calendar.TUESDAY;
                    else if (idDay == R.id.chipWed) selectedDayOfWeek = Calendar.WEDNESDAY;
                    else if (idDay == R.id.chipThu) selectedDayOfWeek = Calendar.THURSDAY;
                    else if (idDay == R.id.chipFri) selectedDayOfWeek = Calendar.FRIDAY;
                    else if (idDay == R.id.chipSat) selectedDayOfWeek = Calendar.SATURDAY;
                    else selectedDayOfWeek = Calendar.SUNDAY;
                } else if (selectedFrequency.equals("Mensalmente")) {
                    selectedDayOfMonth = npDayOfMonth.getValue();
                }
                return true;
            case 11: return true;
            case 12: return true;
            case 13:
                userName = edtFinalName.getText().toString().trim();
                if (userName.isEmpty()) { edtFinalName.setError("Obrigatório"); return false; }
                return true;
            case 14:
                userEmail = edtEmail.getText().toString().trim();
                if (userEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    edtEmail.setError("E-mail inválido"); return false;
                }
                return true;
        }
        return true;
    }

    private boolean erro(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void salvarFinal() {
        try {
            float targetW = finalWeightKg - 5.0f;

            UserProfile profile = new UserProfile(
                    userName,
                    finalWeightKg,
                    targetW,
                    finalHeightM,
                    "Perda de Peso",
                    selectedActivityLevel,
                    2.0f,
                    false
            );
            UserStorage.saveUserProfile(this, profile);
            WeightStorage.addWeight(this, new WeightEntry(System.currentTimeMillis(), finalWeightKg));

            String nextDateTxt = "";
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);

            if (selectedFrequency.equals("Diariamente")) {
                nextDateTxt = "Todos os dias às " + timeStr;
            } else if (selectedFrequency.equals("Mensalmente")) {
                nextDateTxt = "Todo dia " + selectedDayOfMonth + " às " + timeStr;
            } else {
                String diaStr = "Dia";
                switch (selectedDayOfWeek) {
                    case Calendar.MONDAY: diaStr = "Segunda"; break;
                    case Calendar.TUESDAY: diaStr = "Terça"; break;
                    case Calendar.WEDNESDAY: diaStr = "Quarta"; break;
                    case Calendar.THURSDAY: diaStr = "Quinta"; break;
                    case Calendar.FRIDAY: diaStr = "Sexta"; break;
                    case Calendar.SATURDAY: diaStr = "Sábado"; break;
                    case Calendar.SUNDAY: diaStr = "Domingo"; break;
                }
                nextDateTxt = "Toda " + diaStr + " às " + timeStr;
            }

            // CORREÇÃO: Criar variável 'med' antes de adicionar na lista
            Medication med = new Medication(
                    selectedMedName,
                    selectedDose,
                    selectedFrequency,
                    nextDateTxt,
                    selectedDayOfWeek
            );

            List<Medication> meds = new ArrayList<>();
            meds.add(med);
            MedicationStorage.saveMedications(this, meds);

            // AGORA FUNCIONA: Variável 'med' existe
            NotificationScheduler.scheduleMedication(this, med);

            SharedPreferences prefs = getSharedPreferences("glp1_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("user_email", userEmail)
                    .putString("user_birthdate", birthDate)
                    .putString("user_gender", selectedGender)
                    .putFloat("user_weekly_deficit_goal", weeklyGoalKg)
                    .apply();

            UserStorage.setOnboardingDone(this, true);
            abrirApp();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao salvar. Tente novamente.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void abrirApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) prevStep();
        else super.onBackPressed();
    }
}