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

import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.pyamsoft.fridge.entry.databinding.EntryContainerBinding
import com.pyamsoft.fridge.ui.SnackbarContainer
import com.pyamsoft.pydroid.arch.BaseUiView
import javax.inject.Inject

class EntryContainer @Inject internal constructor(
    parent: ViewGroup,
    list: EntryList,
    private val addNew: EntryAddNew,
) : BaseUiView<EntryViewState, EntryViewEvent, EntryContainerBinding>(parent), SnackbarContainer {

    override val viewBinding = EntryContainerBinding::inflate

    override val layoutRoot by boundView { entryContainer }

    init {
        nest(list)
        nest(addNew)
    }

    override fun container(): CoordinatorLayout? {
        return addNew.container()
    }

    override fun onRender(state: EntryViewState) {
    }

}
