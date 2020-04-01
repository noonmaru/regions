package com.github.noonmaru.regions.commands

import com.github.noonmaru.regions.api.Protection
import com.github.noonmaru.regions.api.Regions
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

class CommandRegionProtection : CommandComponent {
    override val argsCount: Int
        get() = 1

    // /region protection <region> <add | remove> <Protections...>
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        val regionName = args.next()

        val region = Regions.getRegion(regionName)

        if (region == null) {
            sender.sendMessage("등록되지 않은 구역입니다. ${ChatColor.GRAY}$regionName")
            return true
        }

        if (args.hasNext()) {
            val op = args.next()

            if (op.equals("add", true) || op.equals("remove", true)) {
                val protections = TreeSet<Protection>()

                while (args.hasNext()) {
                    val key = args.next()
                    val protection = Protection.getByKey(key)

                    if (protection == null) {
                        sender.sendMessage("알 수 없는 보호 옵션입니다 ${ChatColor.GRAY}${key}")
                        return true
                    }

                    protections += protection
                }

                if (op.equals("add", true)) {
                    region.addProtection(protections)
                    sender.sendMessage("${region.name} 구역에 보호를 설정했습니다..")

                } else /*remove*/ {
                    region.removeProtection(protections)
                    sender.sendMessage("${region.name} 구역에서 보호를 제거했습니다.")
                }

                region.save()

                for (protection in protections) {
                    sender.sendMessage("  - ${protection.key}")
                }

                return true
            }
        } else {
            sender.sendMessage("다음은 ${region.name} 구역에 적용된 보호 목록입니다.")

            for (protection in region.protections) {
                sender.sendMessage("  - ${protection.name}")
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): List<String> {
        val last = args.last()

        return when (args.remain()) {
            1 -> Regions.regions.tabComplete(last) { it.name }
            2 -> listOf("add", "remove").tabComplete(last)
            else -> Protection.values().asList().tabComplete(last) { it.toString() }
        }
    }
}