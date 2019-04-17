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

package com.pyamsoft.fridge.detail.list

import com.pyamsoft.fridge.detail.list.DetailList.Callback
import com.pyamsoft.fridge.detail.list.DetailListHandler.ListEvent
import com.pyamsoft.pydroid.arch.UiEventHandler
import com.pyamsoft.pydroid.core.bus.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

internal abstract class DetailListHandler<E : ListEvent, C : Callback> protected constructor(
  bus: EventBus<E>
) : UiEventHandler<E, C>(bus) {

  override fun handle(delegate: C): Disposable {
    return listen()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { handleEvent(it, delegate) }
  }

  interface ListEvent

  protected abstract fun handleEvent(event: E, delegate: C)

}
