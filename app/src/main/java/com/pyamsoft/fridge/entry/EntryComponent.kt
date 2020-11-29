/*
 * Copyright 2020 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.fridge.entry

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.pyamsoft.fridge.ThemeProviderModule
import com.pyamsoft.fridge.core.FridgeViewModelFactory
import com.pyamsoft.fridge.entry.EntryComponent.ViewModelModule
import com.pyamsoft.fridge.tooltip.TooltipModule
import com.pyamsoft.pydroid.arch.UiViewModel
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Subcomponent(modules = [ViewModelModule::class, EntryListModule::class, ThemeProviderModule::class, TooltipModule::class])
internal interface EntryComponent {

    fun inject(fragment: EntryFragment)

    @Subcomponent.Factory
    interface Factory {

        @CheckResult
        fun create(
            @BindsInstance activity: Activity,
            @BindsInstance owner: LifecycleOwner,
            @BindsInstance parent: ViewGroup
        ): EntryComponent
    }

    @Module
    abstract class ViewModelModule {

        @Binds
        internal abstract fun bindViewModelFactory(factory: FridgeViewModelFactory): ViewModelProvider.Factory

        @Binds
        @IntoMap
        @ClassKey(EntryAddViewModel::class)
        internal abstract fun entryAddViewModel(viewModel: EntryAddViewModel): UiViewModel<*, *, *>

        @Binds
        @IntoMap
        @ClassKey(EntryListViewModel::class)
        internal abstract fun entryListViewModel(viewModel: EntryListViewModel): UiViewModel<*, *, *>
    }
}
