package com.vn.myinvestmentcoin

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.multidex.MultiDexApplication
import com.vn.custom.util.GeneralUtil
import java.util.*

class MIApplication : MultiDexApplication() {
    init {
        instance = this
    }

    companion object {
        var instance: MIApplication? = null
        lateinit var appContext: Context

        fun applicationContext(): MIApplication {
            return instance as MIApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        WebView(this).destroy()
        appContext = applicationContext
        getConfigLocale(this)
    }

    fun getConfigLocale(base: Context): Context {
        var currentLanguage = GeneralUtil.getCurrentLanguage(this).locale
        val locale = Locale(currentLanguage)
        Locale.setDefault(locale)
        val config = base.resources.configuration
        config.setLocale(locale)
        return createConfigurationContext(config)
    }

}