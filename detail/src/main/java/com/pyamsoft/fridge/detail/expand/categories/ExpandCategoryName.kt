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

package com.pyamsoft.fridge.detail.expand.categories

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.pyamsoft.fridge.detail.databinding.ExpandCategoryNameBinding
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import javax.inject.Inject

class ExpandCategoryName @Inject internal constructor(
    private val themeProvider: ThemeProvider,
    parent: ViewGroup
) : ExpandCategoryClickable<ExpandCategoryNameBinding>(parent) {

    override val viewBinding = ExpandCategoryNameBinding::inflate

    override val layoutRoot by boundView { expandCategoryName }

    init {
        doOnTeardown {
            layoutRoot.isVisible = false
            layoutRoot.text = null
        }
    }

    override fun onRender(state: ExpandedCategoryViewState) {
        handleCategory(state)
    }

    private fun handleCategory(state: ExpandedCategoryViewState) {
        state.category.let { category ->
            layoutRoot.isVisible = true
            if (category == null || category.name.isBlank()) {
                layoutRoot.text = "None"
                layoutRoot.setTextColor(if (themeProvider.isDarkTheme()) Color.WHITE else Color.BLACK)
            } else {
                layoutRoot.text = category.name
                layoutRoot.setTextColor(Color.WHITE)
            }
        }
    }
}