package com.github.noonmaru.regions

import org.bukkit.entity.Player

interface Region : Protectible {
    val name: String

    val world: World

    val chunk: Chunk

    val parents: Set<Region>

    val children: Set<Region>

    val everyoneRole: Role

    val rolesByPlayer: Map<com.github.noonmaru.regions.Player, Role>

    fun getRole(player: com.github.noonmaru.regions.Player): Role {
        return rolesByPlayer[player] ?: everyoneRole
    }

    fun addParent(region: Region): Boolean

    fun removeParent(region: Region): Boolean

    fun findRole(player: Player): Role

    fun getOrCreateRole(player: com.github.noonmaru.regions.Player): Role

    fun removeRole(player: com.github.noonmaru.regions.Player): Role?

    fun relocate(world: World, region: Region)
}
