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

package com.pyamsoft.fridge.core

import androidx.annotation.CheckResult
import java.util.Calendar
import java.util.Date

const val PRIVACY_POLICY_URL =
    "https://pyamsoft.blogspot.com/p/fridgefriend-privacy-policy.html"
const val TERMS_CONDITIONS_URL =
    "https://pyamsoft.blogspot.com/p/fridgefriend-terms-and-conditions.html"

@CheckResult
fun currentDate(): Date {
    return today().time
}

@CheckResult
fun today(): Calendar {
    return Calendar.getInstance()
}

@CheckResult
inline fun today(func: Calendar.() -> Unit): Calendar {
    return Calendar.getInstance().apply(func)
}
