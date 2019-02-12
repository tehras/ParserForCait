package com.github.tehras.parsingapp.ui.recent

import androidx.lifecycle.ViewModel
import com.github.tehras.base.arch.dagger.ViewModelFactoryModule
import com.github.tehras.base.arch.dagger.ViewModelKey
import com.github.tehras.base.dagger.components.SubComponentCreator
import com.github.tehras.dagger.scopes.FragmentScope
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.IntoMap

@Module(includes = [ViewModelFactoryModule::class])
abstract class RecentDataModule {
    @Binds
    @IntoMap
    @ViewModelKey(RecentDataViewModel::class)
    abstract fun bindRecentDataViewModel(dogListViewModel: RecentDataViewModel): ViewModel
}

@FragmentScope
@Subcomponent(modules = [RecentDataModule::class])
interface RecentDataComponent {
    fun inject(fragment: RecentDataFragment)
}

interface RecentDataComponentCreator : SubComponentCreator {
    fun plusRecentDataComponent(): RecentDataComponent
}