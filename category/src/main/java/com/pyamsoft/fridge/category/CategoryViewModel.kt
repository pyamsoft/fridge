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

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.UnitControllerEvent
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class CategoryViewModel
@Inject
internal constructor(
    private val interactor: CategoryInteractor,
) :
    UiViewModel<CategoryViewState, UnitControllerEvent>(
        CategoryViewState(categories = emptyList())) {

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      interactor
          .loadCategories()
          .onSuccess { setState { copy(categories = it) } }
          .onFailure { Timber.e(it, "Error loading categories") }
          .onFailure { setState { copy(categories = emptyList()) } }
    }
  }
}
