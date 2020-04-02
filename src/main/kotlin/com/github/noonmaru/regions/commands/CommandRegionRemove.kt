package com.github.noonmaru.regions.commands

import com.github.noonmaru.regions.api.Regions
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class CommandRegionRemove : CommandComponent {
    override val argsCount: Int
        get() = 1

    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        val regionName = args.next()

        val region = Regions.getRegion(regionName)

        if (region == null) {
            sender.sendMessage("등록되지 않은 구역입니다. ${ChatColor.GRAY}$regionName")
            return true
        }

        region.delete()
        sender.sendMessage("[${region.name}] 구역을 제거했습니다.")

        return true
    }
}