/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.regions.plugin

import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Protection
import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.regions.api.area
import org.bukkit.World
import org.bukkit.block.Container
import org.bukkit.block.Dispenser
import org.bukkit.block.data.Directional
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
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.projectiles.BlockProjectileSource

private fun Player.hasMasterKey(): Boolean {
    return hasPermission("regions.action")
}

class EventListener : Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val toLoc = event.to

        if (player.hasMasterKey()) return

        val from = event.from.area
        val to = toLoc!!.area

        if (from !== to) {
            if (!from.testPermission(player, Permission.EXIT) ||
                !to.testPermission(player, Permission.ENTRANCE)
            ) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val cause = event.cause

        if (cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
            || cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
        ) {

            val player = event.player
            val toLoc = event.to

            if (player.hasMasterKey()) return

            val from = event.from.area
            val to = toLoc!!.area

            if (from !== to) {
                if (!from.testPermission(player, Permission.EXIT) ||
                    !to.testPermission(player, Permission.ENTRANCE)
                ) {
                    event.isCancelled = true
                }
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
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val player = event.player

        if (player.hasMasterKey()) return

        val target = event.rightClicked

        if (!target.area.testPermission(player, Permission.INTERACTION)) {
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
        } else if (shooter is BlockProjectileSource) {
            val block = shooter.block
            val area = block.area

            if (area.hasProtection(Protection.DISPENSER)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockCanBuild(event: BlockCanBuildEvent) {
        val player = event.player
        if (player == null || player.hasMasterKey()) return

        if (!event.block.area.testPermission(player, Permission.BLOCK_PLACE)) {
            event.isBuildable = false
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
            if (area.hasProtection(Protection.FIRE)) {
                event.isCancelled = true
            }
        } else {
            if (player.hasMasterKey()) return

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
    fun onPlayerPickupItem(event: EntityPickupItemEvent) {
        val entity = event.entity

        if (entity is Player) {
            if (entity.hasMasterKey()) return

            val item = event.item

            if (!item.area.testPermission(entity, Permission.PICKUP_ITEM)) {
                event.isCancelled = true
                item.pickupDelay = 10
            }
        } else {
            val item = event.item

            if (item.area.hasProtection(Protection.ENTITY_PICKUP_ITEM)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPotionSplash(event: PotionSplashEvent) {
        for (affectedEntity in event.affectedEntities) {
            val area = affectedEntity.area

            if (area.hasProtection(Protection.POTION)) {
                event.setIntensity(affectedEntity, 0.0)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onLingeringPotionSplash(event: LingeringPotionSplashEvent) {
        if (event.areaEffectCloud.area.hasProtection(Protection.POTION)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onAreaEffectCloudApply(event: AreaEffectCloudApplyEvent) {
        event.affectedEntities.removeIf { entity ->
            entity.area.hasProtection(Protection.POTION)
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
    fun onBlockPistonExtend(event: BlockPistonExtendEvent) {
        val direction = event.direction
        val piston = event.block
        val head = piston.getRelative(direction)

        val pistonArea = piston.area
        val headArea = head.area
        val pistonProtection = pistonArea.hasProtection(Protection.PISTON)

        if (pistonArea !== headArea
            && (pistonProtection || headArea.hasProtection(Protection.PISTON))
        ) {
            event.isCancelled = true
            return
        }

        if (pistonProtection) {
            for (block in event.blocks) {
                if (block.area !== pistonArea || block.getRelative(direction).area !== pistonArea) {
                    event.isCancelled = true
                    break
                }
            }
        } else {
            for (block in event.blocks) {
                val blockArea = block.area

                if (blockArea !== pistonArea && blockArea.hasProtection(Protection.PISTON)) {
                    event.isCancelled = true
                    break
                }

                val toBlockArea = block.getRelative(direction).area

                if (toBlockArea !== pistonArea && toBlockArea.hasProtection(Protection.PISTON)) {
                    event.isCancelled = true
                    break
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockPistonRetract(event: BlockPistonRetractEvent) {
        val blocks = event.blocks

        if (blocks.isEmpty())
            return

        val direction = event.direction
        val piston = event.block
        val pistonArea = piston.area
        val pistonProtection = pistonArea.hasProtection(Protection.PISTON)


        if (pistonProtection) {
            for (block in event.blocks) {
                if (block.area !== pistonArea || block.getRelative(direction).area !== pistonArea) {
                    event.isCancelled = true
                    break
                }
            }
        } else {
            for (block in event.blocks) {
                val blockArea = block.area

                if (blockArea !== pistonArea && blockArea.hasProtection(Protection.PISTON)) {
                    event.isCancelled = true
                    break
                }

                val toBlockArea = block.getRelative(direction).area

                if (toBlockArea !== pistonArea && toBlockArea.hasProtection(Protection.PISTON)) {
                    event.isCancelled = true
                    break
                }
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
            event.blockList().removeIf { block ->
                block.area.hasProtection(Protection.EXPLOSION)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeIf { it.area.hasProtection(Protection.EXPLOSION) }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBlockDispense(event: BlockDispenseEvent) {
        val block = event.block
        val state = block.state

        if (state is Dispenser) {
            val data = block.blockData
            if (data is Directional) {
                val from = block.area
                val to = block.getRelative(data.facing).area

                if (from !== to &&
                    (from.hasProtection(Protection.DISPENSER) || to.hasProtection(Protection.DISPENSER))
                ) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onInventoryMoveItem(event: InventoryMoveItemEvent) {
        val sourceHolder = event.source.holder
        val destinationHolder = event.destination.holder

        if (sourceHolder is Container && destinationHolder is Container) {
            val sourceArea = sourceHolder.block.area
            val destinationArea = destinationHolder.block.area

            if (sourceArea !== destinationArea &&
                (sourceArea.hasProtection(Protection.ITEM_TRANSFER) || destinationArea.hasProtection(Protection.ITEM_TRANSFER))
            ) {
                event.isCancelled = true
            }
        }
    }
}