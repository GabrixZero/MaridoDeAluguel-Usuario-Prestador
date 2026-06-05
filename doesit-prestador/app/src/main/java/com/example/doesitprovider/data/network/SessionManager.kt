package com.example.doesitprovider.data.network

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SessionManager {
    private const val PREF_NAME        = "secure_session_provider"
    private const val KEY_TOKEN        = "token"
    private const val KEY_USER_ID      = "userId"
    private const val KEY_USER_NAME    = "userName"
    private const val KEY_USER_EMAIL   = "userEmail"
    private const val KEY_USER_PHONE   = "userPhone"
    private const val KEY_USER_CPF     = "userCpf"
    private const val KEY_USER_BIRTH   = "userBirthDate"
    private const val KEY_USER_GENDER  = "userGender"
    private const val KEY_RATING       = "rating"
    private const val KEY_RATING_COUNT = "ratingCount"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        prefs = EncryptedSharedPreferences.create(
            PREF_NAME, masterKeyAlias, context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        _rating      = prefs?.getFloat(KEY_RATING, 0f)?.toDouble() ?: 0.0
        _ratingCount = prefs?.getInt(KEY_RATING_COUNT, 0) ?: 0
    }

    // ── Campos persistidos ────────────────────────────────────────────────────

    var token: String
        get() = prefs?.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_TOKEN, value)?.apply() ?: Unit

    var userId: Long
        get() = prefs?.getLong(KEY_USER_ID, 0L) ?: 0L
        set(value) = prefs?.edit()?.putLong(KEY_USER_ID, value)?.apply() ?: Unit

    var userName: String
        get() = prefs?.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_NAME, value)?.apply() ?: Unit

    var userEmail: String
        get() = prefs?.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_EMAIL, value)?.apply() ?: Unit

    var userPhone: String
        get() = prefs?.getString(KEY_USER_PHONE, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_PHONE, value)?.apply() ?: Unit

    var userCpf: String
        get() = prefs?.getString(KEY_USER_CPF, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_CPF, value)?.apply() ?: Unit

    var userBirthDate: String
        get() = prefs?.getString(KEY_USER_BIRTH, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_BIRTH, value)?.apply() ?: Unit

    var userGender: String
        get() = prefs?.getString(KEY_USER_GENDER, "") ?: ""
        set(value) = prefs?.edit()?.putString(KEY_USER_GENDER, value)?.apply() ?: Unit

    // ── Preferências de notificação — salvas por conta (chave inclui userId) ─
    // Isso garante que cada conta tenha suas próprias configurações independentes.

    var notifPushEnabled: Boolean
        get() = prefs?.getBoolean("notifPush_$userId", true) ?: true
        set(value) = prefs?.edit()?.putBoolean("notifPush_$userId", value)?.apply() ?: Unit

    var notifEmailEnabled: Boolean
        get() = prefs?.getBoolean("notifEmail_$userId", true) ?: true
        set(value) = prefs?.edit()?.putBoolean("notifEmail_$userId", value)?.apply() ?: Unit

    var notifSmsEnabled: Boolean
        get() = prefs?.getBoolean("notifSms_$userId", false) ?: false
        set(value) = prefs?.edit()?.putBoolean("notifSms_$userId", value)?.apply() ?: Unit

    var notifWhatsappEnabled: Boolean
        get() = prefs?.getBoolean("notifWhatsapp_$userId", false) ?: false
        set(value) = prefs?.edit()?.putBoolean("notifWhatsapp_$userId", value)?.apply() ?: Unit

    // ── Campos reativos (Compose) ─────────────────────────────────────────────

    private var _rating by mutableStateOf(0.0)
    var rating: Double
        get() = _rating
        set(value) { _rating = value; prefs?.edit()?.putFloat(KEY_RATING, value.toFloat())?.apply() }

    private var _ratingCount by mutableStateOf(0)
    var ratingCount: Int
        get() = _ratingCount
        set(value) { _ratingCount = value; prefs?.edit()?.putInt(KEY_RATING_COUNT, value)?.apply() }

    // Estado online — apenas em memória (reset correto ao reiniciar)
    var isOnline by mutableStateOf(false)

    // Access token Cognito — apenas em memória (usado para GlobalSignOut)
    var accessToken: String = ""

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun isLoggedIn() = token.isNotEmpty()
    fun bearerToken() = "Bearer $token"

    fun save(
        token: String, id: Long, name: String, email: String,
        phone: String = "", cpf: String = "", birthDate: String = "",
        gender: String = "", rating: Double = 0.0, ratingCount: Int = 0
    ) {
        this.token = token; this.userId = id; this.userName = name
        this.userEmail = email; this.userPhone = phone; this.userCpf = cpf
        this.userBirthDate = birthDate; this.userGender = gender
        this.rating = rating; this.ratingCount = ratingCount
    }

    fun clear() {
        prefs?.edit()?.apply {
            remove(KEY_TOKEN); remove(KEY_USER_ID); remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL); remove(KEY_USER_PHONE); remove(KEY_USER_CPF)
            remove(KEY_USER_BIRTH); remove(KEY_USER_GENDER)
            remove(KEY_RATING); remove(KEY_RATING_COUNT)
            // Preferências de notificação são mantidas intencionalmente por conta
            // (identificadas por userId, continuam válidas para futuros logins)
        }?.apply()
        _rating = 0.0; _ratingCount = 0; isOnline = false; accessToken = ""
    }
}