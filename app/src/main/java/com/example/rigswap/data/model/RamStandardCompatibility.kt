package com.example.rigswap.data.model

/**
 * RAM standards available in the real-world model picker.
 *
 * A listed model carries its DDR generation and rated speed in its name, so that
 * value is used to keep the listing specification accurate. Custom models retain
 * the complete list for manual selection.
 */
object RamStandardCompatibility {
    val allSupportedStandards = listOf(
        "DDR5-6400",
        "DDR5-6000",
        "DDR5-5600",
        "DDR5-4800",
        "DDR4-3600",
        "DDR4-3200",
        "DDR4-2666"
    )

    fun standardForModel(model: String): String? =
        allSupportedStandards.firstOrNull { model.contains(it, ignoreCase = true) }

    fun standardsForModel(model: String): List<String> =
        standardForModel(model)?.let(::listOf) ?: allSupportedStandards
}
