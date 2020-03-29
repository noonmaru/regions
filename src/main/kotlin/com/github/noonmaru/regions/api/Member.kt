package com.github.noonmaru.regions.api

interface Member : Permissible, Checkable {
    val region: Region

    val user: User

    val roles: List<Role>

    fun delete()
}