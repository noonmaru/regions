package com.github.noonmaru.regions.api


interface Region : Node, Area, Checkable {
    override val parent: RegionWorld

    val box: RegionBox

    val parents: List<Region>

    val children: List<Region>

    fun relocate(newWorld: RegionWorld, newBox: RegionBox)

    fun addParent(region: Region): Boolean

    fun removeParent(region: Region): Boolean

    fun getDirectAncestors(): Set<Region>

    fun getAllDescendants(): Set<Region>
}