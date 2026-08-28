package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * ذخیره‌سازی امن نشست کاربر با EncryptedSharedPreferences (AES256-GCM / AES256-SIV)
 * برای حفظ نشست کاربر تا زمان خروج دستی (Logout)
 */
class EncryptedSessionStorage(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context.applicationContext,
            "ghadir_secure_session_vault",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // ایجاد حافظه پشتیبان استاندارد در صورت عدم پشتیبانی سخت‌افزاری KeyStore یا محیط‌های تست
        context.applicationContext.getSharedPreferences("ghadir_secure_session_vault_fallback", Context.MODE_PRIVATE)
    }

    fun saveUserSession(userId: Long, username: String, role: String, token: String = "SESSION_VALID") {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_ROLE, role)
            .putString(KEY_TOKEN, token)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getSavedUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    fun getSavedUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getSavedUserId() > 0
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_ROLE)
            .remove(KEY_TOKEN)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    companion object {
        private const val KEY_USER_ID = "enc_saved_user_id"
        private const val KEY_USERNAME = "enc_saved_username"
        private const val KEY_ROLE = "enc_saved_role"
        private const val KEY_TOKEN = "enc_session_token"
        private const val KEY_IS_LOGGED_IN = "enc_is_logged_in"
        private const val KEY_TIMESTAMP = "enc_login_timestamp"
    }
}
