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
import android.view.MenuItem
import com.pyamsoft.fridge.detail.R
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.ui.app.ToolbarActivity
import com.pyamsoft.pydroid.ui.arch.InvalidIdException
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import javax.inject.Inject

internal class DetailToolbar @Inject internal constructor(
  private val toolbarActivity: ToolbarActivity,
  private val callback: Callback
) : UiView {

  private var saveMenuItem: MenuItem? = null

  override fun id(): Int {
    throw InvalidIdException
  }

  override fun inflate(savedInstanceState: Bundle?) {
    toolbarActivity.requireToolbar { toolbar ->
      toolbar.setUpEnabled(true)
      toolbar.setNavigationOnClickListener(DebouncedOnClickListener.create {
        callback.onNavigationClicked()
      })

      toolbar.inflateMenu(R.menu.menu_detail)
      val saveItem = toolbar.menu.findItem(R.id.menu_item_save)
      saveItem.setOnMenuItemClickListener {
        callback.onSaveClicked()
        return@setOnMenuItemClickListener true
      }
      saveMenuItem = saveItem
    }
  }

  override fun saveState(outState: Bundle) {
  }

  override fun teardown() {
    toolbarActivity.withToolbar { toolber ->
      toolber.setUpEnabled(false)
      toolber.setNavigationOnClickListener(null)

      saveMenuItem?.setOnMenuItemClickListener(null)
      saveMenuItem = null
      toolber.menu.removeItem(R.id.menu_item_save)
    }
  }

  interface Callback {

    fun onNavigationClicked()

    fun onSaveClicked()
  }

}
