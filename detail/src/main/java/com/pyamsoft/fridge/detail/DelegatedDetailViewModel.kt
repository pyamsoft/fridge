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

package com.pyamsoft.fridge.detail

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.Renderable
import com.pyamsoft.pydroid.arch.UiViewModel

abstract class DelegatedDetailViewModel<V : DetailViewEvent, C : DetailControllerEvent> protected constructor(
    protected val delegate: DetailListStateModel,
) : UiViewModel<DetailViewState, V, C>(
    initialState = delegate.initialState
) {

    init {
        val job = delegate.bind(Renderable { state ->
            state.render(viewModelScope) { setState { it } }
        })
        doOnCleared {
            job.cancel()
        }
        doOnCleared {
            delegate.clear()
        }
    }
}
