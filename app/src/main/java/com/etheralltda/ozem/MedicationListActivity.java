package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MedicationListActivity extends AppCompatActivity {

    private RecyclerView recyclerMedList;
    private TextView txtMedListEmpty;
    private Button btnAddMedList;

    private List<Medication> medications = new ArrayList<>();
    private MedicationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_list);

        recyclerMedList = findViewById(R.id.recyclerMedList);
        txtMedListEmpty = findViewById(R.id.txtMedListEmpty);
        btnAddMedList = findViewById(R.id.btnAddMedList);

        recyclerMedList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicationAdapter(medications);
        recyclerMedList.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnAddMedList.setOnClickListener(v -> {
            Intent intent = new Intent(MedicationListActivity.this, ConfigMedicationActivity.class);
            startActivity(intent);
        });

        carregarLista();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }

    private void carregarLista() {
        medications.clear();
        medications.addAll(MedicationStorage.loadMedications(this));
        adapter.notifyDataSetChanged();

        if (medications.isEmpty()) {
            txtMedListEmpty.setVisibility(View.VISIBLE);
            recyclerMedList.setVisibility(View.GONE);
        } else {
            txtMedListEmpty.setVisibility(View.GONE);
            recyclerMedList.setVisibility(View.VISIBLE);
        }
    }
}
