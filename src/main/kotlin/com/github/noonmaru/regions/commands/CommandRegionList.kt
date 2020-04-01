package com.github.noonmaru.regions.commands

import com.github.noonmaru.regions.api.Regions
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.pageInformation
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandRegionList : CommandComponent {
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        val page = if (args.hasNext()) {
            args.next().toIntOrNull() ?: 0
        } else {
            0
        }

        val regions = Regions.regions

        val infos = pageInformation(
            list = regions,
            describer = { index, o ->
                "$index. ${ChatColor.AQUA}${o.name} ${ChatColor.RESET}= ${ChatColor.GREEN}${o.world.name} ${ChatColor.GOLD}${o.box}"
            },
            _page = page,
            length = if (sender is Player) 9 else 20,
            name = "REGIONS",
            nameColor = ChatColor.WHITE,
            lineColor = ChatColor.YELLOW
        )

        infos.forEach { sender.sendMessage(it) }

        return true
    }
}