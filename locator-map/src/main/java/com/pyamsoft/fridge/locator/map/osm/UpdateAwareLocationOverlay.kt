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

package com.pyamsoft.fridge.locator.map.osm

import android.location.Location
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

internal class UpdateAwareLocationOverlay internal constructor(
    provider: IMyLocationProvider,
    mapView: MapView,
    private val onLocationChanged: (location: Location?) -> Unit
) : MyLocationNewOverlay(provider, mapView) {

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        onLocationChanged(location)
        return super.onLocationChanged(location, source)
    }
}