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

package com.pyamsoft.fridge.locator.map

import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.fridge.locator.R
import com.pyamsoft.fridge.locator.databinding.MapActionsBinding
import com.pyamsoft.fridge.ui.SnackbarContainer
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.disposeOnDestroy
import com.pyamsoft.pydroid.ui.util.Snackbreak
import com.pyamsoft.pydroid.ui.util.popShow
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import javax.inject.Inject
import com.pyamsoft.fridge.ui.R as R2

class MapActions @Inject internal constructor(
    private val owner: LifecycleOwner,
    private val imageLoader: ImageLoader,
    parent: ViewGroup,
) : BaseUiView<MapViewState, MapViewEvent, MapActionsBinding>(parent), SnackbarContainer {

    override val viewBinding = MapActionsBinding::inflate

    override val layoutRoot by boundView { osmActions }

    private var nearbyAnimator: ViewPropertyAnimatorCompat? = null
    private var meAnimator: ViewPropertyAnimatorCompat? = null

    init {
        doOnInflate {
            binding.osmFindNearby.isVisible = false
            binding.osmFindMe.isVisible = false
        }

        doOnInflate {
            imageLoader.load(R2.drawable.ic_shopping_cart_24dp)
                .into(binding.osmFindNearby)
                .disposeOnDestroy(owner)
        }

        doOnInflate {
            imageLoader.load(R.drawable.ic_location_search_24dp)
                .into(binding.osmFindMe)
                .disposeOnDestroy(owner)
        }

        doOnInflate {
            binding.osmFindMe.setOnDebouncedClickListener {
                publish(MapViewEvent.RequestMyLocation(firstTime = false))
            }
        }

        doOnInflate {
            binding.osmFindNearby.setOnDebouncedClickListener {
                publish(MapViewEvent.RequestFindNearby)
            }
        }

        doOnTeardown {
            binding.osmFindMe.setOnDebouncedClickListener(null)
            binding.osmFindNearby.setOnDebouncedClickListener(null)

            dismissNearbyAnimator()
            dismissMeAnimator()
        }
    }

    override fun container(): CoordinatorLayout {
        return layoutRoot
    }

    private fun dismissNearbyAnimator() {
        nearbyAnimator?.cancel()
        nearbyAnimator = null
    }

    private fun dismissMeAnimator() {
        meAnimator?.cancel()
        meAnimator = null
    }

    override fun onRender(state: UiRender<MapViewState>) {
        state.distinctBy { it.bottomOffset }.render(viewScope) { handleBottomMargin(it) }
        state.distinctBy { it.centerMyLocation }.render(viewScope) { handleCenterLocation(it) }
        state.distinctBy { it.nearbyError }.render(viewScope) { handleNearbyError(it) }
        state.distinctBy { it.cachedFetchError }.render(viewScope) { handleFetchError(it) }
    }

    private fun handleBottomMargin(height: Int) {
        if (height > 0) {
            layoutRoot.updatePadding(bottom = height)
        }
    }

    private fun handleCenterLocation(centerMyLocation: MapViewState.CenterMyLocation?) {
        centerMyLocation?.let { event ->
            if (event.firstTime) {
                revealButtons()
            }
        }
    }

    private fun revealButtons() {
        if (nearbyAnimator != null) {
            return
        }
        nearbyAnimator = binding.osmFindNearby.popShow(startDelay = 700L).withEndAction {
            revealFindMeButton()
        }
    }

    private fun revealFindMeButton() {
        dismissNearbyAnimator()
        if (meAnimator != null) {
            return
        }
        meAnimator = binding.osmFindMe.popShow(startDelay = 0L).withEndAction {
            dismissMeAnimator()
        }
    }

    private fun handleFetchError(throwable: Throwable?) {
        if (throwable == null) {
            clearCacheError()
        } else {
            showCacheError(throwable)
        }
    }

    private fun handleNearbyError(throwable: Throwable?) {
        if (throwable == null) {
            clearError()
        } else {
            showError(throwable)
        }
    }

    private fun showError(throwable: Throwable) {
        Snackbreak.bindTo(owner, "nearby") {
            make(layoutRoot, throwable.message ?: "An unexpected error occurred.")
        }
    }

    private fun clearError() {
        Snackbreak.bindTo(owner, "nearby") {
            dismiss()
        }
    }

    private fun showCacheError(throwable: Throwable) {
        Snackbreak.bindTo(owner, "cache") {
            make(layoutRoot, throwable.message ?: "An error occurred fetching cached stores.")
        }
    }

    private fun clearCacheError() {
        Snackbreak.bindTo(owner, "cache") {
            dismiss()
        }
    }
}