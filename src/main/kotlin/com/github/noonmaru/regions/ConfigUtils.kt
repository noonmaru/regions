package com.github.noonmaru.regions

import com.github.noonmaru.regions.api.RegionBox
import org.bukkit.configuration.ConfigurationSection

fun <E : Enum<E>> Iterable<String>.toEnumList(
    transform: (String) -> E?,
    onNotFound: ((name: String) -> Unit)? = null
): List<E> {
    val list = ArrayList<E>(count())

    loop@ for (name in this) {
        val value = kotlin.runCatching { transform(name) }.getOrNull()

        if (value != null) {
            list += list
        } else {
            onNotFound?.runCatching { invoke(name) }
        }
    }

    return list
}

fun <E : Enum<E>> Iterable<E>.toStringList(transform: (E) -> String = { it.toString() }): List<String> {
    return map(transform)
}

fun ConfigurationSection.getBox(path: String): RegionBox {
    val config = getSection(path)
    val min = config.getSection("min")
    val max = config.getSection("max")

    return RegionBox(
        min.getInt("x"),
        min.getInt("y"),
        min.getInt("z"),
        max.getInt("x"),
        max.getInt("y"),
        max.getInt("z")
    )
}

fun ConfigurationSection.getSection(path: String): ConfigurationSection {
    return requireNotNull(getConfigurationSection(path)) { "Not found path $path" }
}

fun ConfigurationSection.getStringValue(path: String): String {
    return requireNotNull(getString(path)) { "Undefined $path" }
}

