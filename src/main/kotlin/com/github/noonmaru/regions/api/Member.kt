package com.github.noonmaru.regions.api

interface Member : Permissible, Checkable, Deletable {
    val parent: Organizable

    val user: User

    val roles: List<Role>
}