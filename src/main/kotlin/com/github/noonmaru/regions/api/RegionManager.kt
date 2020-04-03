package com.github.noonmaru.regions.api

import com.github.noonmaru.tap.mojang.MojangProfile
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*


interface RegionManager {
    val cachedUsers: List<User>

    val onlineUsers: List<User>

    val worlds: List<RegionWorld>

    val regions: List<Region>

    fun findUser(uniqueId: UUID): User?

    fun getUser(profile: MojangProfile): User

    fun getUser(player: Player): User?

    fun getRegionWorld(bukkitWorld: World): RegionWorld?

    fun getRegionWorld(name: String): RegionWorld?

    fun getRegion(name: String): Region?

    fun registerNewRegion(name: String, world: RegionWorld, box: RegionBox): Region

    fun removeRegion(name: String): Region?

    fun regionAt(bukkitWorld: World, x: Int, y: Int, z: Int): Region?

    fun areaAt(bukkitWorld: World, x: Int, y: Int, z: Int): Area?

    fun saveAll()

}