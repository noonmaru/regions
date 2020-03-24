package com.github.noonmaru.regions

import java.util.*
import org.bukkit.entity.Player as BukkitPlayer

interface Player {
    val uniqueId: UUID

    val name: String

    val bukkitPlayer: BukkitPlayer?

    val regions: Collection<Region>

    val isOnline: Boolean
        get() {
            return bukkitPlayer != null
        }
}