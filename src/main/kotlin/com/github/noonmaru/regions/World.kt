package com.github.noonmaru.regions

import org.bukkit.World as BukkitWorld

interface World : Protectible {
    val world: BukkitWorld

    fun chunkAt(x: Int, y: Int): Chunk?

    fun regionAt(x: Int, y: Int, z: Int): Region?
}