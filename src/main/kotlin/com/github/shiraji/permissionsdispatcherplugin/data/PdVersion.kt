package com.github.shiraji.permissionsdispatcherplugin.data

enum class PdVersion() {
    VERSION_2_1_3,
    VERSION_2_2_0,
    UNKNOWN;

    companion object {
        fun fromText(versionText: String): PdVersion {
            return when (versionText) {
                "2.1.3" -> VERSION_2_1_3
                "2.2.0" -> VERSION_2_2_0
                else -> UNKNOWN
            }
        }
    }
}