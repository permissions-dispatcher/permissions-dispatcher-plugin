package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiType
import com.intellij.psi.impl.light.LightMethodBuilder
import com.sun.tools.doclets.internal.toolkit.builders.MethodBuilder
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
    override fun startInWriteAction() = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (file !is PsiJavaFile) return
        val model = GeneratePMCodeModel(project)
        addRuntimePermissionAnnotation(file, model, project)
        addNeedsPermissionMethod(file, model, project)
        addOnRequestPermissionsResult(file, model, project)
    }

    private fun addOnRequestPermissionsResult(file: PsiJavaFile, model: GeneratePMCodeModel, project: Project) {
        val methods = file.classes[0].findMethodsByName("onRequestPermissionsResult", false)

        if (methods.size == 0) {
            val methodBuilder = LightMethodBuilder(PsiManager.getInstance(project), JavaLanguage.INSTANCE, "onRequestPermissionsResult")
            methodBuilder.addModifiers("@Override", "public")
            methodBuilder.addParameter("requestCode", PsiType.INT)
            methodBuilder.addParameter("permissions", "String[]")
            methodBuilder.addParameter("grantResults", "int[]")
            methodBuilder.setMethodReturnType(PsiType.VOID)
        }


        // if no method, then add onRequestPermissionsResult method. maybe I should not use let
    }

    private fun addNeedsPermissionMethod(file: PsiJavaFile, model: GeneratePMCodeModel, project: Project) {

    }

    private fun addRuntimePermissionAnnotation(file: PsiJavaFile, model: GeneratePMCodeModel, project: Project) {
        if (file.classes[0].modifierList?.findAnnotation("permissions.dispatcher.RuntimePermissions") != null) return
        file.classes[0].modifierList?.addAnnotation("RuntimePermissions")
        file.importClass(model.createPsiClass("permissions.dispatcher.RuntimePermissions", project))
    }

}