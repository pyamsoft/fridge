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

package com.pyamsoft.fridge.detail.item

import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.CheckResult
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.fridge.core.today
import com.pyamsoft.fridge.db.item.FridgeItem
import com.pyamsoft.fridge.db.item.FridgeItem.Presence.HAVE
import com.pyamsoft.fridge.db.item.cleanMidnight
import com.pyamsoft.fridge.db.item.daysLaterMidnight
import com.pyamsoft.fridge.db.item.getExpiredMessage
import com.pyamsoft.fridge.db.item.getExpiringSoonMessage
import com.pyamsoft.fridge.db.item.isArchived
import com.pyamsoft.fridge.db.item.isExpired
import com.pyamsoft.fridge.db.item.isExpiringSoon
import com.pyamsoft.fridge.detail.R
import com.pyamsoft.fridge.detail.databinding.DetailListItemGlancesBinding
import com.pyamsoft.fridge.detail.item.DetailItemViewEvent.ExpandItem
import com.pyamsoft.fridge.tooltip.Tooltip
import com.pyamsoft.fridge.tooltip.TooltipCreator
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.Loaded
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.pydroid.util.tintWith
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class DetailListItemGlances @Inject internal constructor(
    private val tooltipCreator: TooltipCreator,
    private val theming: ThemeProvider,
    private val imageLoader: ImageLoader,
    private val owner: LifecycleOwner,
    parent: ViewGroup
) : BaseUiView<DetailListItemViewState, DetailItemViewEvent, DetailListItemGlancesBinding>(parent) {

    override val viewBinding = DetailListItemGlancesBinding::inflate

    override val layoutRoot by boundView { detailItemGlances }

    private var dateRangeLoader: Loaded? = null
    private var expiringLoader: Loaded? = null
    private var expiredLoader: Loaded? = null

    private var dateRangeTooltip: Tooltip? = null
    private var expiringTooltip: Tooltip? = null
    private var expiredTooltip: Tooltip? = null

    init {
        doOnInflate {
            layoutRoot.setOnDebouncedClickListener { publish(ExpandItem) }
            binding.detailItemGlancesDate.setOnDebouncedClickListener { dateRangeTooltip?.show(it) }
            binding.detailItemGlancesExpiring.setOnDebouncedClickListener { expiringTooltip?.show(it) }
            binding.detailItemGlancesExpired.setOnDebouncedClickListener { expiredTooltip?.show(it) }
        }

        doOnTeardown {
            layoutRoot.setOnDebouncedClickListener(null)
            binding.detailItemGlancesDate.setOnDebouncedClickListener(null)
            binding.detailItemGlancesExpiring.setOnDebouncedClickListener(null)
            binding.detailItemGlancesExpired.setOnDebouncedClickListener(null)
            clear()
        }
    }

    private fun clear() {
        dateRangeLoader?.dispose()
        dateRangeLoader = null
        dateRangeTooltip?.hide()
        dateRangeTooltip = null

        expiringLoader?.dispose()
        expiringLoader = null
        expiringTooltip?.hide()
        expiringTooltip = null

        expiredLoader?.dispose()
        expiredLoader = null
        expiredTooltip?.hide()
        expiredTooltip = null
    }

    override fun onRender(state: DetailListItemViewState) {
        handleItem(state)
    }

    private fun handleItem(state: DetailListItemViewState) {
        state.item.let { item ->
            require(item.isReal()) { "Cannot render non-real item: $item" }
            val range = state.expirationRange
            val isSameDayExpired = state.isSameDayExpired

            val isVisible =
                !item.isArchived() && item.presence() == HAVE && range != null && isSameDayExpired != null
            layoutRoot.isVisible = isVisible

            if (isVisible) {
                // This should be fine because of the isVisible conditional
                val dateRange = requireNotNull(range).range
                val isSameDay = requireNotNull(isSameDayExpired).isSame

                val now = today().cleanMidnight()
                val soonDate = today().daysLaterMidnight(dateRange)
                val expireTime = item.expireTime()
                val hasTime = expireTime != null
                val isExpiringSoon = item.isExpiringSoon(now, soonDate, isSameDay)
                val isExpired = item.isExpired(now, isSameDay)

                setDateRangeView(item, expireTime, hasTime)
                setExpiringView(item, now, isExpiringSoon, isExpired, hasTime)
                setExpiredView(item, now, isExpired, hasTime)
            }
        }
    }

    private fun setDateRangeView(
        item: FridgeItem,
        expireTime: Date?,
        hasTime: Boolean
    ) {
        dateRangeLoader?.dispose()
        dateRangeLoader = setViewColor(
            theming,
            imageLoader,
            binding.detailItemGlancesDate,
            R.drawable.ic_date_range_24dp,
            dateRangeLoader,
            hasTime,
            hasTime
        )

        dateRangeTooltip?.hide()
        if (expireTime == null) {
            dateRangeTooltip = null
            return
        }

        dateRangeTooltip = tooltipCreator.top(owner) {
            val dateFormatted = SimpleDateFormat.getDateInstance().format(expireTime)
            setText("${item.name().trim()} will expire on $dateFormatted")
        }
    }

    private fun setExpiringView(
        item: FridgeItem,
        now: Calendar,
        isExpiringSoon: Boolean,
        isExpired: Boolean,
        hasTime: Boolean
    ) {
        // Show this if there is a time and the item is not yet expired
        val isVisible = hasTime && !isExpired

        expiredLoader?.dispose()
        expiringLoader = setViewColor(
            theming,
            imageLoader,
            binding.detailItemGlancesExpiring,
            R.drawable.ic_consumed_24dp,
            expiringLoader,
            isExpiringSoon,
            isVisible
        )

        expiringTooltip?.hide()
        if (!isVisible || !hasTime) {
            expiringTooltip = null
            return
        }

        expiringTooltip = tooltipCreator.top(owner) {
            // shitty old time format parser for very basic expiration estimate

            setText("${item.name().trim()} ${item.getExpiringSoonMessage(now)}")
        }
    }

    private fun setExpiredView(
        item: FridgeItem,
        now: Calendar,
        isExpired: Boolean,
        hasTime: Boolean
    ) {
        // Show this if there is a time and the item is expired
        val isVisible = hasTime && isExpired

        expiredLoader?.dispose()
        expiredLoader = setViewColor(
            theming,
            imageLoader,
            binding.detailItemGlancesExpired,
            R.drawable.ic_spoiled_24dp,
            expiredLoader,
            isExpired,
            isVisible
        )

        expiredTooltip?.hide()
        if (!isVisible) {
            expiredTooltip = null
            return
        }

        expiredTooltip = tooltipCreator.top(owner) {
            setText("${item.name().trim()} ${item.getExpiredMessage(now)}")
        }
    }

    companion object {

        @JvmStatic
        @CheckResult
        private fun setViewColor(
            theming: ThemeProvider,
            imageLoader: ImageLoader,
            view: ImageView,
            @DrawableRes drawable: Int,
            loaded: Loaded?,
            isColored: Boolean,
            isVisible: Boolean
        ): Loaded? {
            if (isVisible) {
                view.isVisible = true
                val color =
                    if (isColored) R.color.colorSecondary else if (theming.isDarkTheme()) R.color.white else R.color.black
                loaded?.dispose()
                return imageLoader.load(drawable)
                    .mutate { it.tintWith(view.context, color) }
                    .into(view)
            }

            view.isVisible = false
            return loaded
        }
    }
}
