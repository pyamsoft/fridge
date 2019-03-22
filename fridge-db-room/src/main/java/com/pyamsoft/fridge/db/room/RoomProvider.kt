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

package com.pyamsoft.fridge.db.room

import android.content.Context
import androidx.annotation.CheckResult
import androidx.room.Room
import com.pyamsoft.fridge.db.item.FridgeItemDeleteDao
import com.pyamsoft.fridge.db.item.FridgeItemInsertDao
import com.pyamsoft.fridge.db.item.FridgeItemQueryDao
import com.pyamsoft.fridge.db.item.FridgeItemRealtime
import com.pyamsoft.fridge.db.item.FridgeItemUpdateDao
import com.pyamsoft.fridge.db.room.impl.FridgeItemDb
import com.pyamsoft.fridge.db.room.impl.RoomFridgeDb
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object RoomProvider {

  @Singleton
  @JvmStatic
  @Provides
  @CheckResult
  internal fun provideDb(context: Context): RoomFridgeDb {
    return Room.databaseBuilder(
      context.applicationContext,
      RoomFridgeDb::class.java,
      "fridge_item_room_db.db"
    )
      .build()
  }

  @JvmStatic
  @Provides
  @CheckResult
  internal fun provideFridgeItemDb(room: RoomFridgeDb): FridgeItemDb {
    return room
  }

  @JvmStatic
  @Provides
  @CheckResult
  internal fun provideRealtimeDao(db: FridgeItemDb): FridgeItemRealtime {
    return db.realtimeItems()
  }

  @JvmStatic
  @Provides
  @CheckResult
  internal fun provideQueryDao(db: FridgeItemDb): FridgeItemQueryDao {
    return db.queryItems()
  }

  @JvmStatic
  @Provides
  @CheckResult
  internal fun provideInsertDao(db: FridgeItemDb): FridgeItemInsertDao {
    return db.insertItems()
  }

  @JvmStatic
  @Provides
  @CheckResult
  internal fun provideUpdateDao(db: FridgeItemDb): FridgeItemUpdateDao {
    return db.updateItems()
  }

  @JvmStatic
  @Provides
  @CheckResult
  internal fun provideDeleteDao(db: FridgeItemDb): FridgeItemDeleteDao {
    return db.deleteItems()
  }

}