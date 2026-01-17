package org.gua.imper

import android.content.Context

object SettingsManager {

    private const val PREFS_NAME = "AppSettingsPrefs"
    private const val KEY_CUSTOM_API_KEY = "custom_api_key"
    private const val KEY_USE_CUSTOM_API_KEY = "use_custom_api_key"

    fun saveApiKey(context: Context, apiKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_API_KEY, apiKey).apply()
    }

    fun getApiKey(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_API_KEY, null)
    }

    fun setUseCustomApiKey(context: Context, useCustom: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USE_CUSTOM_API_KEY, useCustom).apply()
    }

    fun shouldUseCustomApiKey(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_CUSTOM_API_KEY, false)
    }
}