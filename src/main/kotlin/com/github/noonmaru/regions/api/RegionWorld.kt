package com.github.noonmaru.regions.api

import org.bukkit.World

interface RegionWorld : Area {
    val bukkitWorld: World?

    val regions: List<Region>

    fun chunkAt(chunkX: Int, chunkZ: Int): RegionChunk?

    fun regionAt(x: Int, y: Int, z: Int): Region?

    fun getOverlapRegions(box: RegionBox, except: Region? = null): Set<Region>

    fun checkOverlap(box: RegionBox, except: Region? = null) {
        getOverlapRegions(box, except).let { overlaps ->
            require(overlaps.isEmpty()) {
                "Overlap with ${overlaps.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { it.name }}"
            }
        }
    }
}