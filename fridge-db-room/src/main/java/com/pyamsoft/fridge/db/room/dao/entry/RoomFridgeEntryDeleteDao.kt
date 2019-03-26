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

package com.pyamsoft.fridge.db.room.dao.entry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.pyamsoft.fridge.db.entry.FridgeEntry
import com.pyamsoft.fridge.db.entry.FridgeEntryDeleteDao
import com.pyamsoft.fridge.db.room.dao.applyDbSchedulers
import com.pyamsoft.fridge.db.room.entity.RoomFridgeEntry
import io.reactivex.Completable
import io.reactivex.Single

@Dao
internal abstract class RoomFridgeEntryDeleteDao internal constructor() : FridgeEntryDeleteDao {

  override fun delete(entry: FridgeEntry): Completable {
    return Single.just(entry)
      .map { RoomFridgeEntry.create(it) }
      .flatMapCompletable {
        return@flatMapCompletable Completable.fromAction { daoDelete(it) }
          .applyDbSchedulers()
      }
  }

  @Delete
  internal abstract fun daoDelete(entry: RoomFridgeEntry)

  override fun deleteAll(): Completable {
    return Completable.fromAction { daoDeleteAll() }
  }

  @Query("DELETE FROM ${RoomFridgeEntry.TABLE_NAME} WHERE 1 = 1")
  internal abstract fun daoDeleteAll()

}