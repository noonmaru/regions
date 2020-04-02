package com.github.noonmaru.regions.api


interface Region : Area, Checkable, Deletable {
    val name: String

    val world: RegionWorld

    val box: RegionBox

    val parents: List<Region>

    val children: List<Region>

    fun relocate(newWorld: RegionWorld, newBox: RegionBox)

    fun addParent(region: Region): Boolean

    fun removeParent(region: Region): Boolean

    fun save(): Boolean

}