package com.github.noonmaru.regions.plugin

import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.regions.api.region
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.*

class EventListener : Listener {
    companion object {
        private val permAction = "regions.action"
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (player.hasPermission(permAction)) return

        val from = event.from.region
        val to = event.to.region

        if (from !== to) {
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
        if (player.hasPermission(permAction)) return

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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        var damager = event.damager

        if (damager is Projectile) {
            val shooter = damager.shooter

            if (shooter is Entity)
                damager = shooter
        }

        if (damager is Player) {
            if (damager.hasPermission(permAction)) return

            val entity = event.entity
            val entityLoc = entity.location

            entityLoc.region?.let { region ->
                val perm = if (entity is Player) Permission.PLAYER_ATTACK else Permission.ENTITY_ATTACK

                if (!region.hasPermission(damager, perm)) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val projectile = event.entity
        val shooter = projectile.shooter

        if (shooter is Player) {
            if (shooter.hasPermission(permAction)) return

            val loc = projectile.location

            loc.region?.let { region ->
                if (!region.hasPermission(shooter, Permission.PROJECTILE_LAUNCH)) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        if (player.hasPermission(permAction)) return

        event.block.region?.let { region ->
            if (!region.hasPermission(player, Permission.BLOCK_BREAK)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        if (player.hasPermission(permAction)) return

        event.block.region?.let { region ->
            if (!region.hasPermission(player, Permission.BLOCK_PLACE)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockIgnite(event: BlockIgniteEvent) {
        val player = event.player

        if (player == null || player.hasPermission(permAction)) {
            //TODO
        } else {
            event.block.region?.let { region ->
                if (!region.hasPermission(player, Permission.BLOCK_IGNITING)) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerBucketFill(event: PlayerBucketFillEvent) {
        val player = event.player
        if (player.hasPermission(permAction)) return

        event.blockClicked.region?.let { region ->
            if (!region.hasPermission(player, Permission.BUCKET_FILL)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        val player = event.player
        if (player.hasPermission(permAction)) return

        event.blockClicked.getRelative(event.blockFace).region?.let { region ->
            if (!region.hasPermission(player, Permission.BUCKET_EMPTY)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onHangingBreak(event: HangingBreakByEntityEvent) {
        var remover = event.remover ?: return

        if (remover is Projectile) {
            val shooter = remover.shooter

            if (shooter is Entity)
                remover = shooter
        }

        if (remover is Player) {
            if (remover.hasPermission(permAction)) return

            event.entity.location.region?.let { region ->
                if (!region.hasPermission(remover, Permission.HANGING_BREAK)) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onHangingPlace(event: HangingPlaceEvent) {
        val player = event.player

        if (player == null || player.hasPermission(permAction)) return

        event.block.getRelative(event.blockFace).region?.let { region ->
            if (!region.hasPermission(player, Permission.HANGING_PLACE)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        if (player.hasPermission(permAction)) return

        event.itemDrop.location.region?.let { region ->
            if (!region.hasPermission(player, Permission.DROP_ITEM))
                event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerPickupItem(event: PlayerAttemptPickupItemEvent) {
        val player = event.player
        if (player.hasPermission(permAction)) return

        event.item.location.region?.let { region ->
            if (!region.hasPermission(player, Permission.PICKUP_ITEM))
                event.isCancelled = true
        }
    }
}

private fun Region.hasPermission(player: Player, permission: Permission): Boolean {
    return getPermissions(player).contains(permission)
}