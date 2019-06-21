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

package com.pyamsoft.fridge.locator.map.permission

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.pyamsoft.fridge.locator.map.R
import com.pyamsoft.fridge.locator.map.permission.PermissionViewEvent.FireLocationPermission
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import javax.inject.Inject

class LocationPermissionScreen @Inject internal constructor(
  parent: ViewGroup
) : BaseUiView<UnitViewState, PermissionViewEvent>(parent) {

  override val layout: Int = R.layout.permission
  override val layoutRoot by boundView<FrameLayout>(R.id.location_permission)
  private val permissionButton by boundView<Button>(R.id.location_permission_button)

  override fun onInflated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    permissionButton.setOnDebouncedClickListener {
      publish(FireLocationPermission)
    }
  }

  override fun onTeardown() {
    permissionButton.setOnDebouncedClickListener(null)
  }

  override fun onRender(
    state: UnitViewState,
    savedState: UiSavedState
  ) {
  }

}