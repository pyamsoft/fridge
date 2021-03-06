/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.fridge.db.room.dao.item

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Delete
import com.pyamsoft.fridge.db.item.FridgeItem
import com.pyamsoft.fridge.db.item.FridgeItemDeleteDao
import com.pyamsoft.fridge.db.room.entity.RoomFridgeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomFridgeItemDeleteDao internal constructor() : FridgeItemDeleteDao {

  final override suspend fun delete(o: FridgeItem, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        val roomItem = RoomFridgeItem.create(o)
        return@withContext daoDelete(roomItem) > 0
      }

  @Delete @CheckResult internal abstract fun daoDelete(item: RoomFridgeItem): Int
}
