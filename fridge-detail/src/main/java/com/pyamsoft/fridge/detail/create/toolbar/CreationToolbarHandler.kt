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

package com.pyamsoft.fridge.detail.create.toolbar

import com.pyamsoft.fridge.detail.create.toolbar.CreationToolbarHandler.ToolbarEvent
import com.pyamsoft.fridge.detail.create.toolbar.CreationToolbarHandler.ToolbarEvent.Delete
import com.pyamsoft.fridge.detail.create.toolbar.CreationToolbarHandler.ToolbarEvent.Navigate
import com.pyamsoft.pydroid.arch.UiEventHandler
import com.pyamsoft.pydroid.core.bus.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class CreationToolbarHandler @Inject internal constructor(
  bus: EventBus<ToolbarEvent>
) : UiEventHandler<ToolbarEvent, CreationToolbar.Callback>(bus),
  CreationToolbar.Callback {

  override fun onDeleteClicked() {
    publish(Navigate)
  }

  override fun onNavigationClicked() {
    publish(Delete)
  }

  override fun handle(delegate: CreationToolbar.Callback): Disposable {
    return listen()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        return@subscribe when (it) {
          is Navigate -> delegate.onNavigationClicked()
          is Delete -> delegate.onDeleteClicked()
        }
      }
  }

  sealed class ToolbarEvent {
    object Navigate : ToolbarEvent()
    object Delete : ToolbarEvent()
  }

}
