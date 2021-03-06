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

package com.pyamsoft.fridge.db.room.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.fridge.core.currentDate
import java.util.Date

internal object DateTypeConverter {

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toDate(time: Long?): Date? {
    return if (time == null) null else currentDate().apply { this.time = time }
  }

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toLong(date: Date?): Long? {
    return date?.time
  }
}
