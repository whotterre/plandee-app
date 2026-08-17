package com.example.plandee.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences

    companion object {
        private const val PREF_NAME = "plandee_secure_prefs"
        private const val FALLBACK_PREF_NAME = "plandee_standard_prefs"
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
            context.getSharedPreferences(FALLBACK_PREF_NAME, Context.MODE_PRIVATE)
        }

        try {
            if (!prefs.contains(KEY_TOKENS) || prefs.getInt(KEY_TOKENS, 0) < DEFAULT_TOKENS) {
                saveTokens(DEFAULT_TOKENS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveTokens(tokens: Int) {
        try {
            prefs.edit().putInt(KEY_TOKENS, tokens).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTokens(): Int {
        return try {
            val count = prefs.getInt(KEY_TOKENS, DEFAULT_TOKENS)
            if (count < 10 && !prefs.contains(KEY_TOKENS)) 10 else count
        } catch (e: Exception) {
            DEFAULT_TOKENS
        }
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
        try {
            prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAuthToken(): String? {
        return try {
            prefs.getString(KEY_JWT_TOKEN, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveUserId(userId: String) {
        try {
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUserId(): String {
        return try {
            prefs.getString(KEY_USER_ID, "user_demo_uuid") ?: "user_demo_uuid"
        } catch (e: Exception) {
            "user_demo_uuid"
        }
    }

    fun saveUserEmail(email: String) {
        try {
            prefs.edit().putString(KEY_USER_EMAIL, email).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUserEmail(): String? {
        return try {
            prefs.getString(KEY_USER_EMAIL, null)
        } catch (e: Exception) {
            null
        }
    }

    fun clearSession() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }
}
