package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EducationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education);

        setupHeader();
        setupCards();
    }

    private void setupHeader() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupCards() {
        setupCard(findViewById(R.id.cardTip1),
                R.string.tip_1_title, R.string.tip_1_body, R.string.tip_1_source);

        setupCard(findViewById(R.id.cardTip2),
                R.string.tip_2_title, R.string.tip_2_body, R.string.tip_2_source);

        setupCard(findViewById(R.id.cardTip3),
                R.string.tip_3_title, R.string.tip_3_body, R.string.tip_3_source);

        setupCard(findViewById(R.id.cardTip4),
                R.string.tip_4_title, R.string.tip_4_body, R.string.tip_4_source);

        setupCard(findViewById(R.id.cardTip5),
                R.string.tip_5_title, R.string.tip_5_body, R.string.tip_5_source);
    }

    private void setupCard(View cardView, int titleRes, int bodyRes, int sourceRes) {
        TextView txtTitle = cardView.findViewById(R.id.txtTipTitle);
        TextView txtBody = cardView.findViewById(R.id.txtTipBody);
        TextView txtSource = cardView.findViewById(R.id.txtTipSource);

        txtTitle.setText(titleRes);
        txtBody.setText(bodyRes);
        txtSource.setText(sourceRes);
    }
}