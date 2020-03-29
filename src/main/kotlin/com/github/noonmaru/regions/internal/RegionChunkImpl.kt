package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.regions.api.RegionChunk
import com.github.noonmaru.regions.api.RegionWorld
import com.google.common.collect.ImmutableList

class RegionChunkImpl(
    world: RegionWorld,
    override val x: Int,
    override val z: Int
) : RegionChunk {

    override val world: RegionWorld
        get() = worldRef.get()
    override val regions: List<Region>
        get() = ImmutableList.copyOf(_regions)

    private val worldRef = UpstreamReference(world)
    internal val _regions: MutableList<RegionImpl> = ArrayList(0)

    override fun regionAt(x: Int, y: Int, z: Int): Region? {
        return regions.find { it.box.contains(x, y, z) }
    }

    fun addRegion(region: RegionImpl) {
        _regions.add(region)
    }

    fun removeRegion(region: RegionImpl) {
        _regions.remove(region)
    }

    fun isEmpty(): Boolean {
        return _regions.isEmpty()
    }
}