package com.github.shiraji.permissionsdispatcherplugin.data

class AndroidGradleVersion(versionText: String) {
    private var major: Int
    private var minor: Int

    init {
        try {
            major = versionText.substringBefore(".").toInt()
            minor = versionText.substring(versionText.indexOf(".") + 1, versionText.lastIndexOf(".")).toInt()
        } catch (e: NumberFormatException) {
            major = -1
            minor = -1
        }
    }

    fun isHigherThan2_2() = major >= 2 && minor >= 2

    fun isValid() = major >= 0 && minor >= 0
}