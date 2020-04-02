package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Role
import com.github.noonmaru.regions.util.IntBitSet
import com.github.noonmaru.regions.util.UpstreamReference
import java.util.*

class RoleImpl(
    region: RegionImpl,
    override val name: String,
    private val defaultRole: Boolean = false
) : Role {
    override var valid: Boolean = true
    private val regionRef = UpstreamReference(region)

    override val region: RegionImpl
        get() = regionRef.get()

    override val permissions: Set<Permission>
        get() = Collections.unmodifiableSet(_permissions.clone())

    internal val _permissions = IntBitSet { Permission.getByOffset(it) }

    internal fun addPermissions(permissions: Collection<Permission>): Boolean {
        return _permissions.addAll(permissions)
    }

    internal fun removePermissions(permissions: Collection<Permission>): Boolean {
        return _permissions.removeAll(permissions)
    }

    override fun hasPermission(permission: Permission): Boolean {
        return _permissions.contains(permission)
    }

    override fun hasPermissions(permissions: Collection<Permission>): Boolean {
        return _permissions.containsAll(permissions)
    }

    override fun delete() {
        checkState()
        region.checkState()

        require(!defaultRole) { "Cannot delete default role" }
        region.removeRole(name)
    }

    fun destroy() {
        valid = false
    }
}