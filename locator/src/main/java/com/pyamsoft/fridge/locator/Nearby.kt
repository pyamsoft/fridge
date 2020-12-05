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

package com.pyamsoft.fridge.locator

import androidx.annotation.CheckResult
import com.pyamsoft.fridge.db.BaseModel
import com.pyamsoft.fridge.db.store.NearbyStore
import com.pyamsoft.fridge.db.zone.NearbyZone

interface Nearby {

    @CheckResult
    suspend fun nearbyStores(force: Boolean, range: Float): List<DistancePairing<NearbyStore>>

    @CheckResult
    suspend fun nearbyZones(force: Boolean, range: Float): List<DistancePairing<NearbyZone>>

    interface DistancePairing<N : BaseModel<N>> {
        val nearby: N
        val distance: Float
    }
}