package com.github.noonmaru.regions.api

import com.github.noonmaru.regions.internal.RegionManagerImpl
import com.github.noonmaru.regions.plugin.RegionPlugin
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.io.File
import java.util.logging.Logger

object Regions {
    lateinit var manager: RegionManager
        private set
    lateinit var logger: Logger
        private set

    internal fun initialize(plugin: RegionPlugin) {
        val regionsFolder = File(plugin.dataFolder, "regions")

        manager = RegionManagerImpl(plugin, regionsFolder)
        logger = plugin.logger
    }
}

val Player.user: User
    get() = requireNotNull(Regions.manager.getUser(this)) { "Offline player $name" }

val Block.region: Region?
    get() = Regions.manager.regionAt(world, x, y, z)

val Location.region: Region?
    get() = Regions.manager.regionAt(world, blockX, blockY, blockZ)

