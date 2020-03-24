package com.github.noonmaru.regions

interface Section {
    val y: Int

    fun regionAt(x: Int, y: Int, z: Int): Region?
}