package com.github.noonmaru.regions.api

interface Permissible {
    val permissions: Set<Permission>

    fun hasPermission(permission: Permission): Boolean

    fun hasPermissions(vararg permissions: Permission): Boolean {
        return hasPermissions(permissions.asList())
    }

    fun hasPermissions(permissions: Collection<Permission>): Boolean
}