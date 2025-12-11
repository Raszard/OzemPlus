package com.etheralltda.ozem;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnSignUp.setOnClickListener(v -> handleSignUp());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString();
        String pass = etPassword.getText().toString();
        if(email.isEmpty() || pass.isEmpty()) return;

        SupabaseManager.INSTANCE.signIn(email, pass, new SupabaseManager.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this, "Bem-vindo de volta!", Toast.LENGTH_SHORT).show();
                enableProMode();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, "Erro: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSignUp() {
        String email = etEmail.getText().toString();
        String pass = etPassword.getText().toString();
        if(email.isEmpty() || pass.isEmpty()) return;

        SupabaseManager.INSTANCE.signUp(email, pass, new SupabaseManager.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this, "Conta criada! Você agora é Pro.", Toast.LENGTH_SHORT).show();
                enableProMode();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, "Erro: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableProMode() {
        // Ativa o modo Premium localmente
        UserStorage.setPremium(this, true);
        finish();
    }
}