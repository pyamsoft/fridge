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

package com.pyamsoft.fridge.detail.create.title

import com.pyamsoft.fridge.detail.DetailConstants
import com.pyamsoft.fridge.detail.create.CreationScope
import com.pyamsoft.fridge.detail.create.title.CreationTitlePresenter.Callback
import com.pyamsoft.pydroid.arch.BasePresenter
import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@CreationScope
internal class CreationTitlePresenter @Inject internal constructor(
  private val interactor: CreationTitleInteractor
) : BasePresenter<Unit, Callback>(RxBus.empty()),
  CreationTitle.Callback {

  private var updateDisposable by singleDisposable()

  override fun onBind() {
    observeName(false)
  }

  override fun onUnbind() {
    // Don't dispose updateDisposable here as it may need to outlive the View
    // since the final commit happens as the View is tearing down
  }

  private fun observeName(force: Boolean) {
    interactor.observeEntryName(force)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ callback.handleNameUpdated(it.name, it.firstUpdate) }, {
        Timber.e(it, "Error observing entry name")
        callback.handleNameUpdateError(it)
      }).destroy()
  }

  override fun onUpdateName(name: String) {
    updateDisposable = Completable.complete()
      .delay(DetailConstants.COMMIT_TIMEOUT_DURATION, DetailConstants.COMMIT_TIMEOUT_UNIT)
      .andThen(interactor.saveName(name.trim()))
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doAfterTerminate {
        // Dispose ourselves here after we are done
        updateDisposable.tryDispose()
      }
      .subscribe({ }, {
        Timber.e(it, "Error updating entry name")
        callback.handleNameUpdateError(it)
      })
  }

  interface Callback {

    fun handleNameUpdated(name: String, firstUpdate: Boolean)

    fun handleNameUpdateError(throwable: Throwable)

  }

}