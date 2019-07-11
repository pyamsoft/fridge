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

package com.pyamsoft.fridge.locator.map.osm.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

internal sealed class OsmNodeOrWay {

  @JsonClass(generateAdapter = true)
  internal data class Node internal constructor(
    internal val id: Long,
    internal val lat: Double,
    internal val lon: Double,
    internal val tags: OsmTags
  ) : OsmNodeOrWay() {

    // Needed to generate static adapter
    companion object

  }

  @JsonClass(generateAdapter = true)
  internal data class Way internal constructor(
    internal val id: Long,
    internal val nodes: List<Long>,
    internal val tags: OsmTags
  ) : OsmNodeOrWay() {

    // Needed to generate static adapter
    companion object

  }

  @JsonClass(generateAdapter = true)
  internal data class AsJson internal constructor(
    internal val id: Long,
    internal val nodes: List<Long>?,
    internal val lat: Double?,
    internal val lon: Double?,
    internal val tags: OsmTags?
  ) : OsmNodeOrWay() {

    // Needed to generate static adapter
    companion object

  }

  internal class Adapter {

    @FromJson
    @Suppress("unused")
    fun fromJson(json: AsJson): OsmNodeOrWay {
      val nodes = json.nodes ?: emptyList()
      val tags = json.tags ?: OsmTags("")
      val lat = json.lat ?: 0.0
      val lon = json.lon ?: 0.0

      if (nodes.isNotEmpty()) {
        return Way(json.id, nodes, tags)
      } else {
        return Node(json.id, lat, lon, tags)
      }
    }

    @ToJson
    @Suppress("unused")
    fun toJson(payload: OsmNodeOrWay): AsJson {
      return when (payload) {
        is Node -> AsJson(payload.id, null, payload.lat, payload.lon, payload.tags)
        is Way -> AsJson(payload.id, payload.nodes, 0.0, 0.0, payload.tags)
        else -> throw IllegalArgumentException("Can only convert Node or Way to AsJson")
      }
    }
  }

}
