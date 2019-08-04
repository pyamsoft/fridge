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
import androidx.fragment.app.Fragment

interface MapPermission {

  @CheckResult
  fun hasBackgroundPermission(): Boolean

  fun requestBackgroundPermission(fragment: Fragment)

  fun onBackgroundResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    onBackgroundPermissionGranted: () -> Unit
  )

  @CheckResult
  fun hasForegroundPermission(): Boolean

  fun requestForegroundPermission(fragment: Fragment)

  fun onForegroundResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    onForegroundPermissionGranted: () -> Unit
  )

  @CheckResult
  fun hasStoragePermission(): Boolean

  fun requestStoragePermission(fragment: Fragment)

  fun onStorageResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    onStoragePermissionGranted: () -> Unit
  )

}