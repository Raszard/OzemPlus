package com.etheralltda.ozem;

import android.content.Intent;
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

        // Verifica o modo noturno atual do sistema ou do app
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        // Se ainda não foi definido pelo app, verifica o sistema
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            int systemMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switchDarkMode.setChecked(systemMode == Configuration.UI_MODE_NIGHT_YES);
        } else {
            switchDarkMode.setChecked(currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Mostra idioma atual
        txtCurrentLanguage.setText(Locale.getDefault().getDisplayName());

        // Botão Voltar
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupSettings() {
        // --- TEMA NOTURNO ---
        // O Switch força a mudança do tema imediatamente.
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Nota: Em um app real, você salvaria essa preferência em SharedPreferences
            // e a carregaria na classe Application para persistir após reiniciar o app.
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
            // Exemplo: limpa dados de sessão
            UserStorage.setPremium(this, false);
            // Se usar Supabase ou Firebase, faça o signout aqui.

            Toast.makeText(this, "Desconectado.", Toast.LENGTH_SHORT).show();

            // Redireciona para o Login e limpa a pilha de activities para não poder voltar
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupDataManagement() {
        // A lógica aqui carrega a lista, remove o último item e salva novamente.

        // --- REMOVER ÚLTIMO PESO ---
        findViewById(R.id.btnUndoWeight).setOnClickListener(v -> {
            List<WeightEntry> list = WeightStorage.loadWeights(this);
            if (!list.isEmpty()) {
                list.remove(list.size() - 1); // Remove o último
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
                    // A activity será recriada automaticamente para aplicar o idioma
                })
                .show();
    }
}