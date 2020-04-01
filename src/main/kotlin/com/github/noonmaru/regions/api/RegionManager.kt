package com.github.noonmaru.regions.api

import com.github.noonmaru.regions.internal.RegionImpl
import com.github.noonmaru.tap.mojang.MojangProfile
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*


interface RegionManager {
    val worlds: List<RegionWorld>

    val regions: List<Region>

    val cachedUsers: List<User>

    fun getRegionWorld(bukkitWorld: World): RegionWorld?

    fun getRegionWorld(name: String): RegionWorld?

    fun createRegion(name: String, world: RegionWorld, box: RegionBox): Region

    fun removeRegion(name: String): Region?

    fun regionAt(bukkitWorld: World, x: Int, y: Int, z: Int): Region?

    fun getUser(uniqueId: UUID): User?

    fun getUser(profile: MojangProfile): User

    fun getUser(player: Player): User

    fun getRegion(parentName: String): RegionImpl?

    fun save()
}