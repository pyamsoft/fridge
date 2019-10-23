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

package com.pyamsoft.fridge.locator.map.osm.popup.store

import androidx.annotation.CheckResult
import com.pyamsoft.fridge.butler.Butler
import com.pyamsoft.fridge.db.store.NearbyStore
import com.pyamsoft.fridge.db.store.NearbyStoreChangeEvent.Delete
import com.pyamsoft.fridge.db.store.NearbyStoreChangeEvent.Insert
import com.pyamsoft.fridge.db.store.NearbyStoreChangeEvent.Update
import com.pyamsoft.fridge.db.store.NearbyStoreDeleteDao
import com.pyamsoft.fridge.db.store.NearbyStoreInsertDao
import com.pyamsoft.fridge.db.store.NearbyStoreQueryDao
import com.pyamsoft.fridge.db.store.NearbyStoreRealtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

internal class StoreInfoInteractor @Inject internal constructor(
    private val butler: Butler,
    private val realtime: NearbyStoreRealtime,
    private val queryDao: NearbyStoreQueryDao,
    private val insertDao: NearbyStoreInsertDao,
    private val deleteDao: NearbyStoreDeleteDao
) {

    @CheckResult
    suspend fun isNearbyStoreCached(id: Long): Boolean =
        withContext(context = Dispatchers.Default) {
            return@withContext queryDao.query(false)
                .any { it.id() == id }
        }

    suspend inline fun listenForNearbyCacheChanges(
        id: Long,
        crossinline onInsert: (zone: NearbyStore) -> Unit,
        crossinline onDelete: (zone: NearbyStore) -> Unit
    ) = withContext(context = Dispatchers.Default) {
        realtime.listenForChanges()
            .onEvent { event ->
                return@onEvent when (event) {
                    is Insert -> {
                        if (event.store.id() == id) {
                            onInsert(event.store)
                        } else {
                            // Ignore event for other zone
                            Unit
                        }
                    }
                    is Delete -> {
                        if (event.store.id() == id) {
                            onDelete(event.store)
                        } else {
                            // Ignore event for other zone
                            Unit
                        }
                    }
                    is Update -> {
                        // Ignore Update events
                        Unit
                    }
                }
            }
    }

    suspend fun deleteStoreFromDb(zone: NearbyStore) = withContext(context = Dispatchers.Default) {
        deleteDao.delete(zone)
        restartLocationWorker()
    }

    suspend fun insertStoreIntoDb(zone: NearbyStore) = withContext(context = Dispatchers.Default) {
        insertDao.insert(zone)
        restartLocationWorker()
    }

    private fun restartLocationWorker() {
        butler.unregisterGeofences()
        butler.registerGeofences(1, SECONDS)

        butler.cancelLocationReminder()
        butler.remindLocation(1, SECONDS)
    }
}