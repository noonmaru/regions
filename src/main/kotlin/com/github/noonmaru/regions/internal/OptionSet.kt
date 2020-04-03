package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Protection
import com.github.noonmaru.regions.util.IntBitSet

class ProtectionSet(rawElements: Int = 0) : IntBitSet<Protection>(rawElements, { Protection.getByOffset(it) })

class PermissionSet(rawElements: Int = 0) : IntBitSet<Permission>(rawElements, { Permission.getByOffset(it) })