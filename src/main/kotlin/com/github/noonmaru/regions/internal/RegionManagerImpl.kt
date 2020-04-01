package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.*
import com.github.noonmaru.regions.plugin.RegionPlugin
import com.github.noonmaru.regions.util.softCache
import com.github.noonmaru.tap.mojang.MojangProfile
import com.github.noonmaru.tap.mojang.getProfile
import com.google.common.collect.ImmutableList
import com.google.common.collect.MapMaker
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.io.File
import java.util.*

class RegionManagerImpl(plugin: RegionPlugin, dataFolder: File) : RegionManager {
    internal val regionsFolder: File
    internal val worldsFolder: File

    private val cachedWorlds = softCache { ImmutableList.copyOf(worldsByName.values) }
    override val worlds: List<RegionWorld> by cachedWorlds

    private val cachedRegions = softCache { ImmutableList.copyOf(regionsByName.values) }
    override val regions: List<Region> by cachedRegions

    override val cachedUsers: List<User>
        get() = ImmutableList.copyOf(usersByUniqueId.values)

    private val worldsByName = TreeMap<String, RegionWorldImpl>(String.CASE_INSENSITIVE_ORDER)
    private val worldsByBukkitWorld = HashMap<World, RegionWorldImpl>()
    private val regionsByName = TreeMap<String, RegionImpl>(String.CASE_INSENSITIVE_ORDER)
    private val usersByUniqueId = MapMaker().weakValues().makeMap<UUID, UserImpl>()
    private val usersByPlayer = IdentityHashMap<Player, UserImpl>(Bukkit.getMaxPlayers())

    init {
        plugin.server.pluginManager.apply {
            registerEvents(PlayerListener(), plugin)
            registerEvents(WorldListener(), plugin)
        }

        regionsFolder = File(dataFolder, "regions").apply { mkdirs() }
        worldsFolder = File(dataFolder, "worlds").apply { mkdirs() }

        loadRegionWorlds()
        loadRegions()
    }

    private fun loadRegionWorlds() {
        val worldFiles = worldsFolder.listFiles { file: File ->
            !file.isDirectory && file.name.endsWith(".yml")
        }

        worldFiles?.forEach { worldFile ->
            val name = worldFile.name.removeSuffix(".yml")

            runCatching {
                worldsByName[name] = RegionWorldImpl(this, name)
            }
        }

        Bukkit.getWorlds().forEach { world ->
            val regionWorld = getOrCreateRegionWorld(world.name)
            regionWorld.bukkitWorld = world
            worldsByBukkitWorld[world] = regionWorld
        }
    }

    private fun loadRegions() {
        regionsFolder.listFiles { file: File ->
            !file.isDirectory && file.name.endsWith(".yml")
        }?.let { regionFiles ->
            val loaders = ArrayList<RegionLoader>(regionFiles.count())
            val regionsByName = this.regionsByName

            regionFiles.forEach { regionFile ->
                runCatching {
                    val loader = RegionImpl.load(regionFile, this)
                    val region = loader.region
                    val world = region.world

                    world.checkOverlap(region.box)
                    world.placeRegion(region)
                    regionsByName[region.name] = region

                    loaders += loader
                }.onFailure {
                    it.printStackTrace()
                    Regions.logger.warning("Failed to load region for $regionFile.name")
                }
            }

            loaders.forEach { loader ->
                loader.load()
            }
        }
    }

    override fun getRegionWorld(bukkitWorld: World): RegionWorldImpl? {
        return worldsByBukkitWorld[bukkitWorld]
    }

    override fun getRegionWorld(name: String): RegionWorld? {
        return worldsByName[name]
    }

    override fun createRegion(name: String, world: RegionWorld, box: RegionBox): Region {
        require(name.isNotEmpty()) { "Name is empty" }
        val regionsByName = this.regionsByName
        require(name !in regionsByName) { "Name already in use" }
        val worldImpl = world as RegionWorldImpl
        worldImpl.checkOverlap(box)

        val region = RegionImpl(this, name, worldImpl, box).apply {
            setMustBeSave()
        }
        worldImpl.placeRegion(region)
        regionsByName[name] = region
        cachedRegions.clear()

        return region
    }

    override fun removeRegion(name: String): Region? {
        return regionsByName.remove(name)?.also {
            cachedRegions.clear()
            it.destroy()
        }
    }

    override fun regionAt(bukkitWorld: World, x: Int, y: Int, z: Int): Region? {
        return getRegionWorld(bukkitWorld)?.regionAt(x, y, z)
    }

    override fun getUser(uniqueId: UUID): UserImpl? {
        return usersByUniqueId[uniqueId]
    }

    override fun getUser(profile: MojangProfile): UserImpl {
        return usersByUniqueId.computeIfAbsent(profile.uniqueId) { uniqueId ->
            UserImpl(uniqueId, profile.name)
        }
    }

    override fun getUser(player: Player): UserImpl {
        return usersByUniqueId.computeIfAbsent(player.uniqueId) { uniqueId ->
            UserImpl(uniqueId, player.name)
        }
    }

    override fun save() {
        worlds.forEach {
            it.runCatching { save() }
        }

        regions.forEach {
            it.runCatching { save() }
        }
    }

    private inner class PlayerListener : Listener {
        @EventHandler
        fun onPlayerJoin(event: PlayerJoinEvent) {
            val player = event.player
            val user = getUser(player)
            user.bukkitPlayer = player
            usersByPlayer[player] = user
        }

        @EventHandler
        fun onPlayerQuit(event: PlayerQuitEvent) {
            val player = event.player
            usersByPlayer.remove(player)?.let { user ->
                user.bukkitPlayer = null
            }
        }
    }

    internal fun getOrCreateRegionWorld(name: String): RegionWorldImpl {
        return worldsByName.computeIfAbsent(name) {
            RegionWorldImpl(this, it).apply {
                setMustBeSave()
                cachedWorlds.clear()
            }
        }
    }

    fun getOrCreateUser(uniqueId: UUID): UserImpl? {
        return usersByUniqueId.computeIfAbsent(uniqueId) {
            val profile = getProfile(it)

            if (profile == null) null
            else UserImpl(profile.uniqueId, profile.name)
        }
    }

    override fun getRegion(parentName: String): RegionImpl? {
        return regionsByName[parentName]
    }

    private inner class WorldListener : Listener {
        @EventHandler
        fun onWorldLoad(event: WorldLoadEvent) {
            event.world.let { world ->
                val regionWorld = getOrCreateRegionWorld(world.name)
                worldsByBukkitWorld[world] = regionWorld
            }
        }

        @EventHandler
        fun onWorldUnload(event: WorldUnloadEvent) {
            event.world.let { world ->
                worldsByBukkitWorld.remove(world)?.let { it.bukkitWorld = null }
            }
        }
    }
}