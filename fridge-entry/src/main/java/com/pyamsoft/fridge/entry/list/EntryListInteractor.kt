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

package com.pyamsoft.fridge.entry.list

import androidx.annotation.CheckResult
import com.pyamsoft.fridge.db.entry.FridgeEntry
import com.pyamsoft.fridge.db.entry.FridgeEntryChangeEvent
import com.pyamsoft.fridge.db.entry.FridgeEntryQueryDao
import com.pyamsoft.fridge.db.entry.FridgeEntryRealtime
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

internal class EntryListInteractor @Inject internal constructor(
  private val queryDao: FridgeEntryQueryDao,
  private val realtime: FridgeEntryRealtime
) {

  @CheckResult
  fun getEntries(force: Boolean): Single<List<FridgeEntry>> {
    return queryDao.queryAll(force)
  }

  @CheckResult
  fun listenForChanges(): Observable<FridgeEntryChangeEvent> {
    return realtime.listenForChanges()
  }
}