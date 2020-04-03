package com.github.noonmaru.regions.api

interface Member : Node, Permissible {
    val user: User

    val roles: List<Role>

    fun hasRole(role: Role): Boolean

    fun hasRoles(vararg roles: Role): Boolean {
        return hasRoles(roles.asList())
    }

    fun hasRoles(roles: Collection<Role>): Boolean
}