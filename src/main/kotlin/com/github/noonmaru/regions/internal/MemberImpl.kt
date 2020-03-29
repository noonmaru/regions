package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Member
import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Role
import com.github.noonmaru.regions.api.User
import com.github.noonmaru.regions.util.IntBitSet
import com.google.common.collect.ImmutableList
import java.util.*

class MemberImpl(region: RegionImpl, user: UserImpl) : Member {
    override val region: RegionImpl
        get() = regionRef.get()
    override val user: User
        get() = userRef.get()
    override val roles: List<Role>
        get() = ImmutableList.copyOf(_roles)
    override val permissions: Set<Permission> by lazy(LazyThreadSafetyMode.NONE) {
        Collections.unmodifiableSet(_permissions.clone())
    }
    override var valid: Boolean = true

    private val regionRef = UpstreamReference(region)
    private val userRef = UpstreamReference(user)
    private val _roles = Collections.newSetFromMap(WeakHashMap<RoleImpl, Boolean>())

    private var updatePermission = false
    private val _permissions = IntBitSet { Permission.getByOffset(it) }
        get() {
            if (updatePermission) {
                updatePermission = false

                field.apply {
                    clear()

                    _roles.forEachIndexed { index, role ->
                        val other = role._permissions
                        if (index == 0) field.or(other) else field.and(other)
                    }
                }
            }

            return field
        }

    internal fun addRole(role: RoleImpl): Boolean {
        return _roles.add(role).also {
            if (it) updatePermission = true
        }
    }

    fun hasRoles(roles: Collection<Role>): Boolean {
        return _roles.containsAll(roles)
    }

    internal fun removeRole(role: RoleImpl): Boolean {
        return _roles.remove(role).also {
            if (it) updatePermission = true
        }
    }

    internal fun hasRole(role: RoleImpl): Boolean {
        return _roles.contains(role)
    }

    override fun hasPermission(permission: Permission): Boolean {
        return permissions.contains(permission)
    }

    override fun hasPermissions(permissions: Collection<Permission>): Boolean {
        return this.permissions.containsAll(permissions)
    }

    override fun delete() {
        checkState()
        region.removeMember(user)
    }

    fun destroy() {
        valid = false
    }
}