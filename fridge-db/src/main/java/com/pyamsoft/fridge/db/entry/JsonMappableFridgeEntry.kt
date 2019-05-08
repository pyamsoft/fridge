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

package com.pyamsoft.fridge.db.entry

import android.os.Parcelable
import androidx.annotation.CheckResult
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
@JsonClass(generateAdapter = true)
data class JsonMappableFridgeEntry internal constructor(
  internal val id: String,
  internal val name: String,
  internal val createdTime: Date,
  internal val isReal: Boolean
) : FridgeEntry, Parcelable {

  override fun id(): String {
    return id
  }

  override fun name(): String {
    return name
  }

  override fun createdTime(): Date {
    return createdTime
  }

  override fun isReal(): Boolean {
    return isReal
  }

  override fun name(name: String): FridgeEntry {
    return this.copy(name = name)
  }

  override fun createdTime(createdTime: Date): FridgeEntry {
    return this.copy(createdTime = createdTime)
  }

  override fun makeReal(): FridgeEntry {
    return this.copy(isReal = true)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun from(entry: FridgeEntry): JsonMappableFridgeEntry {
      if (entry is JsonMappableFridgeEntry) {
        return entry
      } else {
        return JsonMappableFridgeEntry(
            entry.id(), entry.name(), entry.createdTime(), entry.isReal()
        )
      }
    }
  }

}
