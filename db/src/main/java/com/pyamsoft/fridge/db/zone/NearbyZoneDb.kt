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

package com.pyamsoft.fridge.db.zone

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.Cached1
import com.pyamsoft.fridge.db.BaseDb

interface NearbyZoneDb : BaseDb<
    NearbyZoneChangeEvent,
    NearbyZoneRealtime,
    NearbyZoneQueryDao,
    NearbyZoneInsertDao,
    NearbyZoneUpdateDao,
    NearbyZoneDeleteDao
    > {

    companion object {

        @CheckResult
        fun wrap(
            cache: Cached1<Sequence<NearbyZone>, Boolean>,
            insertDao: NearbyZoneInsertDao,
            updateDao: NearbyZoneUpdateDao,
            deleteDao: NearbyZoneDeleteDao
        ): NearbyZoneDb {
            return NearbyZoneDbImpl(cache, insertDao, updateDao, deleteDao)
        }
    }
}
