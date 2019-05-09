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

package com.pyamsoft.fridge.detail

import androidx.annotation.CheckResult
import com.pyamsoft.fridge.db.entry.FridgeEntry
import com.pyamsoft.fridge.db.entry.FridgeEntryInsertDao
import com.pyamsoft.fridge.db.entry.FridgeEntryQueryDao
import com.pyamsoft.pydroid.core.optional.Optional
import com.pyamsoft.pydroid.core.optional.Optional.Present
import com.pyamsoft.pydroid.core.optional.asOptional
import com.pyamsoft.pydroid.core.threads.Enforcer
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.util.Calendar

internal abstract class DetailInteractor protected constructor(
  protected val enforcer: Enforcer,
  private val queryDao: FridgeEntryQueryDao,
  private val insertDao: FridgeEntryInsertDao
) {

  @CheckResult
  protected fun getEntryForId(
    entryId: String,
    force: Boolean
  ): Maybe<FridgeEntry> {
    return queryDao.queryAll(force)
        .flatMapObservable {
          Timber.d("Got entries: $it")
          enforcer.assertNotOnMainThread()
          return@flatMapObservable Observable.fromIterable(it)
        }
        .filter { it.id() == entryId }
        .singleElement()
  }

  @CheckResult
  @JvmOverloads
  protected fun guaranteeEntryExists(
    entryId: String,
    name: String = FridgeEntry.EMPTY_NAME
  ): Single<FridgeEntry> {
    return getEntryForId(entryId, false)
        .map { it.asOptional() }
        .toSingle(Optional.ofNullable(null))
        .flatMap {
          enforcer.assertNotOnMainThread()
          if (it is Present) {
            Timber.d("Entry exists, ignore: ${it.value.id()}")
            return@flatMap Single.just(it.value)
          } else {
            val createdTime = Calendar.getInstance()
                .time
            Timber.d("Create entry: $entryId at $createdTime")
            val entry =
              FridgeEntry.create(entryId, name, createdTime, isReal = true, isArchived = false)
            return@flatMap insertDao.insert(entry)
                .andThen(Single.just(entry))
          }
        }
  }
}
