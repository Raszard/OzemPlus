package com.etheralltda.ozem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private SwitchMaterial switchDarkMode;
    private TextView txtCurrentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupSettings();
        setupDataManagement();
    }

    private void initViews() {
        switchDarkMode = findViewById(R.id.switchDarkMode);
        txtCurrentLanguage = findViewById(R.id.txtCurrentLanguage);

        // --- CORREÇÃO: Carregar preferência salva ---
        SharedPreferences sharedPreferences = getSharedPreferences("AppConfig", MODE_PRIVATE);
        boolean isDarkMode;

        // Se o usuário já escolheu antes, usa a preferência dele
        if (sharedPreferences.contains("dark_mode")) {
            isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        } else {
            // Se nunca escolheu, segue o padrão do sistema Android
            int systemMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            isDarkMode = (systemMode == Configuration.UI_MODE_NIGHT_YES);
        }

        // Ajusta o switch visualmente sem disparar o listener agora
        switchDarkMode.setChecked(isDarkMode);

        // Mostra idioma atual
        txtCurrentLanguage.setText(Locale.getDefault().getDisplayName());

        // Botão Voltar
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupSettings() {
        // --- TEMA NOTURNO ---
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 1. Salvar a preferência no armazenamento do celular
            SharedPreferences sharedPreferences = getSharedPreferences("AppConfig", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            // 2. Aplicar o tema em todo o app imediatamente
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // --- IDIOMA ---
        findViewById(R.id.btnChangeLanguage).setOnClickListener(v -> showLanguageDialog());

        // --- SOBRE ---
        findViewById(R.id.btnAbout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sobre o Ozem+")
                    .setMessage("Versão 1.0.0\nDesenvolvido para auxiliar sua jornada GLP-1.\n\nEste app não substitui orientação médica.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        // --- SAIR / LOGOUT ---
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // Limpa dados de sessão se necessário
            UserStorage.setPremium(this, false);

            Toast.makeText(this, "Desconectado.", Toast.LENGTH_SHORT).show();

            // Redireciona para o Login e limpa a pilha de activities
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupDataManagement() {
        // --- REMOVER ÚLTIMO PESO ---
        findViewById(R.id.btnUndoWeight).setOnClickListener(v -> {
            List<WeightEntry> list = WeightStorage.loadWeights(this);
            if (!list.isEmpty()) {
                list.remove(list.size() - 1);
                WeightStorage.saveWeights(this, list);
                Toast.makeText(this, "Último registro de peso removido.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Histórico de peso já está vazio.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- REMOVER ÚLTIMA INJEÇÃO ---
        findViewById(R.id.btnUndoInjection).setOnClickListener(v -> {
            List<InjectionEntry> list = InjectionStorage.loadInjections(this);
            if (!list.isEmpty()) {
                list.remove(list.size() - 1);
                InjectionStorage.saveInjections(this, list);
                Toast.makeText(this, "Última injeção removida.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Histórico de injeções vazio.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- REMOVER ÚLTIMO SINTOMA ---
        findViewById(R.id.btnUndoSymptom).setOnClickListener(v -> {
            List<SymptomEntry> list = SymptomStorage.loadSymptoms(this);
            if (!list.isEmpty()) {
                list.remove(list.size() - 1);
                SymptomStorage.saveSymptoms(this, list);
                Toast.makeText(this, "Último registro de sintomas removido.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Histórico de sintomas vazio.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLanguageDialog() {
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