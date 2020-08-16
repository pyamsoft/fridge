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

package com.pyamsoft.fridge.db.store

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.Cached1
import com.pyamsoft.fridge.db.BaseDb

interface NearbyStoreDb : BaseDb<
    NearbyStoreChangeEvent,
    NearbyStoreRealtime,
    NearbyStoreQueryDao,
    NearbyStoreInsertDao,
    NearbyStoreDeleteDao
    > {

    companion object {

        @CheckResult
        fun wrap(
            cache: Cached1<Sequence<NearbyStore>, Boolean>,
            insertDao: NearbyStoreInsertDao,
            deleteDao: NearbyStoreDeleteDao
        ): NearbyStoreDb {
            return NearbyStoreDbImpl(cache, insertDao, deleteDao)
        }
    }
}