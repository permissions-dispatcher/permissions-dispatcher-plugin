package com.github.shiraji.permissionsdispatcherplugin.actions

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.JOptionPane

class AddDelegationMethod : CodeInsightAction() {
    companion object {
        const val NEEDS_PERMISSION = "permissions.dispatcher.NeedsPermission"
        const val RUNTIME_PERMISSIONS = "permissions.dispatcher.RuntimePermissions"
    }

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)
        val file = e.getData(CommonDataKeys.PSI_FILE) as? PsiJavaFile
        val editor = e.getData(CommonDataKeys.EDITOR)
        if (file == null || editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset)
        val clazz = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
        if (clazz?.modifierList?.findAnnotation(RUNTIME_PERMISSIONS) == null
                || method == null
                || method.modifierList.findAnnotation(NEEDS_PERMISSION) != null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val needsPermissionMethods = clazz.methods.filter { it.modifierList.findAnnotation(NEEDS_PERMISSION) != null }
        e.presentation.isEnabledAndVisible = needsPermissionMethods.isNotEmpty()
    }

    override fun getHandler(): CodeInsightActionHandler {
        return AddDelegationMethodHandler()
    }

    class AddDelegationMethodHandler : CodeInsightActionHandler {
        override fun startInWriteAction() = false

        override fun invoke(project: Project, editor: Editor, file: PsiFile) {
            val offset = editor.caretModel.offset
            val element = file.findElementAt(offset)
            val clazz = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return
            val needsPermissionMethods = clazz.methods.filter { it.modifierList.findAnnotation(NEEDS_PERMISSION) != null }

            val methodName = when (needsPermissionMethods.size) {
                0 -> return
                1 -> needsPermissionMethods[0].name
                else -> JOptionPane.showInputDialog(null, "Which methods do you want to delegate?",
                        "PermissionsDispatcher plugin", JOptionPane.QUESTION_MESSAGE, null,
                        needsPermissionMethods.map { it.name }.toTypedArray(), needsPermissionMethods.first().name) ?: return
            }

            runWriteAction {
                val factory = JavaPsiFacade.getElementFactory(project)
                val newExpression = factory.createStatementFromText("${clazz.name}PermissionsDispatcher.${methodName}WithCheck(this);", element)
                element?.parent?.addAfter(newExpression, element)
            }
        }
    }
}