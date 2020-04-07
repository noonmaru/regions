package com.github.noonmaru.regions.plugin

import com.github.noonmaru.kommand.kommand
import com.github.noonmaru.regions.api.Regions
import com.github.noonmaru.regions.commands.AreaCommands
import com.github.noonmaru.regions.commands.RegionCommands
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class RegionPlugin : JavaPlugin() {
    override fun onEnable() {
        Regions.initialize(this)

        server.pluginManager.apply {
            registerEvents(EventListener(), this@RegionPlugin)
        }

        setupCommands()
    }

    private fun setupCommands() {
        kommand {
            AreaCommands.register(this)
            RegionCommands.register(this)
        }
    }

    override fun onDisable() {
        Regions.manager.saveAll()
    }
}