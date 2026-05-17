package com.kimfamily.ledger.data

import android.content.Context
import com.kimfamily.ledger.R

class AppPreferences(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun getAppName(): String =
        preferences.getString(KEY_APP_NAME, null).orEmpty().ifBlank {
            appContext.getString(R.string.app_name)
        }

    fun setAppName(name: String) {
        preferences.edit()
            .putString(KEY_APP_NAME, name.trim())
            .apply()
    }

    companion object {
        private const val KEY_APP_NAME = "app_name"
    }
}
