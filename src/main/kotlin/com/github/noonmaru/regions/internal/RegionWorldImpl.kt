package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.*
import com.github.noonmaru.regions.toEnumList
import com.github.noonmaru.regions.toStringList
import com.github.noonmaru.regions.util.IntBitSet
import com.github.noonmaru.regions.util.LongHash
import com.github.noonmaru.regions.util.LongObjectHashMap
import com.google.common.collect.ImmutableList
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class RegionWorldImpl(
    private val manager: RegionManagerImpl,
    override val name: String
) : RegionWorld {
    companion object {
        private const val CFG_PROTECTIONS = "protections"
    }

    override var bukkitWorld: World? = null
    override val regions: List<Region>
        get() = ImmutableList.copyOf(_regions)
    override val protections: Set<Protection>
        get() = Collections.unmodifiableSet(_protections.clone())
    private val _regions: MutableSet<Region> = HashSet(0)

    private val _protections = IntBitSet { Protection.getByOffset(it) }

    private val chunks = LongObjectHashMap<RegionChunkImpl>()

    private var mustBeSave = false

    private val file: File
        get() = File(manager.worldsFolder, "$name.yml")

    override fun checkOverlap(box: RegionBox, except: Region?) {
        val overlapList = getOverlapRegions(box, except)
        require(overlapList.isEmpty()) { "Overlap with ${overlapList.joinToString { it.name }}" }
    }

    internal fun placeRegion(region: RegionImpl, box: RegionBox = region.box) {
        _regions.add(region)
        box.forEachChunks { x, z ->
            getOrCreateChunkAt(x, z).addRegion(region)
        }
    }

    internal fun removeRegion(region: RegionImpl) {
        _regions.remove(region)

        val chunks = this.chunks

        region.box.forEachChunks { x, z ->
            val key = LongHash.toLong(x, z)
            chunks.get(key)?.let { chunk ->
                chunk.removeRegion(region)

                if (chunk.isEmpty()) {
                    chunks.remove(key)
                }
            }
        }
    }

    private fun getOrCreateChunkAt(x: Int, z: Int): RegionChunkImpl {
        val chunks = this.chunks
        val key = LongHash.toLong(x, z)

        return chunks[key] ?: RegionChunkImpl(this, x, z).also { chunk ->
            chunks.put(key, chunk)
        }
    }

    override fun chunkAt(x: Int, z: Int): RegionChunkImpl? {
        return chunks.get(LongHash.toLong(x, z))
    }

    override fun regionAt(x: Int, y: Int, z: Int): Region? {
        return chunkAt(x shr 4, z shl 4)?.regionAt(x, y, z)
    }

    override fun getOverlapRegions(box: RegionBox, except: Region?): List<RegionImpl> {
        val map = HashMap<RegionImpl, Boolean>(0)

        box.forEachChunks { x, z ->
            chunkAt(x, z)?._regions?.forEach { region ->
                if (region !== except) {
                    map.computeIfAbsent(region) {
                        box.overlaps(it.box)
                    }
                }
            }
        }

        return ImmutableList.copyOf(map.keys)
    }

    private inline fun RegionBox.forEachChunks(consumer: (x: Int, z: Int) -> Unit) {
        val chunkMinX = minX shr 4
        val chunkMinZ = minZ shr 4
        val chunkMaxX = maxX shr 4
        val chunkMaxZ = maxZ shr 4

        for (x in chunkMinX..chunkMaxX) {
            for (z in chunkMinZ..chunkMaxZ) {
                consumer(x, z)
            }
        }
    }

    override fun hasProtection(protection: Protection): Boolean {
        return _protections.contains(protection)
    }

    override fun addProtection(protections: Collection<Protection>) {
        _protections.addAll(protections)
    }

    override fun removeProtection(protections: Collection<Protection>) {
        _protections.removeAll(protections)
    }

    fun load(file: File) {
        val config = YamlConfiguration.loadConfiguration(file)
        val protections = config.getStringList(CFG_PROTECTIONS).toEnumList({ Protection.getByKey(it) }) { name ->
            Regions.logger.warning("Unknown protection '$name' in world file '${file.name}'")
        }

        _protections.addAll(protections)
    }

    override fun save(): Boolean {
        if (!mustBeSave) return false

        val config = YamlConfiguration()
        config[CFG_PROTECTIONS] = _protections.toStringList()
        val file = file
        file.parentFile.mkdirs()

        val tempFile = File(file.parent, "${file.name}.tmp")
        config.save(tempFile)
        file.delete()
        tempFile.renameTo(file)
        mustBeSave = false
        return true
    }

    fun setMustBeSave() {
        this.mustBeSave = true
    }
}