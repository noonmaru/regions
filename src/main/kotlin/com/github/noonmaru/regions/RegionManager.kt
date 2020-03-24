package com.github.noonmaru.regions

import org.bukkit.World as BukkitWorld

interface RegionManager {
    val worldsByWorld: Map<BukkitWorld, World>
    val regionsByName: Map<String, Region>
    val regions: List<Region>

    fun createRegion(name: String, box: Box)

    fun regionAt(bukkitWorld: BukkitWorld, x: Int, y: Int, z: Int)
}