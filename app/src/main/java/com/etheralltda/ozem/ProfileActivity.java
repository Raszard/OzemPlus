package com.etheralltda.ozem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log; // Importante para debug
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.Locale;

import kotlin.Unit;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "OzemDebug"; // Tag para filtrar no Logcat
    // IMPORTANTE: Verifique se este é o WEB CLIENT ID do Google Cloud
    private static final String WEB_CLIENT_ID = "client_secret_797194641445-r53vsquj79hmfgdo91laq94327emnm4i.apps.googleusercontent.com";

    private SwitchMaterial switchDarkMode;
    private TextView txtCurrentLanguage;
    private TextView btnLogout, btnLoginGoogle;
    private Button btnGetPro; // Novo botão

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupGoogleSignIn();
        initViews();
        setupSettings();
        setupDataManagement();

        // Atualiza a interface ao abrir a tela
        updateAccountUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garante que a UI esteja atualizada se voltar de outra tela
        updateAccountUI();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "Google SignIn Result Code: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    } else {
                        Log.e(TAG, "Google SignIn cancelado ou falhou na Activity.");
                        Toast.makeText(this, "Login cancelado.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if (idToken != null) {
                Log.d(TAG, "Google ID Token obtido com sucesso. Autenticando no Supabase...");
                Toast.makeText(this, "Conectando...", Toast.LENGTH_SHORT).show();

                SupabaseManager.INSTANCE.signInWithGoogle(idToken, new SupabaseManager.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Login Supabase bem-sucedido! Buscando perfil...");
                        // 1. Login OK. Busca o perfil na nuvem.
                        SupabaseManager.INSTANCE.getUserProfile(profile -> {
                            if (profile != null) {
                                Log.d(TAG, "Perfil encontrado na nuvem. Sincronizando. Premium: " + profile.isPremium());
                                UserStorage.saveUserProfile(ProfileActivity.this, profile);
                            } else {
                                Log.d(TAG, "Nenhum perfil encontrado. Criando perfil padrão (Free).");
                                UserProfile newProfile = UserStorage.loadUserProfile(ProfileActivity.this);
                                if (newProfile == null) newProfile = new UserProfile();
                                // Garante que salva no banco
                                UserStorage.saveUserProfile(ProfileActivity.this, newProfile);
                            }

                            // 2. Força a atualização da tela na Thread principal
                            runOnUiThread(() -> {
                                updateAccountUI();
                                String status = UserStorage.isPremium(ProfileActivity.this) ? "Pro" : "Free";
                                Toast.makeText(ProfileActivity.this, "Bem-vindo! Conta " + status, Toast.LENGTH_SHORT).show();
                            });
                            return Unit.INSTANCE;
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Erro no login Supabase: " + message);
                        Toast.makeText(ProfileActivity.this, "Erro ao conectar: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Log.e(TAG, "Google ID Token veio NULO.");
                Toast.makeText(this, "Falha ao obter credenciais do Google.", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            // O código de status aqui é crucial para entender o erro do lado do Google
            Log.e(TAG, "Falha na Task do Google SignIn. StatusCode: " + e.getStatusCode(), e);
            Toast.makeText(this, "Falha Google (Cód: " + e.getStatusCode() + ")", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        switchDarkMode = findViewById(R.id.switchDarkMode);
        txtCurrentLanguage = findViewById(R.id.txtCurrentLanguage);
        btnLogout = findViewById(R.id.btnLogout);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        btnGetPro = findViewById(R.id.btnGetPro); // Inicializa o novo botão

        // ... (código de tema e idioma mantido) ...
        SharedPreferences sharedPreferences = getSharedPreferences("AppConfig", MODE_PRIVATE);
        boolean isDarkMode;
        if (sharedPreferences.contains("dark_mode")) {
            isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        } else {
            int systemMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            isDarkMode = (systemMode == Configuration.UI_MODE_NIGHT_YES);
        }
        switchDarkMode.setChecked(isDarkMode);
        txtCurrentLanguage.setText(Locale.getDefault().getDisplayName());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // --- LÓGICA CENTRAL DE ATUALIZAÇÃO DA UI ---
    private void updateAccountUI() {
        boolean isLoggedIn = SupabaseManager.INSTANCE.isUserLoggedIn();
        boolean isPremium = UserStorage.isPremium(this);

        Log.d(TAG, "Atualizando UI -> Logado: " + isLoggedIn + ", Premium: " + isPremium);

        if (isLoggedIn) {
            // LOGADO: Mostra SAIR, Esconde ENTRAR
            btnLogout.setVisibility(View.VISIBLE);
            btnLoginGoogle.setVisibility(View.GONE);
        } else {
            // DESLOGADO: Esconde SAIR, Mostra ENTRAR
            btnLogout.setVisibility(View.GONE);
            btnLoginGoogle.setVisibility(View.VISIBLE);
        }

        // BOTÃO PRO: Mostra se NÃO for premium (estando logado ou não)
        if (!isPremium) {
            btnGetPro.setVisibility(View.VISIBLE);
        } else {
            btnGetPro.setVisibility(View.GONE);
        }
    }

    private void setupSettings() {
        // ... (listeners de tema, idioma e sobre mantidos) ...
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences sharedPreferences = getSharedPreferences("AppConfig", MODE_PRIVATE);
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
        findViewById(R.id.btnChangeLanguage).setOnClickListener(v -> showLanguageDialog());
        findViewById(R.id.btnAbout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sobre o Ozem+")
                    .setMessage("Versão 1.0.0\nDesenvolvido para auxiliar sua jornada GLP-1.")
                    .setPositiveButton("OK", null)
                    .show();
        });


        // --- BOTÃO OBTER PRO ---
        btnGetPro.setOnClickListener(v -> {
            showPremiumUpgradeDialog();
        });

        // --- SAIR / LOGOUT ---
        btnLogout.setOnClickListener(v -> {
            SupabaseManager.INSTANCE.logout();
            if (googleSignInClient != null) googleSignInClient.signOut();

            // Opcional: Resetar para Free localmente ao sair para evitar uso indevido
            // Se quiser manter o status mesmo deslogado, comente a linha abaixo.
            // UserStorage.setPremium(this, false);

            Toast.makeText(this, "Desconectado.", Toast.LENGTH_SHORT).show();
            updateAccountUI();
        });

        // --- ENTRAR COM GOOGLE ---
        btnLoginGoogle.setOnClickListener(v -> {
            Log.d(TAG, "Iniciando fluxo de login Google...");
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // --- SIMULAÇÃO DO FLUXO DE COMPRA ---
    public void showPremiumUpgradeDialog() {
        // Aqui você integraria com o Google Play Billing no futuro.
        // Por enquanto, simulamos a compra.
        new AlertDialog.Builder(this)
                .setTitle("Desbloquear Ozem+ Pro")
                .setMessage("Tenha acesso a Rotinas personalizadas, Jornada detalhada e backup na nuvem.\n\n(Simulação de Compra)")
                .setPositiveButton("Comprar Agora (Simular)", (dialog, which) -> {
                    // 1. Define como Premium
                    UserStorage.setPremium(this, true);

                    // 2. Tenta salvar na nuvem se estiver logado
                    if (SupabaseManager.INSTANCE.isUserLoggedIn()) {
                        UserProfile profile = UserStorage.loadUserProfile(this);
                        SupabaseManager.INSTANCE.saveProfileToCloud(profile);
                    }

                    // 3. Atualiza a tela (o botão Pro vai sumir)
                    updateAccountUI();
                    Toast.makeText(this, "Parabéns! Você agora é PRO.", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupDataManagement() {
        // ... (código dos botões de desfazer mantido) ...
        findViewById(R.id.btnUndoWeight).setOnClickListener(v -> {
            List<WeightEntry> list = WeightStorage.loadWeights(this);
            if (!list.isEmpty()) {
                list.remove(list.size() - 1);
                WeightStorage.saveWeights(this, list);
                Toast.makeText(this, "Último registro de peso removido.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Histórico vazio.", Toast.LENGTH_SHORT).show();
            }
        });
        // (repetir para Injection e Symptom conforme seu código original)
    }

    private void showLanguageDialog() {
        // ... (código do dialog de idioma mantido) ...
        String[] languages = {"Português", "English", "Español"};
        String[] codes = {"pt", "en", "es"};
        new AlertDialog.Builder(this)
                .setTitle("Selecione o Idioma")
                .setItems(languages, (dialog, which) -> {
                    LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(codes[which]);
                    AppCompatDelegate.setApplicationLocales(appLocale);
                    txtCurrentLanguage.setText(languages[which]);
                })
                .show();
    }
}