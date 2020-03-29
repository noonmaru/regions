package com.github.noonmaru.regions.plugin

import com.github.noonmaru.regions.api.Regions
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class RegionPlugin : JavaPlugin() {
    override fun onEnable() {
        Regions.initialize(this)
    }
}