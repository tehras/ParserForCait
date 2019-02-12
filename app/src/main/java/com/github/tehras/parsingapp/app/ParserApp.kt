package com.github.tehras.parsingapp.app

import android.app.Application
import android.os.StrictMode
import com.github.tehras.base.dagger.components.ComponentProvider
import com.github.tehras.base.dagger.components.DaggerApplication
import com.github.tehras.base.log.CrashReportingTree
import com.github.tehras.parsingapp.BuildConfig
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import timber.log.Timber

class ParserApp : Application(), DaggerApplication, ComponentProvider<ParserAppComponent> {

    private lateinit var appComponent: ParserAppComponent

    override fun getComponent(): ParserAppComponent {
        return appComponent
    }
    override fun onCreate() {
        appComponent = DaggerParserAppComponent
            .builder()
            .application(this)
            .build()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        } else {
            Timber.plant(CrashReportingTree())

            AppCenter.start(
                this,
                "f9ee8608-5fa4-4b7b-baf2-58999dca1911",
                Analytics::class.java, Crashes::class.java
            )
        }

        appComponent.plusApplication(this)

        super.onCreate()
    }
}