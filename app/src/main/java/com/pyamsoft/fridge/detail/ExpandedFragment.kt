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
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment

class ExpandedFragment : Fragment() {

  companion object {

    const val TAG = "ExpandedFragment"
    private const val ENTRY_ID = "entry_id"
    private const val ITEM_ID = "item_id"

    @JvmStatic
    @CheckResult
    fun newInstance(
      entryId: String,
      itemId: String
    ): Fragment {
      return CreationFragment().apply {
        arguments = Bundle().apply {
          putString(ENTRY_ID, entryId)
          putString(ITEM_ID, itemId)
        }
      }
    }
  }

}
