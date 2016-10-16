package com.github.shiraji.permissionsdispatcherplugin.handlers

import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

class GeneratePMCodeHandlerJava(model: GeneratePMCodeModel) : GeneratePMCodeHandler(model) {

    private lateinit var file: PsiJavaFile

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (file !is PsiJavaFile) return
        this.file = file
        super.invoke(project, editor, file)
    }

    override fun createOnPermissionDeniedMethodTemplate(): String {
        return """void ${model.onPermissionDeniedMethodName}() {
                }""".trimMargin()
    }

    override fun createOnNeverAskAgainMethodTemplate(): String {
        return """void ${model.onNeverAskAgainMethodName}() {
                }""".trimMargin()
    }

    override fun createOnShowRationaleMethodTemplate(): String {
        return """void ${model.onShowRationaleMethodName}(final PermissionRequest request) {
                }""".trimMargin()
    }

    override fun createNeedsPermissionMethodTemplate(): String {
        return """void ${model.needsPermissionMethodName}() {
               }""".trimMargin()
    }

    override fun createOnRequestPermissionsResultMethodTemplate(): String {
        return """public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                ${file.classes[0].name}PermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
            }""".trimMargin()
    }

    override fun createOnRequestPermissionsResultStatementTemplate(): String {
        val methods = file.classes[0].findMethodsByName("onRequestPermissionsResult", false)
        assert(methods.isNotEmpty())
        return "${file.classes[0].name}PermissionsDispatcher.onRequestPermissionsResult(this, ${methods[0].parameterList.parameters[0].name}, ${methods[0].parameterList.parameters[2].name});"
    }

    override fun createOnActivityResultMethodTemplate(): String {
        return """public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                ${file.classes[0].name}PermissionsDispatcher.onActivityResult(this, requestCode);
            }""".trimMargin()
    }

    override fun createOnActivityResultStatementTemplate(): String {
        val methods = file.classes[0].findMethodsByName("onActivityResult", false)
        return "${file.classes[0].name}PermissionsDispatcher.onActivityResult(this, ${methods[0].parameterList.parameters[0].name});"
    }

    override fun addStatementToMethod(statement: String, methodName: String) {
        val methods = file.classes[0].findMethodsByName(methodName, false)
        val hasStatement = methods[0].body?.text?.contains(statement) ?: false
        if (!hasStatement) {
            val expression = JavaPsiFacade.getElementFactory(project).createStatementFromText(statement, file.classes[0])
            methods[0].body?.add(expression)
        }
    }

    override fun hasMethod(name: String): Boolean {
        return file.classes[0].findMethodsByName(name, false).isNotEmpty()
    }

    override fun addPMMethod(methodTemplate: String, annotation: String) {
        addMethod(methodTemplate, "$annotation(${model.toPermissionParameter()})")
    }

    override fun createNeedsPermissionMethod() {
        if (model.maxSdkVersion < 0) {
            addMethod(createNeedsPermissionMethodTemplate(), "NeedsPermission(${model.toPermissionParameter()})")
        } else {
            addMethod(createNeedsPermissionMethodTemplate(), "NeedsPermission(value = ${model.toPermissionParameter()}, maxSdkVersion = ${model.maxSdkVersion})")
        }
    }

    override fun addMethod(methodTemplate: String, annotation: String) {
        val method = JavaPsiFacade.getElementFactory(project).createMethodFromText(methodTemplate, file.classes[0])
        method.modifierList.addAnnotation(annotation)
        file.classes[0].add(method)
    }

    override fun addAnnotationToClass(fillName: String, name: String) {
        if (file.classes[0].modifierList?.findAnnotation(fillName) != null) return
        file.classes[0].modifierList?.addAnnotation(name)
    }

    override fun addImport(import: String) {
        file.importClass(model.createPsiClass(import) ?: return)
    }

}