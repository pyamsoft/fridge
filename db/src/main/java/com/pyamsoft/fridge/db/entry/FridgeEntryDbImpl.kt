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

package com.pyamsoft.fridge.db.entry

import com.pyamsoft.cachify.Cached
import com.pyamsoft.fridge.db.BaseDbImpl
import com.pyamsoft.fridge.db.entry.FridgeEntryChangeEvent.Delete
import com.pyamsoft.fridge.db.entry.FridgeEntryChangeEvent.DeleteAll
import com.pyamsoft.fridge.db.entry.FridgeEntryChangeEvent.Insert
import com.pyamsoft.pydroid.core.Enforcer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FridgeEntryDbImpl internal constructor(
    private val cache: Cached<List<FridgeEntry>>,
    insertDao: FridgeEntryInsertDao,
    deleteDao: FridgeEntryDeleteDao
) : BaseDbImpl<FridgeEntry, FridgeEntryChangeEvent>(), FridgeEntryDb {

    private val realtime = object : FridgeEntryRealtime {
        override suspend fun listenForChanges(onChange: suspend (event: FridgeEntryChangeEvent) -> Unit) =
            withContext(context = Dispatchers.IO) { onEvent(onChange) }
    }

    private val queryDao = object : FridgeEntryQueryDao {

        override suspend fun query(force: Boolean): List<FridgeEntry> =
            withContext(context = Dispatchers.IO) {
                Enforcer.assertOffMainThread()
                if (force) {
                    invalidate()
                }

                return@withContext cache.call()
            }
    }

    private val insertDao = object : FridgeEntryInsertDao {

        override suspend fun insert(o: FridgeEntry) = withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()
            insertDao.insert(o)
            publish(Insert(o.makeReal()))
        }
    }

    private val deleteDao = object : FridgeEntryDeleteDao {

        override suspend fun delete(o: FridgeEntry) = withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()
            deleteDao.delete(o)
            publish(Delete(o.makeReal()))
        }

        override suspend fun deleteAll() = withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()
            deleteDao.deleteAll()
            publish(DeleteAll)
        }
    }

    override fun realtime(): FridgeEntryRealtime {
        return realtime
    }

    override fun queryDao(): FridgeEntryQueryDao {
        return queryDao
    }

    override fun insertDao(): FridgeEntryInsertDao {
        return insertDao
    }

    override fun deleteDao(): FridgeEntryDeleteDao {
        return deleteDao
    }

    override suspend fun invalidate() {
        cache.clear()
    }
}
