package com.github.shiraji.permissionsdispatcherplugin.handlers

import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

class GeneratePMCodeHandler : CodeInsightActionHandler {
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
            val methodTemplate = """public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                ${file.classes[0].name}PermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
            }""".trimMargin()

            val method = JavaPsiFacade.getElementFactory(project).createMethodFromText(methodTemplate, file.classes[0])
            method.modifierList.addAnnotation("Override")
            file.classes[0].add(method)
        } else {
            // TODO check there is XxxPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults); and if not, add the line
        }
    }

    private fun addNeedsPermissionMethod(file: PsiJavaFile, model: GeneratePMCodeModel, project: Project) {
        val methodTemplate = """void ${model.needsPermissionMethodName}() {
        }""".trimMargin()

        val method = JavaPsiFacade.getElementFactory(project).createMethodFromText(methodTemplate, file.classes[0])
        method.modifierList.addAnnotation("NeedsPermission(${model.toPermissionParameter()})")
        file.importClass(model.createPsiClass("permissions.dispatcher.NeedsPermission", project))
        file.classes[0].add(method)
    }

    private fun addRuntimePermissionAnnotation(file: PsiJavaFile, model: GeneratePMCodeModel, project: Project) {
        if (file.classes[0].modifierList?.findAnnotation("permissions.dispatcher.RuntimePermissions") != null) return
        file.classes[0].modifierList?.addAnnotation("RuntimePermissions")
        file.importClass(model.createPsiClass("permissions.dispatcher.RuntimePermissions", project))
    }

}