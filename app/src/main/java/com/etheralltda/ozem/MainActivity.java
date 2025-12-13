package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICATIONS = 1001;
    private List<Medication> medications = new ArrayList<>();

    // Botões principais (Cards)
    private View btnCardMed, btnCardTips, btnCardReport;
    private Button btnInjection, btnJourney;
    private View btnWeight, btnSymptoms, btnDailyGoals;

    private TextView txtGreeting, txtPlan, txtDashboardWeight, txtDashboardWeightDiff;
    private TextView txtDashboardNextInjection, txtDashboardWeekly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    android.content.pm.PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
                md.update(signature.toByteArray());
                String currentSignature = android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT);
                android.util.Log.e("OzemAuth", "!!! SEU SHA-1 REAL (Base64) !!!: " + currentSignature);

                // Converte para Hexadecimal (formato do Google Cloud) para facilitar
                StringBuilder hexString = new StringBuilder();
                for (byte b : md.digest()) {
                    String hex = Integer.toHexString(0xFF & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                    hexString.append(":");
                }
                android.util.Log.e("OzemAuth", "!!! SEU SHA-1 REAL (HEX) !!!: " + hexString.toString().toUpperCase().substring(0, hexString.length()-1));
            }
        } catch (Exception e) {
            android.util.Log.e("OzemAuth", "Erro ao pegar SHA: " + e);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("AppConfig", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        pedirPermissaoNotificacaoSeNecessario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
        atualizarDashboard();
    }

    private void initViews() {
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
    }

    private void setupClickListeners() {
        btnCardMed.setOnClickListener(v -> startActivity(new Intent(this, MedicationListActivity.class)));
        btnCardReport.setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));
        btnWeight.setOnClickListener(v -> startActivity(new Intent(this, WeightActivity.class)));
        btnDailyGoals.setOnClickListener(v -> startActivity(new Intent(this, DailyGoalsActivity.class)));
        btnSymptoms.setOnClickListener(v -> startActivity(new Intent(this, SymptomsActivity.class)));

        // --- NOVO: Clique no Card de Plano (PRO/FREE) para abrir Perfil ---
        // Pega o MaterialCardView (pai do txtPlan) para aumentar a área do clique
        if (txtPlan.getParent() instanceof View) {
            ((View) txtPlan.getParent()).setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class))
            );
        } else {
            // Fallback caso a hierarquia mude
            txtPlan.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        // --- BLOQUEIO PRO: ROTINA ---
        btnCardTips.setOnClickListener(v -> {
            if (UserStorage.isPremium(this)) {
                startActivity(new Intent(this, RoutineActivity.class));
            } else {
                showProLockDialog();
            }
        });

        // --- BLOQUEIO PRO: JORNADA ---
        btnJourney.setOnClickListener(v -> {
            if (UserStorage.isPremium(this)) {
                startActivity(new Intent(this, JourneyGlp1Activity.class));
            } else {
                showProLockDialog();
            }
        });
    }

    private void showProLockDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Funcionalidade Pro")
                .setMessage("Crie uma conta ou faça login para acessar a Jornada e Rotinas Personalizadas e salvar seus dados na nuvem.")
                .setPositiveButton("Fazer Login / Criar Conta", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
        updateNextInjectionCard();
        updateProfileCard();
    }

    private void updateNextInjectionCard() {
        if (medications.isEmpty()) {
            btnInjection.setText(R.string.dashboard_btn_add_med);
            txtDashboardNextInjection.setText(R.string.dashboard_no_med);
            btnInjection.setOnClickListener(v -> startActivity(new Intent(this, ConfigMedicationActivity.class)));
        } else {
            btnInjection.setText(R.string.dashboard_btn_log_now);
            btnInjection.setOnClickListener(v -> startActivity(new Intent(this, InjectionActivity.class)));

            // Lógica para encontrar o horário mais próximo
            long now = System.currentTimeMillis();
            long closestTime = Long.MAX_VALUE;
            Medication nextMed = null;

            for (Medication med : medications) {
                long t = calculateNextTimestamp(med);
                if (t < closestTime) {
                    closestTime = t;
                    nextMed = med;
                }
            }

            if (nextMed != null) {
                String timeStr = getRelativeDateString(closestTime);
                txtDashboardNextInjection.setText(timeStr + " - " + nextMed.getName());
            } else {
                txtDashboardNextInjection.setText(R.string.dashboard_next_not_set);
            }
        }
    }

    // Calcula quando será a próxima dose
    private long calculateNextTimestamp(Medication med) {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();

        // Extrai hora "08:00"
        int hour = 8, minute = 0;
        if (med.getNextDate() != null && med.getNextDate().contains(":")) {
            String txt = med.getNextDate().trim();
            // Pega HH:mm do fim da string
            if(txt.length() >= 5) {
                String hm = txt.substring(txt.length() - 5);
                try {
                    String[] parts = hm.split(":");
                    hour = Integer.parseInt(parts[0]);
                    minute = Integer.parseInt(parts[1]);
                } catch(Exception e){}
            }
        }

        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        String freq = MedicationUtils.getLocalizedFrequency(this, med.getFrequency());
        String daily = getString(R.string.quiz_freq_daily);
        String weekly = getString(R.string.quiz_freq_weekly);

        if (freq.equals(daily)) {
            if (c.getTimeInMillis() <= now) c.add(Calendar.DAY_OF_YEAR, 1);
        } else if (freq.equals(weekly)) {
            c.set(Calendar.DAY_OF_WEEK, med.getDayOfWeek());
            if (c.getTimeInMillis() <= now) c.add(Calendar.WEEK_OF_YEAR, 1);
        } else {
            // Mensal (Simplificado: adiciona 1 mês se já passou)
            if (c.getTimeInMillis() <= now) c.add(Calendar.MONTH, 1);
        }

        return c.getTimeInMillis();
    }

    private String getRelativeDateString(long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);

        String time = String.format(Locale.getDefault(), "%02d:%02d",
                target.get(Calendar.HOUR_OF_DAY), target.get(Calendar.MINUTE));

        // Se for outro dia, mostra "Segunda, 08:00" (traduzido)
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, HH:mm", Locale.getDefault());
        String date = sdf.format(new Date(timestamp));
        return date.substring(0, 1).toUpperCase() + date.substring(1);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void updateProfileCard() {
        UserProfile profile = UserStorage.loadUserProfile(this);
        if (profile != null) {
            String name = profile.getName();
            txtGreeting.setText((name == null || name.isEmpty()) ? getString(R.string.app_greeting_hello) : getString(R.string.app_greeting_format, name));
            txtPlan.setText(UserStorage.isPremium(this) ? getString(R.string.plan_pro) : getString(R.string.plan_free));

            float cw = profile.getCurrentWeight();
            if (cw > 0) {
                txtDashboardWeight.setText(String.format(Locale.getDefault(), "%.1f kg", cw));
                List<WeightEntry> history = WeightStorage.loadWeights(this);
                if (!history.isEmpty()) {
                    float initial = history.get(0).getWeight();
                    float diff = cw - initial;
                    if (Math.abs(diff) > 0.1) {
                        txtDashboardWeightDiff.setVisibility(View.VISIBLE);
                        String diffStr = String.format(Locale.getDefault(), "%.1f kg", Math.abs(diff));
                        if (diff > 0) {
                            txtDashboardWeightDiff.setText("+" + diffStr);
                            txtDashboardWeightDiff.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
                        } else {
                            txtDashboardWeightDiff.setText("-" + diffStr);
                            txtDashboardWeightDiff.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
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
            txtGreeting.setText(R.string.app_greeting_hello);
            txtPlan.setText(R.string.plan_free);
            txtDashboardWeight.setText("-- kg");
            txtDashboardWeightDiff.setVisibility(View.GONE);
        }
    }
}