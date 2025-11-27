package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class EducationActivity extends AppCompatActivity {

    private TextView txtPlanStatus;
    private Switch switchPremiumTest;
    private TextView txtPremiumLocked;
    private LinearLayout layoutPremiumContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education);

        txtPlanStatus = findViewById(R.id.txtPlanStatus);
        switchPremiumTest = findViewById(R.id.switchPremiumTest);
        txtPremiumLocked = findViewById(R.id.txtPremiumLocked);
        layoutPremiumContent = findViewById(R.id.layoutPremiumContent);

        boolean isPremium = UserStorage.isPremium(this);
        switchPremiumTest.setChecked(isPremium);
        updatePremiumUI(isPremium);

        switchPremiumTest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Salva estado premium para o app inteiro
            UserStorage.setPremium(EducationActivity.this, isChecked);
            updatePremiumUI(isChecked);
        });
    }

    private void updatePremiumUI(boolean premium) {
        String prefix = getString(R.string.education_plan_status_prefix);
        String status = premium
                ? getString(R.string.education_plan_status_premium)
                : getString(R.string.education_plan_status_free);

        txtPlanStatus.setText(prefix + " " + status);

        if (premium) {
            layoutPremiumContent.setVisibility(View.VISIBLE);
            txtPremiumLocked.setVisibility(View.GONE);
        } else {
            layoutPremiumContent.setVisibility(View.GONE);
            txtPremiumLocked.setVisibility(View.VISIBLE);
        }
    }
}
