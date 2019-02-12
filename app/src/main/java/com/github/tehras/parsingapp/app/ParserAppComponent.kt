package com.github.tehras.parsingapp.app

import android.app.Application
import com.github.tehras.base.dagger.components.MainComponent
import com.github.tehras.dagger.modules.AppModule
import com.github.tehras.dagger.scopes.ApplicationScope
import com.github.tehras.parsingapp.ui.recent.RecentDataComponentCreator
import dagger.BindsInstance
import dagger.Component

@Suppress("unused")
@ApplicationScope
@Component(
    modules = [
        AppModule::class
    ]
)
interface ParserAppComponent :
    MainComponent,
    UiComponentCreators {

    fun plusApplication(application: ParserApp)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): ParserAppComponent
    }
}

interface UiComponentCreators : RecentDataComponentCreator

