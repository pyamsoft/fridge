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

package com.pyamsoft.fridge.entry

import androidx.lifecycle.viewModelScope
import com.pyamsoft.fridge.db.entry.FridgeEntry
import com.pyamsoft.fridge.db.item.FridgeItem
import com.pyamsoft.pydroid.arch.Renderable
import com.pyamsoft.pydroid.arch.UiViewModel
import timber.log.Timber
import javax.inject.Inject

class EntryViewModel @Inject internal constructor(
    private val delegate: EntryListStateModel,
) : UiViewModel<EntryViewState, EntryViewEvent, EntryControllerEvent>(
    initialState = delegate.initialState
) {

    init {
        val job = delegate.bind(Renderable { state ->
            state.render(viewModelScope) { setState { it } }
        })
        doOnCleared { job.cancel() }
        doOnCleared { delegate.clear() }
    }

    override fun handleViewEvent(event: EntryViewEvent) {
        return when (event) {
            is EntryViewEvent.ListEvent.SelectEntry -> select(event.entry)
            is EntryViewEvent.ListEvent.ForceRefresh -> delegate.refreshList(true)
            is EntryViewEvent.ListEvent.DeleteEntry -> delegate.deleteEntry(event.entry)
            is EntryViewEvent.AddEvent.AddNew -> handleAddNew()
            is EntryViewEvent.AddEvent.ReallyDeleteEntryNoUndo -> delegate.deleteForever(event.entry)
            is EntryViewEvent.AddEvent.UndoDeleteEntry -> delegate.undoDelete(event.entry)
            is EntryViewEvent.ToolbarEvent.SearchQuery -> delegate.updateSearch(event.search)
            is EntryViewEvent.ToolbarEvent.ChangeSort -> delegate.changeSort(event.sort)
        }
    }

    private fun handleAddNew() {
        Timber.d("Add new entry")
        publish(EntryControllerEvent.AddEntry)
    }

    private fun select(entry: FridgeEntry) {
        Timber.d("Loading entry page: $entry")
        publish(EntryControllerEvent.LoadEntry(entry, FridgeItem.Presence.NEED))
    }

}
