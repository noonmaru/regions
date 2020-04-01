package com.github.noonmaru.regions.api

import com.github.noonmaru.regions.internal.RegionImpl
import com.github.noonmaru.regions.internal.RegionManagerImpl
import com.github.noonmaru.regions.plugin.RegionPlugin
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.logging.Logger

object Regions {
    lateinit var logger: Logger
        private set
    lateinit var manager: RegionManager
        private set

    val regions: List<Region>
        get() = manager.regions

    internal fun initialize(plugin: RegionPlugin) {
        logger = plugin.logger
        manager = RegionManagerImpl(plugin, plugin.dataFolder)
    }

    fun createRegion(name: String, world: RegionWorld, box: RegionBox): Region {
        return manager.createRegion(name, world, box)
    }

    fun getWorld(name: String): RegionWorld? {
        return manager.getRegionWorld(name)
    }

    fun getRegion(name: String): RegionImpl? {
        return manager.getRegion(name)
    }
}

val World.regionWorld: RegionWorld
    get() = requireNotNull(Regions.manager.getRegionWorld(this)) { "Unloaded world $name" }

val Player.user: User
    get() = requireNotNull(Regions.manager.getUser(this)) { "Offline player $name" }

val Block.region: Region?
    get() = Regions.manager.regionAt(world, x, y, z)

val Location.region: Region?
    get() = Regions.manager.regionAt(world, blockX, blockY, blockZ)

