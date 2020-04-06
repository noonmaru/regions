package com.github.noonmaru.regions.commands

import com.github.noonmaru.kommand.KommandDispatcherBuilder
import com.github.noonmaru.kommand.KommandSyntaxException
import com.github.noonmaru.kommand.argument.Argument
import com.github.noonmaru.regions.api.RegionBox
import com.github.noonmaru.regions.api.Regions
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.Region
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object RegionCommand {
    fun register(builder: KommandDispatcherBuilder) {
        builder.register("region") {
            then("add") {
                require {
                    this is Player
                }
                then("name" to Argument) {
                    executes {
                        addRegion(it.sender as Player, it.getArgument("name"))
                    }
                }
            }
        }
    }

    private fun addRegion(sender: Player, name: String) {
        val selection = sender.selection ?: throw KommandSyntaxException("먼저 구역으로 지정할곳을 선택해주세요")
        val min = selection.minimumPoint
        val max = selection.maximumPoint

        val world = BukkitAdapter.adapt(selection.world)
        val box = RegionBox(min.x, min.y, min.z, max.x, max.y, max.z)

        kotlin.runCatching {
            Regions.manager.registerNewRegion(name, Regions.manager.getRegionWorld(world)!!, box)
        }.onFailure {
            sender.sendMessage("${ChatColor.RED}구역을 생성하지 못했습니다. ${it.message}")
        }.onSuccess {
            it.save()
            sender.sendMessage("[${it.name}] 구역을 생성했습니다.")
        }
    }
}

val Player.selection: Region?
    get() {
        return try {
            WorldEdit.getInstance().sessionManager[BukkitAdapter.adapt(this)]?.run {
                getSelection(selectionWorld)
            }
        } catch (e: Exception) {
            null
        }
    }