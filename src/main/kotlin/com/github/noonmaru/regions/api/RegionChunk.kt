package com.github.noonmaru.regions.api

interface RegionChunk {
    val world: RegionWorld
    val x: Int
    val z: Int

    val regions: List<Region>

    fun regionAt(x: Int, y: Int, z: Int): Region?
}