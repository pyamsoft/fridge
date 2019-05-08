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
import com.pyamsoft.fridge.db.item.FridgeItem
import com.pyamsoft.fridge.detail.create.title.CreationTitleUiComponent
import com.pyamsoft.fridge.detail.create.toolbar.CreationToolbarUiComponent
import com.pyamsoft.fridge.detail.list.DetailListUiComponent
import com.pyamsoft.pydroid.arch.layout
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.app.requireArguments
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.util.commit
import timber.log.Timber
import javax.inject.Inject

internal class CreationFragment : Fragment(),
    DetailListUiComponent.Callback,
    CreationToolbarUiComponent.Callback,
    CreationTitleUiComponent.Callback {

  @JvmField @Inject internal var toolbar: CreationToolbarUiComponent? = null
  @JvmField @Inject internal var title: CreationTitleUiComponent? = null
  @JvmField @Inject internal var list: DetailListUiComponent? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.layout_constraint, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    val parent = view.findViewById<ConstraintLayout>(R.id.layout_constraint)
    Injector.obtain<FridgeComponent>(view.context.applicationContext)
        .plusDetailComponent()
        .create(requireToolbarActivity(), parent)
        .plusCreationComponent()
        .create(requireArguments().getString(ENTRY_ID, ""))
        .inject(this)

    val list = requireNotNull(list)
    val title = requireNotNull(title)
    val toolbar = requireNotNull(toolbar)
    list.bind(parent, viewLifecycleOwner, savedInstanceState, this)
    title.bind(parent, viewLifecycleOwner, savedInstanceState, this)
    toolbar.bind(parent, viewLifecycleOwner, savedInstanceState, this)

    parent.layout {
      title.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      list.also {
        connect(it.id(), ConstraintSet.TOP, title.id(), ConstraintSet.BOTTOM)
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
    title?.saveState(outState)
    toolbar?.saveState(outState)
    list?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()

    list = null
    title = null
    toolbar = null
  }

  override fun onBack() {
    requireActivity().onBackPressed()
  }

  override fun onError(throwable: Throwable) {
    requireNotNull(list).showError(throwable)
  }

  override fun onExpandItem(
    containerId: Int,
    item: FridgeItem
  ) {
    val fm = childFragmentManager
    if (fm.findFragmentById(containerId) == null) {
      Timber.d("Add new ExpandedFragment for item: $item")
      fm.beginTransaction()
          .replace(
              containerId,
              ExpandedFragment.newInstance(item),
              ExpandedFragment.TAG
          )
          .commit(viewLifecycleOwner)
    }
  }

  override fun onCollapseItem() {
    val fm = childFragmentManager
    val oldFragment: Fragment? = fm.findFragmentByTag(ExpandedFragment.TAG)
    if (oldFragment != null) {
      Timber.d("Remove ExpandedFragment")
      fm.beginTransaction()
          .remove(oldFragment)
          .commit(viewLifecycleOwner)
    }
  }

  companion object {

    const val TAG = "CreationFragment"
    private const val ENTRY_ID = "entry_id"

    @JvmStatic
    @CheckResult
    fun newInstance(id: String): Fragment {
      return CreationFragment().apply {
        arguments = Bundle().apply {
          putString(ENTRY_ID, id)
        }
      }
    }
  }

}
