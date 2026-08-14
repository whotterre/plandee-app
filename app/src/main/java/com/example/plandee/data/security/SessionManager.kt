package com.example.plandee.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences

    companion object {
        private const val PREF_NAME = "plandee_secure_prefs"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_TOKENS = "user_tokens"
        private const val DEFAULT_TOKENS = 10

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        prefs = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        if (!prefs.contains(KEY_TOKENS) || prefs.getInt(KEY_TOKENS, 0) < DEFAULT_TOKENS) {
            saveTokens(DEFAULT_TOKENS)
        }
    }

    fun saveTokens(tokens: Int) {
        prefs.edit().putInt(KEY_TOKENS, tokens).apply()
    }

    fun getTokens(): Int {
        val count = prefs.getInt(KEY_TOKENS, DEFAULT_TOKENS)
        return if (count < 10 && !prefs.contains(KEY_TOKENS)) 10 else count
    }

    fun consumeToken(): Boolean {
        val current = getTokens()
        return if (current > 0) {
            saveTokens(current - 1)
            true
        } else {
            false
        }
    }

    fun addTokens(count: Int) {
        saveTokens(getTokens() + count)
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String {
        return prefs.getString(KEY_USER_ID, "user_demo_uuid") ?: "user_demo_uuid"
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
}
