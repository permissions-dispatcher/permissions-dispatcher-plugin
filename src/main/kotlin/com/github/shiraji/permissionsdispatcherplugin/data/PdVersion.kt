package com.github.shiraji.permissionsdispatcherplugin.data

enum class PdVersion {
    NOTFOUND,
    VERSION_2_1_3,
    UNKNOWN;

    companion object {
        fun fromText(versionText: String): PdVersion {
            return when (versionText) {
                "2.1.3" -> VERSION_2_1_3
                else -> UNKNOWN
            }
        }

        const val latestVersion = "3.0.1"
    }
}