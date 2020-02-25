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

package com.pyamsoft.fridge.butler

import android.app.Notification
import android.content.Context
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.pyamsoft.fridge.db.item.FridgeItem

internal object ButlerNotifications {

    @JvmStatic
    @CheckResult
    fun getExtraItems(items: List<FridgeItem>): String {
        return when (items.size) {
            1 -> ""
            2 -> "and '${items[1].name()}'"
            else -> "and ${items.size - 1} other items"
        }
    }

    @CheckResult
    @JvmStatic
    fun notify(
        notificationId: Int,
        handler: NotificationHandler,
        context: Context,
        channelId: String,
        channelTitle: String,
        channelDescription: String,
        presence: FridgeItem.Presence,
        createNotification: (builder: NotificationCompat.Builder) -> Notification
    ): Boolean {
        Notifications.notify(
            notificationId,
            handler,
            context,
            R.drawable.ic_get_app_24dp,
            channelId,
            channelTitle,
            channelDescription,
            presence,
            createNotification
        )
        return true
    }
}
