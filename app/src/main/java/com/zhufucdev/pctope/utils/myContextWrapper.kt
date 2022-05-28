package com.zhufucdev.pctope.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

/**
 * Created by zhufu on 17-9-17.
 */
class myContextWrapper(base: Context?) : ContextWrapper(base) {
    val base = base@this
    var language : String? = null

    init {
        language = PreferenceManager.getDefaultSharedPreferences(base).getString("pref_language", "auto")
    }

    fun wrap() : ContextWrapper {
        val resources = base.resources
        val configuration = resources.configuration
        val metrics = resources.displayMetrics
        when (language) {
            "en" -> configuration.setLocale(Locale.ENGLISH)
            "ch" -> configuration.setLocale(Locale.SIMPLIFIED_CHINESE)
            else -> configuration.setLocale(SystemLanguage())
        }
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
            return ContextWrapper(base.createConfigurationContext(configuration))
        }
        else
            resources.updateConfiguration(configuration, metrics)
        return base
    }

    private fun SystemLanguage(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            resources.configuration.locales.get(0)
        else
            resources.configuration.locale
    }
}