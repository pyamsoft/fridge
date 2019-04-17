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

package com.pyamsoft.fridge.detail.toolbar

import android.os.Bundle
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.ui.app.ToolbarActivity
import com.pyamsoft.pydroid.ui.arch.InvalidIdException
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.pydroid.ui.util.setUpEnabled

internal abstract class DetailToolbar<C : DetailToolbar.Callback> protected constructor(
  protected val toolbarActivity: ToolbarActivity,
  protected val callback: C
) : UiView {

  protected abstract fun onInflate(savedInstanceState: Bundle?)

  protected abstract fun onTeardown()

  final override fun id(): Int {
    throw InvalidIdException
  }

  final override fun inflate(savedInstanceState: Bundle?) {
    toolbarActivity.requireToolbar { toolbar ->
      toolbar.setUpEnabled(true)
      toolbar.setNavigationOnClickListener(DebouncedOnClickListener.create {
        callback.onNavigationClicked()
      })
    }

    onInflate(savedInstanceState)
  }

  final override fun saveState(outState: Bundle) {
  }

  final override fun teardown() {
    toolbarActivity.withToolbar { toolber ->
      toolber.setUpEnabled(false)
      toolber.setNavigationOnClickListener(null)
    }

    onTeardown()
  }

  interface Callback {

    fun onNavigationClicked()
  }

}