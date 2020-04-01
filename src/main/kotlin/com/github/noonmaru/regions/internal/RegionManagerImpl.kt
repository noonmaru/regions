package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.*
import com.github.noonmaru.regions.plugin.RegionPlugin
import com.github.noonmaru.tap.mojang.MojangProfile
import com.github.noonmaru.tap.mojang.getProfile
import com.google.common.collect.ImmutableList
import com.google.common.collect.MapMaker
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.io.File
import java.lang.ref.SoftReference
import java.util.*

class RegionManagerImpl(plugin: RegionPlugin, dataFolder: File) : RegionManager {
    internal val regionsFolder: File
    internal val worldsFolder: File

    override val worlds: List<RegionWorld>
        get() {
            return worldsRef.get()
                ?: ImmutableList.copyOf(worldsByName.values).also {
                    worldsRef = SoftReference(it)
                }
        }
    override val regions: List<Region>
        get() {
            return regionsRef.get()
                ?: ImmutableList.copyOf(regionsByName.values).also {
                    regionsRef = SoftReference(it)
                }
        }
    override val cachedUsers: List<User>
        get() = ImmutableList.copyOf(usersByUniqueId.values)

    private var worldsRef: SoftReference<List<RegionWorld>> = SoftReference(ImmutableList.of())
    private var regionsRef: SoftReference<List<Region>> = SoftReference(ImmutableList.of())

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
    }

    private fun loadRegionWorlds() {
        val worldFiles = worldsFolder.listFiles { file: File ->
            !file.isDirectory && file.name.endsWith(".yml")
        }

        worldFiles?.forEach { worldFile ->
            val name = worldFile.name.removeSuffix(".yml")

            runCatching {
                val config = YamlConfiguration.loadConfiguration(worldFile)
                getOrCreateRegionWorld(name).load(config)
            }
        }

        Bukkit.getWorlds().forEach { world ->
            getOrCreateRegionWorld(world.name).bukkitWorld = world
        }
    }

    private fun loadRegions() {
        regionsFolder.listFiles { file: File ->
            !file.isDirectory && file.name.endsWith(".yml")
        }?.let { regionFiles ->
            val inits = ArrayList<RegionLoader>(regionFiles.count())
            val regionsByName = this.regionsByName

            regionFiles.forEach { regionFile ->
                runCatching {
                    inits += RegionImpl.load(regionFile, this).also { init ->
                        val region = init.region
                        regionsByName[region.name] = region
                    }
                }.onFailure {
                    it.printStackTrace()
                    Regions.logger.warning("Failed to create region for $regionFile.name")
                }
            }

            inits.forEach { init ->
                init.initialize()
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
        require(name.isEmpty()) { "Name is empty" }
        val regionsByName = this.regionsByName
        require(name !in regionsByName) { "Name already in use" }
        val worldImpl = world as RegionWorldImpl
        worldImpl.checkOverlap(box)

        val region = RegionImpl(this, name, worldImpl, box).apply {
            setMustBeSave()
        }
        worldImpl.placeRegion(region)
        regionsByName[name] = region
        regionsRef.clear()

        return region
    }

    override fun removeRegion(name: String): Region? {
        return regionsByName.remove(name)?.also {
            regionsRef.clear()
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
            RegionWorldImpl(this, it)
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
                getOrCreateRegionWorld(world.name).bukkitWorld = world
            }
        }

        @EventHandler
        fun onWorldUnload(event: WorldUnloadEvent) {
            event.world.let {
                getRegionWorld(event.world)?.bukkitWorld = null
            }
        }
    }
}