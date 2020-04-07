package com.github.noonmaru.regions.api

import com.github.noonmaru.regions.internal.RegionManagerImpl
import com.github.noonmaru.regions.plugin.RegionPlugin
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.logging.Logger

object Regions {
    lateinit var manager: RegionManager
        private set

    internal fun initialize(plugin: RegionPlugin) {
        Logger = plugin.logger
        manager = RegionManagerImpl(plugin)
    }
}

internal lateinit var Logger: Logger

internal fun warning(name: String) {
    Logger.warning(name)
}

internal fun info(name: String) {
    Logger.info(name)
}

val Player.user: User
    get() = requireNotNull(Regions.manager.getUser(this)) { "$name is unregistered bukkit player" }

val World.regionWorld: RegionWorld
    get() = requireNotNull(Regions.manager.getRegionWorld(this)) { "$name is unregistered bukkit world" }

val Block.area: Area
    get() = requireNotNull(Regions.manager.areaAt(world, x, y, z)) {
        "Failed to fetch area at ${world.name} $x $y $z"
    }

val Block.region: Region?
    get() = Regions.manager.regionAt(world, x, y, z)

val Location.area: Area
    get() = requireNotNull(Regions.manager.areaAt(world, blockX, blockY, blockZ)) {
        "Failed to fetch area at ${world.name} $blockX $blockY $blockZ"
    }

val Location.region: Region?
    get() = Regions.manager.regionAt(world, blockX, blockY, blockZ)

val Entity.area: Area
    get() = location.area

val Entity.region: Region?
    get() = location.region