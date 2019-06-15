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

package com.pyamsoft.fridge.detail.expand

import androidx.annotation.CheckResult
import com.pyamsoft.fridge.db.item.FridgeItem
import com.pyamsoft.fridge.db.item.FridgeItem.Presence
import com.pyamsoft.fridge.db.item.FridgeItemChangeEvent
import com.pyamsoft.fridge.db.item.FridgeItemChangeEvent.Delete
import com.pyamsoft.fridge.db.item.FridgeItemChangeEvent.Insert
import com.pyamsoft.fridge.db.item.FridgeItemChangeEvent.Update
import com.pyamsoft.fridge.db.item.FridgeItemRealtime
import com.pyamsoft.fridge.detail.DetailInteractor
import com.pyamsoft.fridge.detail.item.DateSelectPayload
import com.pyamsoft.fridge.detail.item.DetailItemControllerEvent.CloseExpand
import com.pyamsoft.fridge.detail.item.DetailItemControllerEvent.DatePick
import com.pyamsoft.fridge.detail.item.DetailItemControllerEvent.ExpandDetails
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent.ArchiveItem
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent.CloseItem
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent.CommitName
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent.CommitPresence
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent.DeleteItem
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent.ExpandItem
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent.PickDate
import com.pyamsoft.fridge.detail.item.DetailItemViewModel
import com.pyamsoft.fridge.detail.item.isNameValid
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Named

class ExpandItemViewModel @Inject internal constructor(
  @Named("item_editable") isEditable: Boolean,
  item: FridgeItem,
  defaultPresence: Presence,
  dateSelectBus: EventBus<DateSelectPayload>,
  realtime: FridgeItemRealtime,
  fakeRealtime: EventBus<FridgeItemChangeEvent>,
  private val interactor: DetailInteractor
) : DetailItemViewModel(isEditable, item.presence(defaultPresence), fakeRealtime) {

  private val itemEntryId = item.entryId()
  private val itemId = item.id()

  private var dateDisposable by singleDisposable()
  private var realtimeDisposable by singleDisposable()

  init {
    dateDisposable = dateSelectBus.listen()
        .filter { it.oldItem.entryId() == itemEntryId }
        .filter { it.oldItem.id() == itemId }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { commitDate(it.oldItem, it.year, it.month, it.day) }

    realtimeDisposable =
      Observable.merge(realtime.listenForChanges(itemEntryId), fakeRealtime.listen())
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe { handleRealtimeEvent(it) }
  }

  override fun handleViewEvent(event: DetailItemViewEvent) {
    return when (event) {
      is CommitName -> commitName(event.oldItem, event.name)
      is CommitPresence -> commitPresence(event.oldItem, event.presence)
      is ExpandItem -> expandItem(event.item)
      is PickDate -> pickDate(event.oldItem, event.year, event.month, event.day)
      is CloseItem -> closeSelf(event.item)
      is DeleteItem -> deleteSelf(event.item)
      is ArchiveItem -> archiveSelf(event.item)
    }
  }

  private fun archiveSelf(item: FridgeItem) {
    remove(item, source = { interactor.archive(it) }) { closeSelf(it) }
  }

  private fun deleteSelf(item: FridgeItem) {
    remove(item, source = { interactor.delete(it) }) { closeSelf(it) }
  }

  override fun onTeardown() {
    dateDisposable.tryDispose()
    realtimeDisposable.tryDispose()
  }

  private fun handleRealtimeEvent(event: FridgeItemChangeEvent) {
    return when (event) {
      is Update -> handleModelUpdate(event.item)
      is Insert -> handleModelUpdate(event.item)
      is Delete -> closeSelf(event.item)
    }
  }

  private fun closeSelf(newItem: FridgeItem) {
    if (itemId == newItem.id() && itemEntryId == newItem.entryId()) {
      publish(CloseExpand)
    }
  }

  private fun handleModelUpdate(newItem: FridgeItem) {
    if (itemId == newItem.id() && itemEntryId == newItem.entryId()) {
      setState { copy(item = newItem) }
    }
  }

  private fun pickDate(
    oldItem: FridgeItem,
    year: Int,
    month: Int,
    day: Int
  ) {
    Timber.d("Launch date picker from date: $year ${month + 1} $day")
    publish(DatePick(oldItem, year, month, day))
  }

  @CheckResult
  private fun isReadyToBeReal(item: FridgeItem): Boolean {
    return isNameValid(item.name())
  }

  private fun commitName(
    oldItem: FridgeItem,
    name: String
  ) {
    // Stop any pending updates
    updateDisposable.tryDispose()

    if (isNameValid(name)) {
      setFixMessage("")
      commitItem(item = oldItem.name(name))
    } else {
      Timber.w("Invalid name: $name")
      handleInvalidName(name)
    }
  }

  private fun commitDate(
    oldItem: FridgeItem,
    year: Int,
    month: Int,
    day: Int
  ) {
    // Stop any pending updates
    updateDisposable.tryDispose()

    Timber.d("Attempt save time: $year/${month + 1}/$day")
    val newTime = Calendar.getInstance()
        .apply {
          set(Calendar.YEAR, year)
          set(Calendar.MONTH, month)
          set(Calendar.DAY_OF_MONTH, day)
        }
        .time
    Timber.d("Save expire time: $newTime")
    commitItem(item = oldItem.expireTime(newTime))
  }

  private fun commitPresence(
    oldItem: FridgeItem,
    presence: Presence
  ) {
    // Stop any pending updates
    updateDisposable.tryDispose()

    commitItem(item = oldItem.presence(presence))
  }

  private fun commitItem(item: FridgeItem) {
    if (!item.isReal() && !isReadyToBeReal(item)) {
      Timber.w("Commit called on a non-real item: $item, fake callback")
      handleFakeCommit(item)
      return
    }

    updateDisposable = Completable.complete()
        .andThen(interactor.commit(item.makeReal()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doAfterTerminate { updateDisposable.tryDispose() }
        .subscribe({ }, {
          Timber.e(it, "Error updating item: ${item.id()}")
          handleError(it)
        })
  }

  private fun handleFakeCommit(item: FridgeItem) {
    fakeRealtime.publish(Insert(item))
    Timber.w("Not ready to commit item yet: $item")
  }

  private fun handleInvalidName(name: String) {
    setFixMessage("ERROR: Name $name is invalid. Please fix.")
  }

  private fun handleError(throwable: Throwable) {
    setState { copy(throwable = throwable) }
  }

  private fun setFixMessage(message: String) {
    setState {
      copy(throwable = if (message.isBlank()) null else IllegalArgumentException(message))
    }
  }

  private fun expandItem(item: FridgeItem) {
    publish(ExpandDetails(item))
  }

}