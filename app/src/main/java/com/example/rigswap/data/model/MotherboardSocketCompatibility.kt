package com.example.rigswap.data.model

/**
 * The CPU socket for each motherboard offered by the real-world model picker.
 *
 * Unknown or custom models deliberately return every supported socket so a seller
 * can enter the specification manually.
 */
object MotherboardSocketCompatibility {
    const val LGA_1851 = "LGA 1851"
    const val LGA_1700 = "LGA 1700"
    const val SOCKET_AM5 = "Socket AM5"
    const val SOCKET_AM4 = "Socket AM4"
    const val SOCKET_STR5 = "Socket sTR5"
    const val SOCKET_STRX4 = "Socket sTRX4"

    val allSupportedSockets = listOf(
        SOCKET_AM5,
        SOCKET_AM4,
        LGA_1851,
        LGA_1700,
        SOCKET_STR5,
        SOCKET_STRX4
    )

    private val socketByModel = mapOf(
        // ASUS
        "ROG Maximus Z890 Apex" to LGA_1851,
        "ROG Maximus Z790 Hero" to LGA_1700,
        "ROG Strix B650E-F Gaming WiFi" to SOCKET_AM5,
        "ROG Crosshair X670E Hero" to SOCKET_AM5,
        "TUF Gaming Z890-Pro WiFi" to LGA_1851,
        "TUF Gaming Z790-Plus WiFi D4" to LGA_1700,
        "TUF Gaming B650M-Plus WiFi" to SOCKET_AM5,

        // Gigabyte
        "Z890 AORUS Master" to LGA_1851,
        "Z790 AORUS Master" to LGA_1700,
        "B650 AORUS Elite AX" to SOCKET_AM5,
        "X670E AORUS Xtreme" to SOCKET_AM5,

        // MSI
        "MEG Z890 ACE" to LGA_1851,
        "MEG Z790 ACE" to LGA_1700,
        "MEG X670E ACE" to SOCKET_AM5,
        "MAG Z890 Tomahawk WiFi" to LGA_1851,
        "MAG Z790 Tomahawk WiFi" to LGA_1700,
        "MAG B650 Tomahawk WiFi" to SOCKET_AM5,

        // ASRock
        "Z890 Taichi" to LGA_1851,
        "Z790 Taichi Carrara" to LGA_1700,
        "B650 Steel Legend WiFi" to SOCKET_AM5,

        // NZXT
        "N9 Z790" to LGA_1700,
        "N7 B650E" to SOCKET_AM5,
        "N5 Z790" to LGA_1700
    )

    fun socketForModel(model: String): String? = socketByModel[model]

    fun socketsForModel(model: String): List<String> =
        socketForModel(model)?.let(::listOf) ?: allSupportedSockets
}
