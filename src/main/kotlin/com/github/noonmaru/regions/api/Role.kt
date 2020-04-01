package com.github.noonmaru.regions.api

interface Role : Permissible, Checkable {
    val region: Region

    val name: String

    fun delete()
}