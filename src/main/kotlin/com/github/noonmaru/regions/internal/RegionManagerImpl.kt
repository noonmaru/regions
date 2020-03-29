package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.*
import com.github.noonmaru.regions.plugin.RegionPlugin
import com.github.noonmaru.tap.mojang.MojangProfile
import com.google.common.collect.ImmutableList
import com.google.common.collect.MapMaker
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File
import java.lang.ref.SoftReference
import java.util.*

class RegionManagerImpl(plugin: RegionPlugin, internal val regionsFolder: File) : RegionManager {
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
        plugin.server.apply {
            pluginManager.registerEvents(PlayerListener(), plugin)
        }
    }

    override fun getRegionWorld(bukkitWorld: World): RegionWorld? {
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

        val region = RegionImpl(this, name, worldImpl, box)
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
}