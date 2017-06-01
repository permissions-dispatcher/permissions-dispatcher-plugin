package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.data.ActionEventCommonJavaData
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

private const val NEEDS_PERMISSION = "permissions.dispatcher.NeedsPermission"
private const val ON_SHOW_RATIONALE = "permissions.dispatcher.OnShowRationale"

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

fun PsiClass.isAnnotatedRuntimePermissions() =
        modifierList?.findAnnotation("permissions.dispatcher.RuntimePermissions") != null

fun PsiClass.getNeedsPermissionMethods() = methods.filter { it.isAnnotatedWithNeedsPermission() }

fun PsiClass.getOnShowRationaleMethods() = methods.filter { it.isAnnotatedWithOnShowRationale() }

fun PsiMethod.isAnnotatedWithNeedsPermission() = getNeedsPermissionAnnotation() != null

fun PsiMethod.isAnnotatedWithOnShowRationale() = getOnShowRationaleAnnotation() != null

fun PsiMethod.getAnnotationWith(annotation: String) = modifierList.findAnnotation(annotation)

fun PsiMethod.getNeedsPermissionAnnotation() = getAnnotationWith(NEEDS_PERMISSION)

fun PsiMethod.getOnShowRationaleAnnotation() = getAnnotationWith(ON_SHOW_RATIONALE)