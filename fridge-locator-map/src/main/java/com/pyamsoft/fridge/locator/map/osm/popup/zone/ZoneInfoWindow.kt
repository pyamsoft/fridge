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

package com.pyamsoft.fridge.locator.map.osm.popup.zone

import android.location.Location
import android.view.View
import androidx.annotation.CheckResult
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.pyamsoft.fridge.butler.Butler
import com.pyamsoft.fridge.db.zone.NearbyZone
import com.pyamsoft.fridge.db.zone.NearbyZoneDeleteDao
import com.pyamsoft.fridge.db.zone.NearbyZoneInsertDao
import com.pyamsoft.fridge.db.zone.NearbyZoneQueryDao
import com.pyamsoft.fridge.db.zone.NearbyZoneRealtime
import com.pyamsoft.fridge.locator.map.osm.popup.BaseInfoWindow
import com.pyamsoft.fridge.locator.map.osm.popup.LocationUpdateManager
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.arch.factory
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.util.fakeBind
import com.pyamsoft.pydroid.util.fakeUnbind
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.infowindow.InfoWindow
import timber.log.Timber
import javax.inject.Inject

internal class ZoneInfoWindow private constructor(
    manager: LocationUpdateManager,
    zone: NearbyZone,
    map: MapView,
    butler: Butler,
    imageLoader: ImageLoader,
    nearbyZoneRealtime: NearbyZoneRealtime,
    nearbyZoneQueryDao: NearbyZoneQueryDao,
    nearbyZoneInsertDao: NearbyZoneInsertDao,
    nearbyZoneDeleteDao: NearbyZoneDeleteDao
) : BaseInfoWindow(manager, map), LifecycleOwner {

    private val registry = LifecycleRegistry(this)

    @JvmField
    @Inject
    internal var factory: ViewModelProvider.Factory? = null
    @JvmField
    @Inject
    internal var infoTitle: ZoneInfoTitle? = null
    @JvmField
    @Inject
    internal var infoLocation: ZoneInfoLocation? = null
    private val viewModel by factory<ZoneInfoViewModel>(ViewModelStore()) { factory }

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    init {
        DaggerZoneInfoComponent.factory()
            .create(
                parent,
                imageLoader,
                zone,
                butler,
                nearbyZoneRealtime,
                nearbyZoneQueryDao,
                nearbyZoneInsertDao,
                nearbyZoneDeleteDao
            )
            .inject(this)

        val title = requireNotNull(infoTitle)
        val location = requireNotNull(infoLocation)

        createComponent(
            null, this,
            viewModel,
            title,
            location
        ) {
            // TODO
        }

        listenForLocationUpdates()
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

        registry.fakeBind()
    }

    override fun onOpen(item: Any?) {
        val v: View? = view
        if (v == null) {
            Timber.e("ZoneInfoWindow.open, mView is null! Bail")
            return
        }

        if (item == null || item !is Polygon) {
            Timber.e("ZoneInfoWindow.open, item is not OverlayWithIW! Bail")
            return
        }

        viewModel.updatePolygon(item)
    }

    override fun onLocationUpdate(location: Location?) {
        viewModel.handleLocationUpdate(location)
    }

    override fun onClose() {
    }

    override fun onTeardown() {
        registry.fakeUnbind()
        infoTitle = null
        infoLocation = null
        factory = null
    }

    companion object {

        @JvmStatic
        @CheckResult
        fun fromMap(
            manager: LocationUpdateManager,
            zone: NearbyZone,
            map: MapView,
            butler: Butler,
            imageLoader: ImageLoader,
            nearbyZoneRealtime: NearbyZoneRealtime,
            nearbyZoneQueryDao: NearbyZoneQueryDao,
            nearbyZoneInsertDao: NearbyZoneInsertDao,
            nearbyZoneDeleteDao: NearbyZoneDeleteDao
        ): InfoWindow {
            return ZoneInfoWindow(
                manager,
                zone,
                map,
                butler,
                imageLoader,
                nearbyZoneRealtime,
                nearbyZoneQueryDao,
                nearbyZoneInsertDao,
                nearbyZoneDeleteDao
            )
        }
    }
}
