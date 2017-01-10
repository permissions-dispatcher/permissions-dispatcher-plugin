package com.github.shiraji.permissionsdispatcherplugin.extentions

fun String.generateVersionNumberFrom() = substring(lastIndexOf(":") + 1).replace("\'", "").replace("\"", "")