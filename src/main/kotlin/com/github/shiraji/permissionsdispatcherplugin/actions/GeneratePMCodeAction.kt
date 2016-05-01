package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.handlers.GeneratePMCodeHandler
import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.github.shiraji.permissionsdispatcherplugin.views.GeneratePMCodeDialog
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiJavaFile

class GeneratePMCodeAction : CodeInsightAction() {
    var model: GeneratePMCodeModel? = null
    override fun getHandler(): CodeInsightActionHandler {
        return GeneratePMCodeHandler()
    }

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)

        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is PsiJavaFile) return

        val project = e.getData(CommonDataKeys.PROJECT) ?: return

        e.presentation.isEnabledAndVisible = GeneratePMCodeModel(project).isActivityOrFragment(file.classes[0])
    }

    override fun actionPerformed(e: AnActionEvent?) {
        val dialog = GeneratePMCodeDialog()
        dialog.pack()
        dialog.isVisible = true
        if (dialog.isOk) {
            val project = e?.getData(CommonDataKeys.PROJECT) ?: return
            model = GeneratePMCodeModel(project)
            super.actionPerformed(e)
        }
    }
}