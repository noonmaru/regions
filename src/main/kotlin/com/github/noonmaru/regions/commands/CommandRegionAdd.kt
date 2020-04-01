package com.github.noonmaru.regions.commands

import com.github.noonmaru.regions.api.RegionBox
import com.github.noonmaru.regions.api.Regions
import com.github.noonmaru.regions.api.regionWorld
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandAdd : CommandComponent {
    override val argsCount: Int
        get() = 1

    override fun test(sender: CommandSender): (() -> String)? {
        if (sender is Player) return null

        return { "콘솔에서 사용 할 수 없는 명령입니다." }
    }

    override fun onCommand(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): Boolean {
        val player = sender as Player
        player.selection?.also { selection ->
            val regionName = args.next()
            val world = BukkitAdapter.asBukkitWorld(selection.world).world.regionWorld
            val box = selection.toRegionBox()

            runCatching {
                Regions.createRegion(regionName, world, box).save()
                sender.sendMessage("구역이 추가되었습니다. $AQUA${regionName} = $GREEN${world.name} $GOLD$box")
            }.onFailure { t ->
                t.message?.let { sender.sendMessage("구역 추가에 실패했습니다. $RED$it") }
            }
        } ?: sender.sendMessage("먼저 구역으로 추가할 두 지점을 선택해주세요.")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): List<String> {
        return emptyList()
    }
}

private val Player.selection: CuboidRegion?
    get() {
        return try {
            WorldEdit.getInstance().sessionManager[BukkitAdapter.adapt(this)]?.run {
                getSelection(selectionWorld) as CuboidRegion
            }
        } catch (e: Exception) {
            null
        }
    }

private fun CuboidRegion.toRegionBox(): RegionBox {
    val min = minimumPoint
    val max = maximumPoint

    return RegionBox(min.x, min.y, min.z, max.x, max.y, max.z)
}

