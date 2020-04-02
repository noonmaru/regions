package com.github.noonmaru.regions.api

interface Role : Permissible, Checkable, Deletable {
    val parent: Organizable

    val name: String
}