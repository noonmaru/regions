package com.github.noonmaru.regions.api

interface Node : Checkable {
    val parent: Area

    fun delete()
}