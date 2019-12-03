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

package com.pyamsoft.fridge.detail.base

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.pyamsoft.fridge.detail.R
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent
import com.pyamsoft.fridge.detail.item.DetailItemViewState
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.Loaded
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.pydroid.util.tintWith
import com.pyamsoft.pydroid.util.toDp
import timber.log.Timber
import java.util.Calendar

abstract class BaseItemDate protected constructor(
    private val imageLoader: ImageLoader,
    parent: ViewGroup
) : BaseUiView<DetailItemViewState, DetailItemViewEvent>(parent) {

    final override val layout: Int = R.layout.detail_list_item_date

    final override val layoutRoot by boundView<ViewGroup>(R.id.detail_item_date)

    private val iconView by boundView<ImageView>(R.id.detail_item_date_icon)
    private val textView by boundView<TextView>(R.id.detail_item_date_text)

    private var dateLoaded: Loaded? = null

    init {
        doOnTeardown {
            layoutRoot.setOnDebouncedClickListener(null)
        }

        doOnTeardown {
            dateLoaded?.dispose()
            dateLoaded = null
        }
    }

    protected fun baseRender(state: DetailItemViewState) {
        val item = state.item
        val expireTime = item.expireTime()

        if (expireTime != null) {
            val date = Calendar.getInstance()
                .apply { time = expireTime }
            Timber.d("Expire time is: $date")

            // Month is zero indexed in storage
            val month = date.get(Calendar.MONTH)
            val day = date.get(Calendar.DAY_OF_MONTH)
            val year = date.get(Calendar.YEAR)

            val dateString =
                "${"${month + 1}".padStart(2, '0')}/${
                "$day".padStart(2, '0')}/${
                "$year".padStart(4, '0')}"
            textView.text = dateString
            iconView.isVisible = false

            textView.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = 0 }
            iconView.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = 0 }
        } else {
            textView.text = "-----"
            iconView.isVisible = true

            textView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = 8.toDp(textView.context)
            }
            iconView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = 12.toDp(textView.context)
            }

            dateLoaded?.dispose()
            dateLoaded = imageLoader.load(R.drawable.ic_date_range_24dp)
                .mutate { it.tintWith(iconView.context, R.color.white) }
                .into(iconView)
        }
    }
}
