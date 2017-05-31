package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.data.ActionEventCommonJavaData
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

private const val NEEDS_PERMISSION = "permissions.dispatcher.NeedsPermission"

fun updateForGenerateAction(e: AnActionEvent): ActionEventCommonJavaData? {
    val file = e.getData(CommonDataKeys.PSI_FILE) as? PsiJavaFile
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (file == null || editor == null) {
        e.presentation.isEnabledAndVisible = false
        return null
    }
    val offset = editor.caretModel.offset
    val element = file.findElementAt(offset)
    val clazz = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
    if (element == null || clazz == null || !clazz.isAnnotatedRuntimePermissions()) {
        e.presentation.isEnabledAndVisible = false
        return null
    }
    return ActionEventCommonJavaData(file, editor, element, clazz)
}

fun PsiClass.isAnnotatedRuntimePermissions() =
        modifierList?.findAnnotation("permissions.dispatcher.RuntimePermissions") != null

fun PsiClass.getNeedsPermissionMethods() =
        methods.filter { it.modifierList.findAnnotation(NEEDS_PERMISSION) != null }

fun PsiClass.getOnShowRationaleMethods() =
        methods.filter { it.modifierList.findAnnotation("permissions.dispatcher.OnShowRationale") != null }

private fun PsiMethod.isAnnotatedWith(annotation: String)
        = modifierList.findAnnotation(annotation) != null

fun PsiMethod.isAnnotatedWithNeedsPermission() = isAnnotatedWith(NEEDS_PERMISSION)