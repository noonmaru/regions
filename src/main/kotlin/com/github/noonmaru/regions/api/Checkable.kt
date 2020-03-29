package com.github.noonmaru.regions.api

interface Checkable {
    val valid: Boolean

    fun checkState() {
        require(valid) { "Invalid ${toString()}" }
    }
}