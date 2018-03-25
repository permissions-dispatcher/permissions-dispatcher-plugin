package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.extentions.getNeedsPermissionMethods
import com.github.shiraji.permissionsdispatcherplugin.extentions.isAnnotatedWithNeedsPermission
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.JOptionPane

class AddDelegationMethod : CodeInsightAction() {

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)
        val data = createActionEventCommonData(e)
        if (data == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val method = PsiTreeUtil.getParentOfType(data.element, PsiMethod::class.java)
        if (method == null
                || method.name == "onResume" // this is not perfect but who cares someone creates a custom method calls "onResume".
                || method.isAnnotatedWithNeedsPermission()) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        e.presentation.isEnabledAndVisible = data.clazz.getNeedsPermissionMethods().isNotEmpty()
    }

    override fun getHandler() = object : CodeInsightActionHandler {
        override fun startInWriteAction() = false

        override fun invoke(project: Project, editor: Editor, file: PsiFile) {
            if (file !is PsiJavaFile) return
            val element = getPointingElement(editor, file)
            val clazz = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return
            val needsPermissionMethods = clazz.getNeedsPermissionMethods()
            val methodName = when (needsPermissionMethods.size) {
                0 -> return
                1 -> needsPermissionMethods[0].name
                else -> JOptionPane.showInputDialog(null, "Which methods do you want to delegate?",
                        "PermissionsDispatcher plugin", JOptionPane.QUESTION_MESSAGE, null,
                        needsPermissionMethods.map { it.name }.toTypedArray(), needsPermissionMethods.first().name) ?: return
            }

            runWriteAction {
                val factory = JavaPsiFacade.getElementFactory(project)
                val newExpression = factory.createStatementFromText("${clazz.name}PermissionsDispatcher.${methodName}WithPermissionCheck(this);", element)
                element?.parent?.addAfter(newExpression, element)
            }
        }
    }

}