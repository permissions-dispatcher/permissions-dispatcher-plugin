package com.github.shiraji.permissionsdispatcherplugin.actions

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class AddOnShowRationaleMethod : CodeInsightAction() {

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)
        val (_, _, _, clazz) = updateForGenerateAction(e) ?: return
        val needsPermissionMethods = clazz.getNeedsPermissionMethods()
        val onShowRationaleMethods = clazz.getOnShowRationaleMethods()
    }

    override fun getHandler(): CodeInsightActionHandler {
        return object : CodeInsightActionHandler {
            override fun startInWriteAction() = true
            override fun invoke(project: Project, editor: Editor, file: PsiFile) {
            }
        }
    }
}