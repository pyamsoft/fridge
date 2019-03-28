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

package com.pyamsoft.fridge.detail.toolbar

import com.pyamsoft.fridge.detail.DetailScope
import com.pyamsoft.pydroid.arch.BasePresenter
import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@DetailScope
internal class DetailToolbarPresenter @Inject internal constructor(
  private val interactor: DetailToolbarInteractor
) : BasePresenter<Unit, DetailToolbarPresenter.Callback>(RxBus.empty()),
  DetailToolbar.Callback {

  private var deleteDisposable by singleDisposable()

  override fun onBind() {
    observeReal(false)
    listenForDelete()
  }

  override fun onUnbind() {
    // Do not dispose delete disposable here, we want it to finish
  }

  private fun observeReal(force: Boolean) {
    interactor.observeEntryReal(force)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ callback.handleRealUpdated(it) }, {
        Timber.e(it, "Error observing entry real")
        callback.handleRealUpdateError(it)
      }).destroy()
  }

  private fun listenForDelete() {
    interactor.listenForDeleted()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { callback.handleDeleted() }
      .destroy()
  }

  override fun onNavigationClicked() {
    callback.handleBack()
  }

  override fun onDeleteClicked() {
    deleteDisposable = interactor.delete()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doAfterTerminate { deleteDisposable.tryDispose() }
      .subscribe({}, {
        Timber.e(it, "Error observing delete stream")
        callback.handleDeleteError(it)
      })
  }

  interface Callback {

    fun handleBack()

    fun handleDeleted()

    fun handleRealUpdated(real: Boolean)

    fun handleRealUpdateError(throwable: Throwable)

    fun handleDeleteError(throwable: Throwable)
  }

}
