package com.github.noonmaru.regions.api

interface Role : Node, Permissible {
    val name: String

    val isPublic: Boolean
}