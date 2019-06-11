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

import android.os.Bundle
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.pyamsoft.fridge.BuildConfig
import com.pyamsoft.fridge.FridgeComponent
import com.pyamsoft.fridge.R
import com.pyamsoft.fridge.butler.Butler
import com.pyamsoft.fridge.entry.EntryFragment
import com.pyamsoft.pydroid.arch.doOnDestroy
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.rating.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.rating.RatingActivity
import com.pyamsoft.pydroid.ui.rating.buildChangeLog
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

internal class MainActivity : RatingActivity() {

  override val applicationIcon: Int = R.mipmap.ic_launcher

  override val versionName: String = BuildConfig.VERSION_NAME

  override val changeLogLines: ChangeLogBuilder = buildChangeLog {

  }

  override val applyFluidResizer: Boolean = false

  override val fragmentContainerId: Int
    get() = requireNotNull(container).id()

  override val snackbarRoot: ViewGroup
    get() = requireNotNull(snackbarContainer)

  // Nullable to prevent memory leak
  private var snackbarContainer: CoordinatorLayout? = null

  @JvmField @Inject internal var toolbar: MainToolbar? = null
  @JvmField @Inject internal var container: FragmentContainer? = null
  @JvmField @Inject internal var butler: Butler? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_Fridge_Normal)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.snackbar_screen)

    snackbarContainer = findViewById(R.id.snackbar_container)
    val contentContainer = findViewById<ConstraintLayout>(R.id.content_container)

    Injector.obtain<FridgeComponent>(applicationContext)
        .plusMainComponent()
        .create(this, contentContainer, this)
        .inject(this)

    inflateComponents(contentContainer, savedInstanceState)

    pushFragment()
  }

  override fun onStart() {
    super.onStart()
    requireNotNull(butler).apply {
      cancelExpirationReminder()
      remindExpiration(1, SECONDS)
    }
  }

  private fun inflateComponents(
    constraintLayout: ConstraintLayout,
    savedInstanceState: Bundle?
  ) {
    val container = requireNotNull(container)
    val toolbar = requireNotNull(toolbar)
    val dropshadow = DropshadowView.create(constraintLayout)

    container.inflate(savedInstanceState)
    toolbar.inflate(savedInstanceState)
    dropshadow.inflate(savedInstanceState)

    this.doOnDestroy {
      container.teardown()
      toolbar.teardown()
      dropshadow.teardown()
    }

    constraintLayout.layout {

      toolbar.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      dropshadow.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      container.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
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
      fm.commit(this) {
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
    snackbarContainer = null
    toolbar = null
    container = null
    butler = null
  }

}
