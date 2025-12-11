package com.etheralltda.ozem

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
// NOVOS IMPORTS (Auth em vez de GoTrue)
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SupabaseManager {
    // SUAS CREDENCIAIS
    private const val SUPABASE_URL = "https://rqpefsourqvpwakqlbzx.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJxcGVmc291cnF2cHdha3FsYnp4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxMjY4MDgsImV4cCI6MjA4MDcwMjgwOH0.Ohu7AdChXHknU2w_6bjdNkNwRKN_JQVFMCXcr_0W3s4"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) // Mudou de GoTrue para Auth
        install(Postgrest)
    }

    // Callback interfaces para chamar do Java
    interface AuthCallback {
        fun onSuccess()
        fun onError(message: String)
    }

    // Função de Signup
    fun signUp(email: String, pass: String, callback: AuthCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                }
                launch(Dispatchers.Main) { callback.onSuccess() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { callback.onError(e.message ?: "Erro desconhecido") }
            }
        }
    }

    // Função de Login
    fun signIn(email: String, pass: String, callback: AuthCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }
                launch(Dispatchers.Main) { callback.onSuccess() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { callback.onError("Erro ao entrar: ${e.message}") }
            }
        }
    }

    // Salvar perfil no Supabase (Apenas Pro)
    fun saveProfileToCloud(profile: UserProfile) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = client.auth.currentUserOrNull()
                if (user != null) {
                    // Vincula o ID do usuário autenticado ao perfil
                    profile.id = user.id
                    // Salva na tabela "profiles"
                    client.postgrest["profiles"].upsert(profile)
                    Log.d("Supabase", "Perfil salvo na nuvem!")
                }
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao salvar perfil: ${e.message}")
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return client.auth.currentUserOrNull() != null
    }

    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            client.auth.signOut()
        }
    }
}