package com.github.noonmaru.regions.plugin

import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class RegionPlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("Hello Kotlin Plugin!")
    }
}