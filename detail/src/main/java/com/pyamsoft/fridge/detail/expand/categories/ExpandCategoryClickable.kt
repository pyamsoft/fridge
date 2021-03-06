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

package com.pyamsoft.fridge.detail.expand.categories

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener

abstract class ExpandCategoryClickable<B : ViewBinding> protected constructor(parent: ViewGroup) :
    BaseUiView<ExpandedCategoryViewState, ExpandedCategoryViewEvent, B>(parent) {

  init {
    doOnInflate {
      layoutRoot.setOnDebouncedClickListener { publish(ExpandedCategoryViewEvent.Select) }
    }

    doOnTeardown { layoutRoot.setOnDebouncedClickListener(null) }
  }
}
