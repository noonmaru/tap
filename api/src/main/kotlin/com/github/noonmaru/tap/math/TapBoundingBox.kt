/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.math

import org.bukkit.Location

@Suppress("MemberVisibilityCanBePrivate")
class TapBoundingBox(val center: Location, val x: Double, val y: Double, val z: Double) {
    val minX = center.x - x
    val minY = center.y - y
    val minZ = center.z - z
    val maxX = center.x + x
    val maxY = center.y + y
    val maxZ = center.z + z

    fun contains(x: Double, y: Double, z: Double): Boolean {
        return x in minX..maxX && y in minY..maxY && z in minZ..maxZ
    }
}