/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.fridge.category

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.pyamsoft.fridge.core.ViewModelFactoryModule
import com.pyamsoft.fridge.ui.ThemeProviderModule
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Subcomponent(
    modules =
        [
            CategoryComponent.ComponentModule::class,
            ThemeProviderModule::class,
            ViewModelFactoryModule::class])
internal interface CategoryComponent {

  fun inject(fragment: CategoryFragment)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance appBarActivity: AppBarActivity,
        @BindsInstance activity: Activity,
        @BindsInstance parent: ViewGroup,
        @BindsInstance owner: LifecycleOwner
    ): CategoryComponent
  }

  @Module
  abstract class ComponentModule {

    @Binds
    @IntoMap
    @ClassKey(CategoryViewModel::class)
    internal abstract fun bindViewModel(impl: CategoryViewModel): ViewModel
  }
}
