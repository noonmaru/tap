/*
 * Copyright (c) $date.year Noonmaru
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

package com.github.noonmaru.tap

import com.comphenix.protocol.utility.MinecraftVersion
import com.github.noonmaru.tap.attach.Tools
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        Tools.loadAttachLibrary(dataFolder)

        try {
            classLoader.loadClass("com.comphenix.protocol.wrappers.Pair")
        } catch (exception: ClassNotFoundException) {
            if (MinecraftVersion.getCurrentVersion().minor > 15) {
                throw UnsupportedOperationException("If you are using 1.16 or later, please use the latest" +
                        " ProtocolLib snapshot build from: https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/")
            }
        }
    }
}
