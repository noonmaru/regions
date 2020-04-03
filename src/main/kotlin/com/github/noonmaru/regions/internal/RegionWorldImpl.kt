package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Area
import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.regions.api.RegionBox
import com.github.noonmaru.regions.api.RegionWorld
import com.github.noonmaru.regions.util.LongHash
import com.github.noonmaru.regions.util.LongObjectHashMap
import com.google.common.collect.ImmutableList
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RegionWorldImpl(
    manager: RegionManagerImpl,
    name: String
) : AreaImpl(manager, name), RegionWorld {
    override val file: File
        get() = File(manager.worldsFolder, "$name.yml")
    override val type: Area.Type
        get() = Area.Type.WORLD
    override var bukkitWorld: World? = null
        internal set
    override val regions: List<Region>
        get() = ImmutableList.copyOf(_regions)

    private val _regions = TreeSet<RegionImpl> { o1, o2 -> o1.name.compareTo(o2.name) }
    private val _chunks = LongObjectHashMap<RegionChunkImpl>()

    internal fun placeRegion(region: RegionImpl, box: RegionBox = region.box) {
        _regions.add(region)

        box.forEach { chunkX, chunkZ ->
            getOrGenerateChunk(chunkX, chunkZ).addRegion(region)
        }
    }

    private fun getOrGenerateChunk(chunkX: Int, chunkZ: Int): RegionChunkImpl {
        val chunks = _chunks
        val key = LongHash.toLong(chunkX, chunkZ)

        var chunk = chunks.get(key)

        if (chunk == null) {
            chunk = RegionChunkImpl(this, chunkX, chunkZ).also {
                chunks.put(key, it)
            }
        }

        return chunk
    }

    internal fun removeRegion(region: RegionImpl) {
        _regions.remove(region)

        region.box.forEach { chunkX, chunkZ ->
            chunkAt(chunkX, chunkZ)?.removeRegion(region)
        }
    }

    override fun chunkAt(chunkX: Int, chunkZ: Int): RegionChunkImpl? {
        return _chunks[LongHash.toLong(chunkX, chunkZ)]
    }

    override fun regionAt(x: Int, y: Int, z: Int): RegionImpl? {
        return chunkAt(x.toChunk(), z.toChunk())?.regionAt(x, y, z)
    }

    override fun getOverlapRegions(box: RegionBox, except: Region?): List<Region> {
        val overlaps = ArrayList<RegionImpl>(0)

        box.forEach { chunkX, chunkZ ->
            chunkAt(chunkX, chunkZ)?.let { chunk ->
                chunk._regions.forEach { region ->
                    if (box.overlaps(region.box)) {
                        overlaps += region
                    }
                }
            }
        }

        return overlaps
    }

    companion object {
        internal fun load(file: File, manager: RegionManagerImpl): RegionWorldImpl {
            val name = file.name.removeSuffix(".yml")
            val config = YamlConfiguration.loadConfiguration(file)

            return RegionWorldImpl(manager, name).apply {
                load(config)
            }
        }
    }
}

fun RegionWorld.toImpl(): RegionWorldImpl {
    return this as RegionWorldImpl
}

fun Int.toChunk(): Int {
    return this shr 4
}

inline fun RegionBox.forEach(action: (chunkX: Int, chunkZ: Int) -> Unit) {
    val chunkMinX = minX.toChunk()
    val chunkMinZ = minZ.toChunk()
    val chunkMaxX = maxX.toChunk()
    val chunkMaxZ = maxZ.toChunk()

    for (chunkX in chunkMinX..chunkMaxX) {
        for (chunkZ in chunkMinZ..chunkMaxZ) {
            action(chunkX, chunkZ)
        }
    }
}