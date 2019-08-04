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

package com.pyamsoft.fridge.db.room.dao.zone

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.pyamsoft.fridge.db.room.entity.RoomNearbyZone
import com.pyamsoft.fridge.db.zone.NearbyZone
import com.pyamsoft.fridge.db.zone.NearbyZoneUpdateDao
import timber.log.Timber

@Dao
internal abstract class RoomNearbyZoneUpdateDao internal constructor() : NearbyZoneUpdateDao {

  override suspend fun update(o: NearbyZone) {
    Timber.d("ROOM: NearbyZone Update: $o")
    val roomNearbyZone = RoomNearbyZone.create(o)
    daoUpdate(roomNearbyZone)
  }

  @Update(onConflict = OnConflictStrategy.ABORT)
  internal abstract fun daoUpdate(entry: RoomNearbyZone)

}