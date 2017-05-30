package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.data.ActionEventCommonJavaData
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.util.PsiTreeUtil

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
    if (clazz?.modifierList?.findAnnotation("permissions.dispatcher.RuntimePermissions") == null) {
        e.presentation.isEnabledAndVisible = false
        return null
    }
    return ActionEventCommonJavaData(file, editor, clazz)
}

fun getNeedsPermissionMethods(clazz: PsiClass) =
        clazz.methods.filter { it.modifierList.findAnnotation("permissions.dispatcher.NeedsPermission") != null }

fun getOnShowRationaleMethods(clazz: PsiClass) =
        clazz.methods.filter { it.modifierList.findAnnotation("permissions.dispatcher.OnShowRationale") != null }
