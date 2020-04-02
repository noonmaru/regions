package com.github.noonmaru.regions.commands

import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.regions.api.Regions
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

interface CommandComponentRegion : CommandComponent {
    override val argsCount: Int
        get() = 1

    override fun onCommand(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): Boolean {
        val regionName = args.next()
        val region = Regions.getRegion(regionName)

        if (region == null) {
            sender.sendMessage("등록되지 않은 구역입니다. ${ChatColor.GRAY}$regionName")
            return true
        }

        return onCommand(sender, label, componentLabel, region, args)
    }

    fun onCommand(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        region: Region,
        args: ArgumentList
    ): Boolean

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): List<String> {
        val regionName = args.next()

        if (!args.hasNext())
            return Regions.regions.tabComplete(regionName) { it.name }

        val region = Regions.getRegion(regionName)

        return onTabComplete(sender, label, componentLabel, region, args)
    }

    fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        region: Region?,
        args: ArgumentList
    ): List<String>
}