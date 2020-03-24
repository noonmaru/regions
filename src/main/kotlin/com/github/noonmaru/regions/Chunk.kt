package com.github.noonmaru.regions

interface Chunk {
    val world: World
    val x: Int
    val z: Int

    fun sectionAt(y: Int): Section?

    fun regionAt(x: Int, y: Int, z: Int): Region?
}