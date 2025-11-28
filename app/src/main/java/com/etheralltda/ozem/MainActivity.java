package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICATIONS = 1001;

    private List<Medication> medications = new ArrayList<>();

    // Botões principais (Cards)
    private View btnCardMed;
    private View btnCardTips;
    private View btnCardReport;

    private Button btnInjection;
    private Button btnJourney;

    private View btnWeight;
    private View btnSymptoms;
    private View btnDailyGoals;

    private TextView txtGreeting;
    private TextView txtPlan;
    private TextView txtDashboardWeight;
    private TextView txtDashboardWeightDiff;
    private TextView txtDashboardNextInjection;
    private TextView txtDashboardWeekly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // VINCULAR COMPONENTES
        btnCardMed = findViewById(R.id.cardActionMed);
        btnCardTips = findViewById(R.id.cardActionTips);
        btnCardReport = findViewById(R.id.cardActionReport);

        btnInjection = findViewById(R.id.btnInjection);
        btnJourney = findViewById(R.id.btnJourney);

        btnWeight = findViewById(R.id.btnWeight);
        btnSymptoms = findViewById(R.id.btnSymptoms);
        btnDailyGoals = findViewById(R.id.btnDailyGoals);

        txtGreeting = findViewById(R.id.txtGreeting);
        txtPlan = findViewById(R.id.txtPlan);
        txtDashboardWeight = findViewById(R.id.txtDashboardWeight);
        txtDashboardWeightDiff = findViewById(R.id.txtDashboardWeightDiff);
        txtDashboardNextInjection = findViewById(R.id.txtDashboardNextInjection);
        txtDashboardWeekly = findViewById(R.id.txtDashboardWeekly);

        // CONFIGURAR CLIQUES
        btnCardMed.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MedicationListActivity.class)));
        btnCardTips.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EducationActivity.class)));
        btnCardReport.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ReportActivity.class)));

        // O listener do btnInjection agora é definido dinamicamente no atualizarDashboard()

        btnWeight.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WeightActivity.class)));
        btnDailyGoals.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DailyGoalsActivity.class)));
        btnSymptoms.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SymptomsActivity.class)));
        btnJourney.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, JourneyGlp1Activity.class)));

        pedirPermissaoNotificacaoSeNecessario();
        carregarLista();
        atualizarDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
        atualizarDashboard();
    }

    private void carregarLista() {
        medications.clear();
        medications.addAll(MedicationStorage.loadMedications(this));
    }

    private void pedirPermissaoNotificacaoSeNecessario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
            }
        }
    }

    private void atualizarDashboard() {
        // --- LÓGICA DO BOTÃO DE INJEÇÃO ---
        if (medications.isEmpty()) {
            // Se não tem remédios, muda o botão para cadastrar
            btnInjection.setText("Adicionar Medicamento");
            txtDashboardNextInjection.setText("Nenhum cadastrado");
            btnInjection.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, ConfigMedicationActivity.class))
            );
        } else {
            // Se tem remédios, volta ao normal
            btnInjection.setText("Registrar Aplicação Agora");
            btnInjection.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, InjectionActivity.class))
            );

            String nextInjection = "Não definida";
            for (Medication med : medications) {
                String nd = med.getNextDate();
                if (nd != null && !nd.trim().isEmpty()) {
                    nextInjection = nd.trim();
                    break;
                }
            }
            txtDashboardNextInjection.setText(nextInjection);
        }
        // ----------------------------------

        UserProfile profile = UserStorage.loadUserProfile(this);

        if (profile != null) {
            String name = profile.getName();
            txtGreeting.setText((name == null || name.trim().isEmpty()) ? "Olá!" : "Olá, " + name);

            boolean premium = UserStorage.isPremium(this);
            txtPlan.setText(premium ? "PRO" : "FREE");

            float cw = profile.getCurrentWeight();

            if (cw > 0) {
                txtDashboardWeight.setText(String.format(Locale.getDefault(), "%.1f kg", cw));
                List<WeightEntry> history = WeightStorage.loadWeights(this);

                if (!history.isEmpty()) {
                    float initialWeight = history.get(0).getWeight();
                    float diff = cw - initialWeight;

                    if (history.size() > 1 || Math.abs(diff) > 0.1) {
                        txtDashboardWeightDiff.setVisibility(View.VISIBLE);
                        String diffText = String.format(Locale.getDefault(), "%.1f kg", Math.abs(diff));

                        if (diff > 0) {
                            txtDashboardWeightDiff.setText("+" + diffText);
                            txtDashboardWeightDiff.setTextColor(Color.WHITE);
                            txtDashboardWeightDiff.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
                        } else if (diff < 0) {
                            txtDashboardWeightDiff.setText("-" + diffText);
                            txtDashboardWeightDiff.setTextColor(Color.WHITE);
                            txtDashboardWeightDiff.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
                        } else {
                            txtDashboardWeightDiff.setText("0.0 kg");
                            txtDashboardWeightDiff.setTextColor(ContextCompat.getColor(this, R.color.ozem_text_secondary));
                            txtDashboardWeightDiff.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
                        }
                    } else {
                        txtDashboardWeightDiff.setVisibility(View.GONE);
                    }
                } else {
                    txtDashboardWeightDiff.setVisibility(View.GONE);
                }
            } else {
                txtDashboardWeight.setText("-- kg");
                txtDashboardWeightDiff.setVisibility(View.GONE);
            }
        } else {
            txtGreeting.setText("Olá!");
            txtPlan.setText("FREE");
            txtDashboardWeight.setText("-- kg");
            txtDashboardWeightDiff.setVisibility(View.GONE);
        }

        int countWeek = contarAplicacoesUltimos7Dias();
        txtDashboardWeekly.setText(countWeek + " injeções");
    }

    private int contarAplicacoesUltimos7Dias() {
        List<InjectionEntry> list = InjectionStorage.loadInjections(this);
        long agora = System.currentTimeMillis();
        long limite = agora - 7L * 24 * 60 * 60 * 1000L;
        int count = 0;
        for (InjectionEntry e : list) {
            if (e.getTimestamp() >= limite) {
                count++;
            }
        }
        return count;
    }
}