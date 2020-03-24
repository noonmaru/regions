package com.github.noonmaru.regions

interface Role {
    fun hasPermission(permission: Permission): Boolean

    fun addPermission(vararg permissions: Permission)

    fun removePermission(vararg permissions: Permission)

    fun getPermissions(): Set<Permission>
}