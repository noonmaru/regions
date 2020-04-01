package com.github.noonmaru.regions.api

import kotlin.math.max
import kotlin.math.min

class RegionBox(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) {
    val minX: Int = min(minX, maxX)
    val minY: Int = min(minY, maxY)
    val minZ: Int = min(minZ, maxZ)
    val maxX: Int = max(minX, maxX)
    val maxY: Int = max(minY, maxY)
    val maxZ: Int = max(minZ, maxZ)

    fun expand(x: Int, y: Int, z: Int): RegionBox {
        return RegionBox(
            minX - x,
            minY - y,
            minZ - z,
            maxX + x,
            maxY + y,
            maxZ + z
        )
    }

    fun contains(x: Int, y: Int, z: Int): Boolean {
        return x in minX..maxX && y in minY..maxY && z in minZ..maxZ
    }

    fun overlaps(
        minX: Int,
        minY: Int,
        minZ: Int,
        maxX: Int,
        maxY: Int,
        maxZ: Int
    ): Boolean {
        return this.minX <= maxX && this.maxX >= minX && this.minY <= maxY && this.maxY >= minY && this.minZ <= maxZ && this.maxZ >= minZ
    }

    fun overlaps(box: RegionBox): Boolean {
        return box.run {
            this@RegionBox.overlaps(minX, minY, minZ, maxX, maxY, maxZ)
        }
    }

    override fun toString(): String {
        return "[$minX, $minY, $minZ - $maxX, $maxY, $maxZ]"
    }

}