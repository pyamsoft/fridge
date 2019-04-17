/*
 * Copyright 2019 Peter Kenji Yamanaka
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
 *
 */

package com.pyamsoft.fridge.main

import android.view.ViewGroup
import androidx.annotation.CheckResult
import com.pyamsoft.fridge.main.MainComponent.MainProvider
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@MainScope
@Subcomponent(modules = [MainProvider::class, MainModule::class])
internal interface MainComponent {

  fun inject(activity: MainActivity)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
      @BindsInstance parent: ViewGroup,
      @BindsInstance provider: ToolbarActivityProvider
    ): MainComponent
  }

  @Module
  object MainProvider {

    @Provides
    @JvmStatic
    fun provideDropshadow(parent: ViewGroup): DropshadowView {
      return DropshadowView(parent)
    }

  }

}
