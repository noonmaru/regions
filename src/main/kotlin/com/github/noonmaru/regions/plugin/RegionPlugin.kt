package com.github.noonmaru.regions.plugin

import com.github.noonmaru.regions.api.Regions
import com.github.noonmaru.regions.commands.CommandAdd
import com.github.noonmaru.regions.commands.CommandRegionList
import com.github.noonmaru.regions.commands.CommandRegionProtection
import com.github.noonmaru.tap.command.command
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

        command("region") {
            component("add") {
                usage = "<Name>"
                description = "구역을 추가합니다."
                CommandAdd()
            }
            component("list") {
                usage = "[Page]"
                description = "구역 목록을 확인합니다."
                CommandRegionList()
            }
            component("protection") {
                usage = "<Region> [add | remove] [Protections...]"
                description = "구역 보호를 확인 혹은 설정합니다."
                CommandRegionProtection()
            }
        }
    }

    override fun onDisable() {
        Regions.manager.save()
    }
}