package com.github.shiraji.permissionsdispatcherplugin.data

enum class RebuildType(val id: Int, val text: String) {
    PROMPT(0, "Prompt"),
    ALWAYS(1, "Always"),
    NOT_ALWAYS(2, "Not Always");

    override fun toString(): String {
        return text
    }

    companion object {
        @JvmStatic fun fromId(id: Int): RebuildType? {
            return RebuildType.values().singleOrNull { it.id == id }
        }
    }

}