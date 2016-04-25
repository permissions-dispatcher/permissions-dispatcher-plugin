package com.github.shiraji.permissionsdispatcherplugin.actions

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.lang.CodeInsightActions
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

        file.javaClass.annotations.forEach {
            System.out.println(it)
        }


//        editor.document.replaceString(1,1, "FOOO!!!")
    }

}