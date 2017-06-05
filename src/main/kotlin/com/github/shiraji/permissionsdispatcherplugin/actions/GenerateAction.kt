package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.data.ActionEventCommonJavaData
import com.github.shiraji.permissionsdispatcherplugin.extentions.isAnnotatedRuntimePermissions
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

fun createActionEventCommonData(e: AnActionEvent): ActionEventCommonJavaData? {
    val file = e.getData(CommonDataKeys.PSI_FILE) as? PsiJavaFile ?: return null
    val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
    return createActionEventCommonData(file, editor)
}

fun createActionEventCommonData(file: PsiJavaFile, editor: Editor): ActionEventCommonJavaData? {
    val element = getPointingElement(editor, file) ?: return null
    val clazz = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return null
    if (!clazz.isAnnotatedRuntimePermissions()) return null
    return ActionEventCommonJavaData(file, editor, element, clazz)
}

fun getPointingElement(editor: Editor, file: PsiJavaFile) = file.findElementAt(editor.caretModel.offset)