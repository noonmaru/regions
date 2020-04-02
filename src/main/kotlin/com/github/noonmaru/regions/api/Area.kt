package com.github.noonmaru.regions.api

import org.bukkit.entity.Player

interface Area : Protectable, Organizable {
    fun getPermissions(player: Player): Set<Permission>

    fun testPermission(player: Player, permission: Permission)

    fun testPermissions(player: Player, vararg permissions: Permission) {
        return testPermissions(player, permissions.asList())
    }

    fun testPermissions(player: Player, permissions: Collection<Permission>)

}