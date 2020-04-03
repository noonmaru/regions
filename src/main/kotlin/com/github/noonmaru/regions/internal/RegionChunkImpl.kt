package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.RegionChunk
import com.github.noonmaru.regions.api.RegionWorld
import com.google.common.collect.ImmutableList

class RegionChunkImpl(
    override val world: RegionWorld,
    override val x: Int,
    override val z: Int
) : RegionChunk {
    override val regions: List<RegionImpl>
        get() = ImmutableList.copyOf(_regions)

    internal val _regions = ArrayList<RegionImpl>(1)

    override fun regionAt(x: Int, y: Int, z: Int): RegionImpl? {
        return _regions.find { it.box.contains(x, y, z) }
    }

    internal fun addRegion(region: RegionImpl) {
        _regions += region
    }

    internal fun removeRegion(region: RegionImpl) {
        _regions -= region
    }
}