package com.github.noonmaru.regions.api

interface Role : Permissible, Checkable {
    val region: Region

    val name: String

    fun addPermissions(vararg permissions: Permission) {
        addPermissions(listOf(*permissions))
    }

    fun addPermissions(permissions: Collection<Permission>)

    fun removePermissions(vararg permissions: Permission) {
        removePermission(listOf(*permissions))
    }

    fun removePermission(permissions: Collection<Permission>)

    fun delete()
}