package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View; // Importação necessária
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICATIONS = 1001;
    private static final String PREFS_NAME = "glp1_prefs";

    private List<Medication> medications = new ArrayList<>();

    // Botões que continuam sendo Buttons
    private Button btnAddMed;
    private Button btnReminder;
    private Button btnExportSummary;
    private Button btnInjection;
    private Button btnEducation;
    private Button btnJourney;

    // Widgets que agora são Cards (alterado de Button para View)
    private View btnWeight;
    private View btnSymptoms;
    private View btnDailyGoals;

    private TextView txtGreeting;
    private TextView txtPlan;
    private TextView txtDashboardWeight;
    private TextView txtDashboardNextInjection;
    private TextView txtDashboardWeekly;

    private TextView txtWeeklySummaryInjections;
    private TextView txtWeeklySummarySymptoms;
    private TextView txtWeeklySummaryWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular Views
        btnAddMed = findViewById(R.id.btnAddMed);
        btnReminder = findViewById(R.id.btnReminder);
        btnExportSummary = findViewById(R.id.btnExportSummary);
        btnInjection = findViewById(R.id.btnInjection);
        btnEducation = findViewById(R.id.btnEducation);
        btnJourney = findViewById(R.id.btnJourney);

        // Estes agora aceitam o MaterialCardView do layout
        btnWeight = findViewById(R.id.btnWeight);
        btnSymptoms = findViewById(R.id.btnSymptoms);
        btnDailyGoals = findViewById(R.id.btnDailyGoals);

        txtGreeting = findViewById(R.id.txtGreeting);
        txtPlan = findViewById(R.id.txtPlan);
        txtDashboardWeight = findViewById(R.id.txtDashboardWeight);
        txtDashboardNextInjection = findViewById(R.id.txtDashboardNextInjection);
        txtDashboardWeekly = findViewById(R.id.txtDashboardWeekly);

        // Estes campos podem estar ocultos no novo layout, mas mantemos para evitar erro de null se referenciados
        txtWeeklySummaryInjections = findViewById(R.id.txtWeeklySummaryInjections);
        txtWeeklySummarySymptoms = findViewById(R.id.txtWeeklySummarySymptoms);
        txtWeeklySummaryWeight = findViewById(R.id.txtWeeklySummaryWeight);

        // Configurar Cliques
        btnAddMed.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicationListActivity.class);
            startActivity(intent);
        });

        btnReminder.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
            startActivity(intent);
        });

        btnInjection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InjectionActivity.class);
            startActivity(intent);
        });

        btnWeight.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WeightActivity.class);
            startActivity(intent);
        });

        btnDailyGoals.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DailyGoalsActivity.class);
            startActivity(intent);
        });

        btnSymptoms.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SymptomsActivity.class);
            startActivity(intent);
        });

        btnEducation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        btnExportSummary.setOnClickListener(v -> exportarResumo());

        btnJourney.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, JourneyGlp1Activity.class));
        });

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
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICATIONS
                );
            }
        }
    }

    private void atualizarDashboard() {
        UserProfile profile = UserStorage.loadUserProfile(this);

        if (profile != null) {
            String name = profile.getName();
            if (name == null || name.trim().isEmpty()) {
                txtGreeting.setText("Olá!");
            } else {
                txtGreeting.setText("Olá, " + name);
            }

            boolean premium = UserStorage.isPremium(this);
            txtPlan.setText(premium ? "PRO" : "FREE");

            float cw = profile.getCurrentWeight();
            float tw = profile.getTargetWeight();

            if (cw > 0) {
                txtDashboardWeight.setText(String.format(Locale.getDefault(), "%.1f kg", cw));
            } else {
                txtDashboardWeight.setText("-- kg");
            }
        } else {
            txtGreeting.setText("Olá!");
            txtPlan.setText("FREE");
            txtDashboardWeight.setText("-- kg");
        }

        String nextInjection = "Não definida";
        // Lógica simples para pegar a primeira data encontrada (pode ser melhorada)
        for (Medication med : medications) {
            String nd = med.getNextDate();
            if (nd != null && !nd.trim().isEmpty()) {
                nextInjection = nd.trim();
                break;
            }
        }
        txtDashboardNextInjection.setText(nextInjection);

        int countWeek = contarAplicacoesUltimos7Dias();
        txtDashboardWeekly.setText(countWeek + " injeções");

        // Atualiza textos ocultos se necessário, ou remove se não usar mais
        atualizarResumoSemanal();
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

    private void atualizarResumoSemanal() {
        // Mantendo a lógica para evitar erros, mesmo que os textos estejam "gone" no layout
        int injCount = contarAplicacoesUltimos7Dias();
        if (txtWeeklySummaryInjections != null) {
            String injText = String.format(
                    Locale.getDefault(),
                    getString(R.string.dashboard_summary_injections_format),
                    injCount
            );
            txtWeeklySummaryInjections.setText(injText);
        }

        long agora = System.currentTimeMillis();
        long limite = agora - 7L * 24 * 60 * 60 * 1000L;

        List<SymptomEntry> sintomas = SymptomStorage.loadSymptoms(this);
        int count = 0;
        int sumNausea = 0;
        int sumFatigue = 0;
        int sumSatiety = 0;

        for (SymptomEntry e : sintomas) {
            if (e.getTimestamp() >= limite) {
                sumNausea += e.getNausea();
                sumFatigue += e.getFatigue();
                sumSatiety += e.getSatiety();
                count++;
            }
        }

        if (txtWeeklySummarySymptoms != null) {
            if (count == 0) {
                txtWeeklySummarySymptoms.setText("Sem registros");
            } else {
                float avgNausea = sumNausea / (float) count;
                float avgFatigue = sumFatigue / (float) count;
                float avgSatiety = sumSatiety / (float) count;

                String symText = String.format(
                        Locale.getDefault(),
                        "Médias: N:%.1f | F:%.1f | S:%.1f",
                        avgNausea,
                        avgFatigue,
                        avgSatiety
                );
                txtWeeklySummarySymptoms.setText(symText);
            }
        }

        if (txtWeeklySummaryWeight != null) {
            UserProfile profile = UserStorage.loadUserProfile(this);
            if (profile != null && profile.getCurrentWeight() > 0) {
                txtWeeklySummaryWeight.setText(String.format(Locale.getDefault(), "Atual: %.1f kg", profile.getCurrentWeight()));
            } else {
                txtWeeklySummaryWeight.setText("Sem peso");
            }
        }
    }

    private void exportarResumo() {
        StringBuilder sb = new StringBuilder();
        String dateNow = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new java.util.Date());

        sb.append("RELATÓRIO COMPLETO - OZEM+\n");
        sb.append("Gerado em: ").append(dateNow).append("\n");
        sb.append("================================\n\n");

        // 1. MEDICAMENTOS
        sb.append("[ MEDICAMENTOS ATUAIS ]\n");
        if (medications.isEmpty()) {
            sb.append("Nenhum medicamento cadastrado.\n");
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            for (Medication med : medications) {
                sb.append("• ").append(med.getName()).append("\n");
                sb.append("  Dose: ").append(med.getDose()).append(" | Freq: ").append(med.getFrequency()).append("\n");

                // Notas personalizadas do medicamento
                String notesKey = getNotesKeyForName(med.getName());
                String notes = prefs.getString(notesKey, "");
                if (!notes.isEmpty()) {
                    sb.append("  Nota: ").append(notes).append("\n");
                }
                sb.append("\n");
            }
        }
        sb.append("--------------------------------\n\n");

        // 2. HISTÓRICO DE PESO
        sb.append("[ EVOLUÇÃO DE PESO ]\n");
        List<WeightEntry> weights = WeightStorage.loadWeights(this);
        if (weights.isEmpty()) {
            sb.append("Nenhum registro de peso.\n");
        } else {
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (WeightEntry w : weights) {
                sb.append(sdfDate.format(new java.util.Date(w.getTimestamp())))
                        .append(" - ")
                        .append(String.format(Locale.getDefault(), "%.1f kg", w.getWeight()))
                        .append("\n");
            }
        }
        sb.append("\n--------------------------------\n\n");

        // 3. HISTÓRICO DE INJEÇÕES
        sb.append("[ REGISTRO DE INJEÇÕES ]\n");
        List<InjectionEntry> injections = InjectionStorage.loadInjections(this);
        if (injections.isEmpty()) {
            sb.append("Nenhuma injeção registrada.\n");
        } else {
            java.text.SimpleDateFormat sdfFull = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            for (InjectionEntry inj : injections) {
                sb.append(sdfFull.format(new java.util.Date(inj.getTimestamp())))
                        .append(" - ")
                        .append(inj.getMedicationName())
                        .append(" (Local: ").append(inj.getLocationCode()).append(")")
                        .append("\n");
            }
        }
        sb.append("\n--------------------------------\n\n");

        // 4. DIÁRIO DE SINTOMAS
        sb.append("[ DIÁRIO DE SINTOMAS ]\n");
        List<SymptomEntry> symptoms = SymptomStorage.loadSymptoms(this);
        if (symptoms.isEmpty()) {
            sb.append("Nenhum registro de sintomas.\n");
        } else {
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (SymptomEntry s : symptoms) {
                sb.append(sdfDate.format(new java.util.Date(s.getTimestamp()))).append(":\n");
                sb.append(String.format(Locale.getDefault(), "  Náusea: %d | Fadiga: %d | Saciedade: %d\n", s.getNausea(), s.getFatigue(), s.getSatiety()));
                if (s.getNotes() != null && !s.getNotes().trim().isEmpty()) {
                    sb.append("  Obs: ").append(s.getNotes().trim()).append("\n");
                }
                sb.append("\n");
            }
        }

        // Finalizar e Compartilhar
        String relatorioFinal = sb.toString();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Meu Relatório Ozem+");
        sendIntent.putExtra(Intent.EXTRA_TEXT, relatorioFinal);

        Intent chooser = Intent.createChooser(sendIntent, getString(R.string.export_share_title));
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, getString(R.string.export_share_no_app), Toast.LENGTH_SHORT).show();
        }
    }

    private String getNotesKeyForName(String name) {
        String keyName = (name == null) ? "" : name.trim().toLowerCase().replace(" ", "_");
        return "notes_" + keyName;
    }
}