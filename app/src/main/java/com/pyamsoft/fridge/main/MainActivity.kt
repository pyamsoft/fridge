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

package com.pyamsoft.fridge.main

import android.content.ComponentCallbacks2
import android.os.Bundle
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.pyamsoft.fridge.BuildConfig
import com.pyamsoft.fridge.FridgeComponent
import com.pyamsoft.fridge.R
import com.pyamsoft.fridge.butler.Butler
import com.pyamsoft.fridge.butler.ForegroundState
import com.pyamsoft.fridge.entry.EntryFragment
import com.pyamsoft.pydroid.arch.UnitViewModel
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.rating.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.rating.RatingActivity
import com.pyamsoft.pydroid.ui.rating.buildChangeLog
import com.pyamsoft.pydroid.ui.util.commitNow
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.util.makeWindowSexy
import timber.log.Timber
import javax.inject.Inject

internal class MainActivity : RatingActivity() {

  override val checkForUpdates: Boolean = false

  override val applicationIcon: Int = R.mipmap.ic_launcher

  override val versionName: String = BuildConfig.VERSION_NAME

  override val changeLogLines: ChangeLogBuilder = buildChangeLog {

  }

  override val fragmentContainerId: Int
    get() = requireNotNull(container).id()

  override val snackbarRoot: ViewGroup
    get() {
      val entryFragment = supportFragmentManager.findFragmentByTag(EntryFragment.TAG)
      if (entryFragment is SnackbarContainer) {
        val snackbarContainer = entryFragment.getSnackbarContainer()
        if (snackbarContainer != null) {
          Timber.d("SnackbarContainer provided by ${EntryFragment.TAG}")
          return snackbarContainer
        }
      }

      Timber.d("Fallback SnackbarContainer")
      return requireNotNull(rootView)
    }

  private var rootView: ConstraintLayout? = null
  @JvmField @Inject internal var foregroundState: ForegroundState? = null
  @JvmField @Inject internal var toolbar: MainToolbar? = null
  @JvmField @Inject internal var container: FragmentContainer? = null
  @JvmField @Inject internal var butler: Butler? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_Fridge_Normal)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_constraint)

    val view = findViewById<ConstraintLayout>(R.id.layout_constraint)
    rootView = view

    Injector.obtain<FridgeComponent>(applicationContext)
        .plusMainComponent()
        .create(this, view, this)
        .inject(this)

    view.makeWindowSexy()
    inflateComponents(view, savedInstanceState)

    pushFragment()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    Timber.d("ON BACK PRESSED")
  }

  override fun onStart() {
    super.onStart()
    Timber.d("App enters foreground onStart")
    requireNotNull(foregroundState).isForeground = true
  }

  override fun onStop() {
    super.onStop()

    Timber.d("App enters background onStop")
    requireNotNull(foregroundState).isForeground = false
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
      Timber.d("App enters background UI_HIDDEN")
      requireNotNull(foregroundState).isForeground = false
    }
  }

  private fun inflateComponents(
    constraintLayout: ConstraintLayout,
    savedInstanceState: Bundle?
  ) {
    val container = requireNotNull(container)
    val toolbar = requireNotNull(toolbar)

    createComponent(
        savedInstanceState, this, UnitViewModel.create(),
        container, toolbar
    ) {}

    constraintLayout.layout {

      toolbar.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      container.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }
    }
  }

  private fun pushFragment() {
    val fm = supportFragmentManager
    if (fm.findFragmentById(fragmentContainerId) == null) {
      fm.commitNow(this) {
        add(fragmentContainerId, EntryFragment.newInstance(), EntryFragment.TAG)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    toolbar?.saveState(outState)
    container?.saveState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    rootView = null
    toolbar = null
    container = null
    butler = null
    foregroundState = null
  }

}
