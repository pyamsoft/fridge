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

package com.pyamsoft.fridge

import android.app.Application
import androidx.annotation.CheckResult
import com.pyamsoft.fridge.butler.Butler
import com.pyamsoft.fridge.butler.injector.component.ButlerComponent
import com.pyamsoft.fridge.butler.injector.component.InputButlerComponent
import com.pyamsoft.fridge.category.CategoryListComponent
import com.pyamsoft.fridge.core.Core
import com.pyamsoft.fridge.detail.DetailListComponent
import com.pyamsoft.fridge.detail.expand.ExpandItemCategoryListComponent
import com.pyamsoft.fridge.locator.GeofenceUpdateReceiver
import com.pyamsoft.fridge.locator.map.osm.popup.store.StoreInfoComponent
import com.pyamsoft.fridge.locator.map.osm.popup.zone.ZoneInfoComponent
import com.pyamsoft.fridge.main.MainActivity
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.ui.PYDroid
import com.squareup.moshi.Moshi
import javax.inject.Inject

class FridgeFriend : Application() {

    @JvmField
    @Inject
    internal var butler: Butler? = null

    private var component: FridgeComponent? = null

    override fun onCreate() {
        super.onCreate()
        PYDroid.init(
            this,
            getString(R.string.app_name),
            "https://github.com/pyamsoft/fridgefriend",
            "https://github.com/pyamsoft/fridgefriend/issues",
            Core.PRIVACY_POLICY_URL,
            Core.TERMS_CONDITIONS_URL,
            BuildConfig.VERSION_CODE,
            BuildConfig.DEBUG
        ) { provider ->
            val moshi = Moshi.Builder()
                .build()
            component = DaggerFridgeComponent.factory()
                .create(
                    provider.theming(),
                    moshi,
                    provider.enforcer(),
                    this,
                    provider.imageLoader(),
                    MainActivity::class.java,
                    GeofenceUpdateReceiver::class.java
                )
                .also { onInitialized(it) }
        }
    }

    private fun onInitialized(component: FridgeComponent) {
        addLibraries()

        component.inject(this)

        beginWork()
    }

    private fun beginWork() {
        requireNotNull(butler).initOnAppStart()
    }

    private fun addLibraries() {
        OssLibraries.add(
            "Room",
            "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/room/",
            "The AndroidX Jetpack Room library. Fluent SQLite database access."
        )
        OssLibraries.add(
            "WorkManager",
            "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/work/",
            "The AndroidX Jetpack WorkManager library. Schedule periodic work in a device friendly way."
        )
        OssLibraries.add(
            "Dagger",
            "https://github.com/google/dagger",
            "A fast dependency injector for Android and Java."
        )
        OssLibraries.add(
            "FastAdapter",
            "https://github.com/mikepenz/fastadapter",
            "The bullet proof, fast and easy to use adapter library, which minimizes developing time to a fraction..."
        )
        OssLibraries.add(
            "OsmDroid",
            "https://github.com/osmdroid/osmdroid",
            "OpenStreetMap-Tools for Android"
        )
    }

    override fun getSystemService(name: String): Any? {
        val service = PYDroid.getSystemService(name)
        if (service != null) {
            return service
        }

        return if (name == FridgeComponent::class.java.name) {
            requireNotNull(component)
        } else {
            getServiceFromComponent(name) ?: super.getSystemService(name)
        }
    }

    @CheckResult
    private fun getServiceFromComponent(name: String): Any? {
        val dependency = provideModuleDependencies(name)
        if (dependency != null) {
            return dependency
        }

        return null
    }

    @CheckResult
    private fun provideModuleDependencies(name: String): Any? {
        // ===============================================
        // HACKY INJECTORS see FridgeComponent
        return when (name) {
            ButlerComponent::class.java.name -> requireNotNull(component).plusButlerComponent()
            InputButlerComponent.Factory::class.java.name -> requireNotNull(component).plusInputButlerComponent()
            CategoryListComponent.Factory::class.java.name -> requireNotNull(component).plusCategoryListComponent()
            DetailListComponent.Factory::class.java.name -> requireNotNull(component).plusDetailListComponent()
            ExpandItemCategoryListComponent.Factory::class.java.name -> requireNotNull(component).plusExpandCategoryListComponent()
            StoreInfoComponent.Factory::class.java.name -> requireNotNull(component).plusStoreComponent()
            ZoneInfoComponent.Factory::class.java.name -> requireNotNull(component).plusZoneComponent()
            else -> null
        }
    }
}
