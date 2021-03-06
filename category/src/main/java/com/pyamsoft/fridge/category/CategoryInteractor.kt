/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.fridge.category

import androidx.annotation.CheckResult
import com.pyamsoft.fridge.db.category.FridgeCategory
import com.pyamsoft.fridge.db.category.FridgeCategoryQueryDao
import com.pyamsoft.fridge.db.item.FridgeItem
import com.pyamsoft.fridge.db.item.FridgeItemQueryDao
import com.pyamsoft.fridge.db.persist.PersistentCategories
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class CategoryInteractor
@Inject
internal constructor(
    private val persistentCategories: PersistentCategories,
    private val categoryQueryDao: FridgeCategoryQueryDao,
    private val itemQueryDao: FridgeItemQueryDao
) {

  @CheckResult
  private suspend fun loadFridgeCategories(): List<FridgeCategory> {
    Enforcer.assertOffMainThread()
    persistentCategories.guaranteePersistentCategoriesCreated()
    return categoryQueryDao.query(false)
  }

  @CheckResult
  private suspend fun loadFridgeItems(): List<FridgeItem> {
    Enforcer.assertOffMainThread()
    return itemQueryDao.query(false)
  }

  @CheckResult
  suspend fun loadCategories(): ResultWrapper<List<CategoryViewState.CategoryItemsPairing>> =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val categories = loadFridgeCategories()
          val items = loadFridgeItems()
          ResultWrapper.success(
              categories.map { category ->
                CategoryViewState.CategoryItemsPairing(
                    category,
                    items
                        .asSequence()
                        .filterNot { it.categoryId() == null }
                        .filter { it.categoryId() == category.id() }
                        .toList())
              })
        } catch (e: Throwable) {
          Timber.e(e, "Error loading categories")
          ResultWrapper.failure(e)
        }
      }
}
