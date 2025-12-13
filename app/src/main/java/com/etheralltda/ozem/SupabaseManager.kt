package com.etheralltda.ozem

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
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
        install(Auth)
        install(Postgrest)
    }

    interface AuthCallback {
        fun onSuccess()
        fun onError(message: String)
    }

    // Login com Email (Mantido)
    fun signIn(email: String, pass: String, callback: AuthCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }
                launch(Dispatchers.Main) { callback.onSuccess() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { callback.onError("Erro: ${e.message}") }
            }
        }
    }

    // SignUp com Email (Mantido)
    fun signUp(email: String, pass: String, callback: AuthCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                }
                launch(Dispatchers.Main) { callback.onSuccess() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { callback.onError("Erro: ${e.message}") }
            }
        }
    }

    // --- LOGIN COM GOOGLE ---
    fun signInWithGoogle(token: String, callback: AuthCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signInWith(IDToken) {
                    this.idToken = token
                    this.provider = Google
                }
                launch(Dispatchers.Main) { callback.onSuccess() }
            } catch (e: Exception) {
                Log.e("Supabase", "Erro Google: ${e.message}")
                launch(Dispatchers.Main) { callback.onError("Erro Google: ${e.message}") }
            }
        }
    }

    // --- BUSCAR PERFIL DO USUÁRIO ---
    // Importante para saber se é Pro ou Free após logar
    fun getUserProfile(callback: (UserProfile?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = client.auth.currentUserOrNull()
                if (user != null) {
                    // Busca na tabela 'profiles' onde id == user.id
                    val list = client.postgrest["profiles"]
                        .select {
                            filter {
                                eq("id", user.id)
                            }
                        }.decodeList<UserProfile>()

                    val profile = list.firstOrNull()
                    launch(Dispatchers.Main) { callback(profile) }
                } else {
                    launch(Dispatchers.Main) { callback(null) }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao buscar perfil: ${e.message}")
                launch(Dispatchers.Main) { callback(null) }
            }
        }
    }

    // --- SALVAR PERFIL (Pro ou Free) ---
    fun saveProfileToCloud(profile: UserProfile) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = client.auth.currentUserOrNull()
                if (user != null) {
                    // Garante que o ID do perfil seja o mesmo do usuário logado
                    profile.id = user.id

                    // Upsert: Atualiza se existir, Cria se não existir
                    client.postgrest["profiles"].upsert(profile)
                    Log.d("Supabase", "Perfil salvo: Premium=${profile.isPremium}")
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
            try {
                client.auth.signOut()
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao sair: ${e.message}")
            }
        }
    }
}