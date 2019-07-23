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

package com.pyamsoft.fridge.locator

import androidx.annotation.CheckResult
import java.util.concurrent.TimeUnit

interface Locator {

  @CheckResult
  fun hasBackgroundPermission(): Boolean

  @CheckResult
  fun hasForegroundPermission(): Boolean

  fun registerGeofences(fences: List<Fence>)

  fun unregisterGeofences(fences: List<String>)

  data class Fence(
    val id: String,
    val lat: Double,
    val lon: Double
  )

  companion object {

    val RESCHEDULE_TIME = TimeUnit.HOURS.toMillis(3L)

  }

}
