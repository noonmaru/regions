package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Role
import com.github.noonmaru.regions.util.IntBitSet
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

    override val permissions: Set<Permission> by lazy(LazyThreadSafetyMode.NONE) {
        Collections.unmodifiableSet(_permissions.clone())
    }

    internal val _permissions = IntBitSet { Permission.getByOffset(it) }

    override fun addPermissions(permissions: Collection<Permission>) {
        _permissions.addAll(permissions)
    }

    override fun removePermission(permissions: Collection<Permission>) {
        _permissions.removeAll(permissions)
    }

    override fun hasPermission(permission: Permission): Boolean {
        return _permissions.contains(permission)
    }

    override fun hasPermissions(permissions: Collection<Permission>): Boolean {
        return _permissions.containsAll(permissions)
    }

    override fun delete() {
        checkState()
        require(!defaultRole) { "Cannot delete default role" }
        region.removeRole(name)
    }

    fun destroy() {
        valid = false
    }
}