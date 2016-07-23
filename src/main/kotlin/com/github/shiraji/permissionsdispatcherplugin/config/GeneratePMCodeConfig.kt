package com.github.shiraji.permissionsdispatcherplugin.config

import com.intellij.ide.util.PropertiesComponent

object GeneratePMCodeConfig {

    const val REBUILD_TYPE_KEY = "com.github.shiraji.permissionsdispatcherplugin.config.GeneratePMCodeConfig.REBUILD_TYPE_KEY"

    var rebuildTypeId: Int
        @JvmStatic get() = PropertiesComponent.getInstance().getInt(REBUILD_TYPE_KEY, 0)
        @JvmStatic set(value) = PropertiesComponent.getInstance().setValue(REBUILD_TYPE_KEY, value, 0)
}