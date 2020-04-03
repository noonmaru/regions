package com.github.noonmaru.regions.plugin

import com.github.noonmaru.regions.api.*
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.StructureGrowEvent

private fun Player.hasMasterKey(): Boolean {
    return hasPermission("regions.action")
}

class EventListener : Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (player.hasMasterKey()) return

        val from = event.from.area
        val to = event.to.area

        if (from !== to) {
            if (!from.testPermission(player, Permission.EXIT) ||
                !to.testPermission(player, Permission.ENTRANCE)
            ) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (player.hasMasterKey()) return

        event.clickedBlock?.let { clickedBlock ->
            val area = clickedBlock.area
            if (!area.testPermission(player, Permission.INTERACTION))
                event.isCancelled = true

            return
        }

        if (!player.area.testPermission(player, Permission.INTERACTION)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        var damager = event.damager

        if (damager is Projectile) {
            val shooter = damager.shooter

            if (shooter is Entity)
                damager = shooter
        }

        val area = entity.area

        if (damager is Player) {
            if (damager.hasMasterKey()) return

            val permission = if (entity is Player) Permission.ATTACK_PLAYER else Permission.ATTACK_ENTITY

            if (!area.testPermission(damager, permission)) {
                event.isCancelled = true
                return
            }
        }

        if (area.hasProtection(Protection.DAMAGE)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val projectile = event.entity
        val shooter = projectile.shooter

        if (shooter is Player) {
            if (shooter.hasMasterKey()) return

            if (!projectile.area.testPermission(shooter, Permission.PROJECTILE_LAUNCH)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        if (player.hasMasterKey()) return

        if (!event.block.area.testPermission(player, Permission.BLOCK_BREAK)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        if (player.hasMasterKey()) return

        if (!event.block.area.testPermission(player, Permission.BLOCK_PLACE)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockIgnite(event: BlockIgniteEvent) {
        val area = event.block.area
        val player = event.player

        if (player == null) {
            if (!area.hasProtection(Protection.FIRE)) {
                event.isCancelled = true
            }
        } else {
            if (!area.testPermission(player, Permission.BLOCK_IGNITING)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerBucketFill(event: PlayerBucketFillEvent) {
        val player = event.player
        if (player.hasMasterKey()) return

        if (!event.blockClicked.area.testPermission(player, Permission.BUCKET_FILL)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        val player = event.player
        if (player.hasMasterKey()) return

        if (!event.blockClicked.getRelative(event.blockFace).area.testPermission(player, Permission.BUCKET_EMPTY)) {
            event.isCancelled = true
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
            if (remover.hasMasterKey()) return

            if (!event.entity.area.testPermission(remover, Permission.HANGING_BREAK)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onHangingPlace(event: HangingPlaceEvent) {
        val player = event.player

        if (player == null || player.hasMasterKey()) return

        if (!event.block.area.testPermission(player, Permission.HANGING_PLACE)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        if (player.hasMasterKey()) return

        if (!event.itemDrop.area.testPermission(player, Permission.DROP_ITEM)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerPickupItem(event: PlayerAttemptPickupItemEvent) {
        val player = event.player
        if (player.hasMasterKey()) return

        if (!event.item.area.testPermission(player, Permission.PICKUP_ITEM)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPotionSplash(event: PotionSplashEvent) {
        var area: Area? = null

        for (affectedEntity in event.affectedEntities) {
            if (area == null) {
                area = affectedEntity.area
            } else {
                if (area is RegionWorld
                    || (area is Region && !area.box.contains(affectedEntity.location))
                ) {
                    area = affectedEntity.area
                }
            }

            if (!area.hasProtection(Protection.POTION)) {
                event.setIntensity(affectedEntity, 0.0)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockFade(event: BlockFadeEvent) {
        if (event.block.area.hasProtection(Protection.FADE)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockGrow(event: BlockGrowEvent) {
        if (event.block.area.hasProtection(Protection.GROWTH)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockForm(event: BlockFormEvent) {
        if (event.block.area.hasProtection(Protection.FORM)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockSpread(event: BlockSpreadEvent) {
        val source = event.source.area
        val block = event.block.area

        if (source !== block) {
            if (source.hasProtection(Protection.SPREAD)
                || block.hasProtection(Protection.SPREAD)
            ) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onBlockRedstone(event: BlockRedstoneEvent) {
        if (event.block.area.hasProtection(Protection.REDSTONE)) {
            event.newCurrent = 0
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onStructureGrow(event: StructureGrowEvent) {
        val area = event.location.area

        if (area.hasProtection(Protection.OVERFLOW)) {
            val blocks = event.blocks

            if (area is Region) {
                val box = area.box
                blocks.removeIf { !box.contains(it.x, it.y, it.z) }
            } else if (area is World) {
                blocks.removeIf { it.block.area === area }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockFromTo(event: BlockFromToEvent) {
        val from = event.block.area
        val to = event.toBlock.area

        if (from !== to) {
            if (from.hasProtection(Protection.FLOOD)
                || to.hasProtection(Protection.FLOOD)
            ) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEntityBlockChange(event: EntityChangeBlockEvent) {
        if (event.block.area.hasProtection(Protection.ENTITY_CHANGE_BLOCK)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (event.entity.area.hasProtection(Protection.EXPLOSION)) {
            event.isCancelled = true
        } else {
            event.blockList().removeIf { it.area.hasProtection(Protection.EXPLOSION) }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeIf { it.area.hasProtection(Protection.EXPLOSION) }
    }
}