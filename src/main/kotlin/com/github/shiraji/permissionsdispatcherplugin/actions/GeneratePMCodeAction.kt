package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import javax.swing.JOptionPane

class GeneratePMCodeAction : CodeInsightAction() {
    override fun getHandler(): CodeInsightActionHandler {
        return Handler()
    }

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)

        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is PsiJavaFile) return

        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val model = GeneratePMCodeModel(project)

        e.presentation.isEnabledAndVisible = model.isActivityOrFragment(file.classes[0])
    }

    override fun actionPerformed(e: AnActionEvent?) {
        val result = JOptionPane.showConfirmDialog(null,
                "Generate Permissions?", "permissions-dispatcher-plugin", JOptionPane.YES_NO_OPTION);

        if (JOptionPane.YES_OPTION == result) {
            super.actionPerformed(e)
        }
    }
}

class Handler : CodeInsightActionHandler {
    override fun startInWriteAction() = false

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if(file !is PsiJavaFile) return
        val model = GeneratePMCodeModel(project)
        addRuntimePermissionAnnotation(file, model, project)
    }

    private fun addRuntimePermissionAnnotation(file: PsiJavaFile, model: GeneratePMCodeModel, project: Project) {
        val psiAnnotation = file.classes[0].modifierList?.findAnnotation("permissions.dispatcher.RuntimePermissions")
        if (psiAnnotation == null) {
            val psiClass = model.createPsiClass("permissions.dispatcher.RuntimePermissions", project)
            val application = ApplicationManager.getApplication();
            application.runWriteAction {
                file.classes[0].modifierList?.addAnnotation("RuntimePermissions")
                file.importClass(psiClass)
            }
        }
    }

}