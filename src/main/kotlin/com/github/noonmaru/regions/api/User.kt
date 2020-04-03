package com.github.noonmaru.regions.api

import org.bukkit.entity.Player
import java.util.*

interface User {
    val uniqueId: UUID

    val name: String

    val bukkitPlayer: Player?

    val regionMembers: List<Member>

    fun getMemberByRegion(region: Region): Member?

    val isOnline: Boolean
        get() {
            return bukkitPlayer != null
        }

    fun sendMessage(message: String) {
        bukkitPlayer?.run { sendMessage(message) }
    }
}