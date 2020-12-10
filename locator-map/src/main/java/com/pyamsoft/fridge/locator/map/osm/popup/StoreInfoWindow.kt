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

package com.pyamsoft.fridge.locator.map.osm.popup

import android.location.Location
import androidx.annotation.CheckResult
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.pyamsoft.fridge.db.store.NearbyStore
import com.pyamsoft.fridge.locator.location.LocationUpdateReceiver
import com.pyamsoft.fridge.locator.map.popup.store.StoreInfoLocation
import com.pyamsoft.fridge.locator.map.popup.store.StoreInfoTitle
import com.pyamsoft.fridge.locator.map.popup.store.StoreInfoViewModel
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.ui.arch.viewModelFactory
import com.pyamsoft.pydroid.ui.util.layout
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import javax.inject.Inject

class StoreInfoWindow private constructor(
    receiver: LocationUpdateReceiver,
    store: NearbyStore,
    map: MapView,
    componentFactory: StoreInfoComponent.Factory
) : BaseInfoWindow<Marker>(receiver, map), LifecycleOwner {

    private var stateSaver: StateSaver? = null

    private val registry by lazy(LazyThreadSafetyMode.NONE) { LifecycleRegistry(this) }

    @JvmField
    @Inject
    internal var factory: ViewModelProvider.Factory? = null

    @JvmField
    @Inject
    internal var infoTitle: StoreInfoTitle? = null

    @JvmField
    @Inject
    internal var infoLocation: StoreInfoLocation? = null
    private val viewModel by viewModelFactory<StoreInfoViewModel>(ViewModelStore()) { factory }

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    init {
        componentFactory.create(store, parent).inject(this)

        val title = requireNotNull(infoTitle)
        val location = requireNotNull(infoLocation)
        stateSaver = createComponent(
            null,
            this,
            viewModel,
            title,
            location
        ) {
            // TODO
        }

        parent.layout {
            title.also {
                connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
                constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
            }

            location.also {
                connect(it.id(), ConstraintSet.TOP, title.id(), ConstraintSet.BOTTOM)
                connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
                constrainWidth(it.id(), ConstraintSet.WRAP_CONTENT)
            }
        }

        registry.currentState = Lifecycle.State.RESUMED
    }

    override fun onLocationUpdate(location: Location?) {
        viewModel.handleLocationUpdate(location)
    }

    override fun onPopupOpened(popup: Marker) {
        val position = popup.position
        viewModel.updatePopup(
            name = popup.title,
            latitude = position.latitude,
            longitude = position.longitude
        )
    }

    override fun onClose() {
    }

    override fun onTeardown() {
        registry.currentState = Lifecycle.State.DESTROYED

        infoTitle = null
        infoLocation = null
        factory = null
        stateSaver = null
    }

    companion object {

        @JvmStatic
        @CheckResult
        fun fromMap(
            receiver: LocationUpdateReceiver,
            store: NearbyStore,
            map: MapView,
            factory: StoreInfoComponent.Factory
        ): InfoWindow {
            return StoreInfoWindow(receiver, store, map, factory)
        }
    }
}