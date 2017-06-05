package com.github.shiraji.permissionsdispatcherplugin.data

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement

data class ActionEventCommonJavaData(val element: PsiElement, val clazz: PsiClass)