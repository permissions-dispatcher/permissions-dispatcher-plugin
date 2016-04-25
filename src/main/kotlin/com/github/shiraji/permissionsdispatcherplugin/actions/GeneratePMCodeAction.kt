package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

class GeneratePMCodeAction : CodeInsightAction() {
    override fun getHandler(): CodeInsightActionHandler {
        return Handler()
    }
}

class Handler : CodeInsightActionHandler {
    override fun startInWriteAction() = false

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if(file !is PsiJavaFile) return

        val model = GeneratePMCodeModel()

        if (model.isActivity(file.classes[0], project)) {
            System.out.println("Activity!")
        }

        if (model.isSupportFragment(file.classes[0], project)) {
            System.out.println("SupportFragment!")
        }

        file.javaClass.annotations.forEach {
            System.out.println(it)
        }


//        editor.document.replaceString(1,1, "FOOO!!!")
    }

}