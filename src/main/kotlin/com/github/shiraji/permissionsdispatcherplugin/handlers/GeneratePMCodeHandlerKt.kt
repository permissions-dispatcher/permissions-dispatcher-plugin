package com.github.shiraji.permissionsdispatcherplugin.handlers

import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ImportPath

class GeneratePMCodeHandlerKt(model: GeneratePMCodeModel) : GeneratePMCodeHandler(model) {

    private lateinit var file: KtFile

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (file !is KtFile) return
        this.file = file
        super.invoke(project, editor, file)
    }

    override fun createOnPermissionDeniedMethodTemplate(): String {
        return """fun ${model.onPermissionDeniedMethodName}() {
                }""".trimMargin()
    }

    override fun createOnNeverAskAgainMethodTemplate(): String {
        return """fun ${model.onNeverAskAgainMethodName}() {
                }""".trimMargin()
    }

    override fun createOnShowRationaleMethodTemplate(): String {
        return """fun ${model.onShowRationaleMethodName}(request: PermissionRequest) {
                }""".trimMargin()
    }

    override fun createNeedsPermissionMethodTemplate(): String {
        return """fun ${model.needsPermissionMethodName}() {
               }""".trimMargin()
    }

    override fun createOnRequestPermissionsResultMethodTemplate(): String {
        return """override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                onRequestPermissionsResult(requestCode, grantResults)
               }""".trimMargin()
    }

    override fun createOnRequestPermissionsResultStatementTemplate(): String {
        val method = getMethod("onRequestPermissionsResult")
        return "onRequestPermissionsResult(${method!!.valueParameters[0].name}, ${method.valueParameters[2].name})"
    }

    override fun createOnActivityResultMethodTemplate(): String {
        return """override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
                super.onActivityResult(requestCode, resultCode, data)
                onActivityResult(requestCode)
            }""".trimMargin()
    }

    override fun createOnActivityResultStatementTemplate(): String {
        val method = getMethod("onActivityResult")
        return "onActivityResult(${method!!.valueParameters[0].name})"
    }

    override fun addStatementToMethod(statement: String, methodName: String) {
        val method = getMethod(methodName) ?: return
        val hasStatement = method.bodyExpression?.text?.contains(statement) ?: false
        if (!hasStatement) {
            method.bodyExpression?.addBefore(KtPsiFactory(project).createExpression(statement), (method.bodyExpression as KtBlockExpression).rBrace)
        }
    }

    private fun getMethod(methodName: String): KtNamedFunction? {
        val clazz = file.declarations[0] as KtClass
        val method = clazz.getBody()?.children?.find { (it as KtNamedFunction).name == methodName } ?: return null
        return method as KtNamedFunction
    }

    override fun hasMethod(name: String): Boolean {
        return file.classes[0].findMethodsByName(name, false).isNotEmpty()
    }

    override fun addPMMethod(methodTemplate: String, annotation: String) {
        addMethod(methodTemplate, "$annotation(${model.toListParameter()})")
    }

    override fun createNeedsPermissionMethod() {
        if (model.maxSdkVersion < 0) {
            addMethod(createNeedsPermissionMethodTemplate(), "NeedsPermission(${model.toListParameter()})")
        } else {
            addMethod(createNeedsPermissionMethodTemplate(), "NeedsPermission(${model.toListParameter()}, maxSdkVersion = ${model.maxSdkVersion})")
        }
    }

    override fun addMethod(methodTemplate: String, annotation: String) {
        val psiFactory = KtPsiFactory(project)
        val function = psiFactory.createFunction(methodTemplate)
        if (!annotation.equals("Override", false)) {
            val entry = function.addAnnotationEntry(psiFactory.createAnnotationEntry("@$annotation"))
            entry.add(psiFactory.createNewLine())
        }
        val clazz = file.declarations[0] as KtClass
        clazz.getBody()!!.addBefore(function, clazz.getBody()!!.rBrace)
    }

    override fun addAnnotationToClass(fullName: String, name: String) {
        if (file.classes[0].modifierList?.findAnnotation(fullName) != null) return
        val psiFactory = KtPsiFactory(project)
        val annotationEntry = psiFactory.createAnnotationEntry("@$name")
        val entry = file.declarations[0].addAnnotationEntry(annotationEntry)
        entry.add(psiFactory.createNewLine())
    }

    override fun addImport(import: String) {
        val psiFactory = KtPsiFactory(project)
        val importDirective = psiFactory.createImportDirective(ImportPath(FqName(import), false))
        if (file.importDirectives.none { it.importPath == importDirective.importPath }) {
            file.importList?.add(importDirective)
        }
    }

}