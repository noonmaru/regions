package com.github.noonmaru.regions.internal

internal class RegionLoader(
    val region: RegionImpl,
    val parents: List<String>,
    val members: List<Pair<UserImpl, List<RoleImpl>>>
) {
    fun load() {
        region.load(this)
    }
}