package com.github.noonmaru.regions.api

import org.bukkit.World

interface RegionWorld : Protectible {
    val name: String

    val bukkitWorld: World?

    val regions: List<Region>

    fun chunkAt(x: Int, z: Int): RegionChunk?

    fun regionAt(x: Int, y: Int, z: Int): Region?

    fun getOverlapRegions(box: RegionBox, except: Region? = null): List<Region>

    fun checkOverlap(box: RegionBox, except: Region? = null)

    fun save(): Boolean
}