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

package com.pyamsoft.fridge.entry.list

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.pyamsoft.fridge.db.entry.FridgeEntry
import com.pyamsoft.fridge.entry.R
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.ui.util.refreshing
import javax.inject.Inject

internal class EntryList @Inject internal constructor(
  parent: ViewGroup,
  callback: Callback
) : BaseUiView<EntryList.Callback>(parent, callback) {

  override val layout: Int = R.layout.entry_list

  override val layoutRoot by lazyView<SwipeRefreshLayout>(R.id.entry_swipe_refresh)

  private val recyclerView by lazyView<RecyclerView>(R.id.entry_list)
  private val emptyState by lazyView<TextView>(R.id.entry_empty)

  private var modelAdapter: ModelAdapter<FridgeEntry, EntryListItem>? = null

  override fun onInflated(view: View, savedInstanceState: Bundle?) {
    modelAdapter = ModelAdapter { EntryListItem(it, callback) }

    recyclerView.layoutManager = LinearLayoutManager(view.context).apply {
      isItemPrefetchEnabled = true
      initialPrefetchItemCount = 3
    }

    recyclerView.adapter =
      FastAdapter.with<EntryListItem, ModelAdapter<FridgeEntry, EntryListItem>>(usingAdapter())
  }

  @CheckResult
  fun usingAdapter(): ModelAdapter<FridgeEntry, EntryListItem> {
    return requireNotNull(modelAdapter)
  }

  fun beginRefresh() {
    layoutRoot.refreshing(true)
    usingAdapter().clear()
  }

  fun setList(entries: List<FridgeEntry>) {
    usingAdapter().add(entries)
  }

  fun showError(throwable: Throwable) {
    // TODO clear list
    // TODO set error text
  }

  fun finishRefresh() {
    layoutRoot.refreshing(false)
    // TODO Decide view state based on number of list items
  }

  fun insert(entry: FridgeEntry) {
    usingAdapter().add(entry)
  }

  fun update(entry: FridgeEntry) {
    for ((index, e) in usingAdapter().models.withIndex()) {
      if (entry.id() == e.id()) {
        usingAdapter().set(index, entry)
        break
      }
    }
  }

  fun delete(id: String) {
    var index = -1
    for ((i, entry) in usingAdapter().models.withIndex()) {
      if (entry.id() == id) {
        index = i
        break
      }
    }

    if (index >= 0) {
      usingAdapter().remove(index)
    }
  }

  fun deleteAll() {
    usingAdapter().clear()
  }

  interface Callback : EntryListItem.Callback {

  }
}
