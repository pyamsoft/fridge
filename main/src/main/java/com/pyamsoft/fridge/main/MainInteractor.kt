/*
 * Copyright 2020 Peter Kenji Yamanaka
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

package com.pyamsoft.fridge.main

import androidx.annotation.CheckResult
import com.pyamsoft.fridge.core.today
import com.pyamsoft.fridge.db.item.FridgeItem
import com.pyamsoft.fridge.db.item.FridgeItemChangeEvent
import com.pyamsoft.fridge.db.item.FridgeItemPreferences
import com.pyamsoft.fridge.db.item.FridgeItemQueryDao
import com.pyamsoft.fridge.db.item.FridgeItemRealtime
import com.pyamsoft.fridge.db.item.cleanMidnight
import com.pyamsoft.fridge.db.item.daysLaterMidnight
import com.pyamsoft.fridge.db.item.isArchived
import com.pyamsoft.fridge.db.item.isExpired
import com.pyamsoft.fridge.db.item.isExpiringSoon
import com.pyamsoft.fridge.locator.Geofencer
import com.pyamsoft.fridge.locator.Nearby
import com.pyamsoft.pydroid.core.Enforcer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MainInteractor @Inject internal constructor(
    private val nearby: Nearby,
    private val itemQueryDao: FridgeItemQueryDao,
    private val itemRealtime: FridgeItemRealtime,
    private val fridgeItemPreferences: FridgeItemPreferences
) {

    @CheckResult
    suspend fun getNeededCount(): Int = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext itemQueryDao.query(false).byPresence(FridgeItem.Presence.NEED).count()
    }

    @CheckResult
    suspend fun getExpiredOrExpiringCount(): Int = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        val now = today().cleanMidnight()
        val later = today().daysLaterMidnight(fridgeItemPreferences.getExpiringSoonRange())
        val isSameDayExpired = fridgeItemPreferences.isSameDayExpired()
        return@withContext itemQueryDao.query(false).byPresence(FridgeItem.Presence.HAVE)
            .filter {
                it.isExpired(now, isSameDayExpired) || it.isExpiringSoon(
                    now,
                    later,
                    isSameDayExpired
                )
            }
            .count()
    }

    suspend fun getNearbyStoreCount(): Int = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext nearby.nearbyStores(false, Geofencer.DEFAULT_RADIUS_IN_METERS)
            .sortedBy { it.distance }
            .map { it.nearby }
            .count()
    }

    suspend fun getNearbyZoneCount(): Int = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext nearby.nearbyZones(false, Geofencer.DEFAULT_RADIUS_IN_METERS)
            .sortedBy { it.distance }
            .map { it.nearby }
            .count()
    }

    suspend fun listenForItemChanges(onEvent: suspend (FridgeItemChangeEvent) -> Unit) =
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()
            return@withContext itemRealtime.listenForChanges(onEvent)
        }

    companion object {

        @JvmStatic
        @CheckResult
        private fun List<FridgeItem>.byPresence(presence: FridgeItem.Presence): Sequence<FridgeItem> {
            return this.asSequence()
                .filterNot { it.isArchived() }
                .filter { it.presence() == presence }
        }
    }
}
