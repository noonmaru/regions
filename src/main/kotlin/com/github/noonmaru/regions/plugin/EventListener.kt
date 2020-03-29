package com.github.noonmaru.regions.plugin

import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.region
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

class EventListener : Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val from = event.from.region
        val to = event.to.region

        if (from != to) {
            val player = event.player

            if ((from != null && !from.getPermissions(player).contains(Permission.EXIT))
                || (to != null && !to.getPermissions(player).contains(Permission.ENTRANCE))
            ) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        event.clickedBlock?.let { clickedBlock ->
            val region = clickedBlock.region

            if (region != null && !region.getPermissions(player).contains(Permission.INTERACTION))
                event.isCancelled = true

            return
        }

        player.location.region?.let { region ->
            if (!region.getPermissions(player).contains(Permission.INTERACTION)) {
                event.isCancelled = true
            }
        }
    }
}