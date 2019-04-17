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

package com.pyamsoft.fridge.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.pyamsoft.fridge.FridgeComponent
import com.pyamsoft.fridge.R
import com.pyamsoft.fridge.detail.list.DetailListUiComponent
import com.pyamsoft.fridge.detail.shop.toolbar.ShoppingToolbarUiComponent
import com.pyamsoft.pydroid.arch.layout
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import javax.inject.Inject

internal class ShoppingFragment : Fragment(),
  ShoppingToolbarUiComponent.Callback,
  DetailListUiComponent.Callback {

  @field:Inject internal lateinit var toolbar: ShoppingToolbarUiComponent
  @field:Inject internal lateinit var list: DetailListUiComponent

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.layout_constraint, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val parent = view.findViewById<ConstraintLayout>(R.id.layout_constraint)
    Injector.obtain<FridgeComponent>(view.context.applicationContext)
      .plusDetailComponent()
      .create(requireToolbarActivity(), parent)
      .plusShoppingComponent()
      .create()
      .inject(this)

    list.bind(parent, viewLifecycleOwner, savedInstanceState, this)
    toolbar.bind(parent, viewLifecycleOwner, savedInstanceState, this)

    parent.layout {
      list.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    list.saveState(outState)
    toolbar.saveState(outState)
  }

  override fun onBack() {
    requireActivity().onBackPressed()
  }

  companion object {

    const val TAG = "ShoppingFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return ShoppingFragment().apply {
        arguments = Bundle().apply {
        }
      }
    }
  }

}
