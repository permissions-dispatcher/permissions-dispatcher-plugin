package com.github.shiraji.permissionsdispatcherplugin.handlers

import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

class GeneratePMCodeHandlerKt(val model: GeneratePMCodeModel) : CodeInsightActionHandler {
    override fun startInWriteAction() = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (file !is KtFile) return
        addRuntimePermissionAnnotation(file, project)
        addNeedsPermissionMethod(file, project)
        if (model.isSpecialPermissions()) {
            addOnActivityResult(file, project)
        } else {
            addOnRequestPermissionsResult(file, project)
        }
        addOnShowRationale(file, project)
        addOnPermissionDenied(file, project)
        addOnNeverAskAgain(file, project)
    }

    private fun addOnNeverAskAgain(file: KtFile, project: Project) {
        if (model.onNeverAskAgainMethodName.length <= 0) return
        val psiFactory = KtPsiFactory(project)
        val function = psiFactory.createFunction(
                """
                fun ${model.onNeverAskAgainMethodName}() {
                }
                """.trimMargin()
        )

        function.addAnnotationEntry(psiFactory.createAnnotationEntry("@OnNeverAskAgain(${model.toListParameter()})")).add(psiFactory.createNewLine())

        val clazz = file.declarations[0] as KtClass
        clazz.getBody()!!.addBefore(function, clazz.getBody()!!.rBrace)

        addImport(file, project, "permissions.dispatcher.OnNeverAskAgain")
    }

    private fun addOnPermissionDenied(file: KtFile, project: Project) {
        if (model.onPermissionDeniedMethodName.length <= 0) return
        val psiFactory = KtPsiFactory(project)
        val function = psiFactory.createFunction(
                """
                fun ${model.onPermissionDeniedMethodName}() {
                }
                """.trimMargin()
        )

        function.addAnnotationEntry(psiFactory.createAnnotationEntry("@OnPermissionDenied(${model.toListParameter()})")).add(psiFactory.createNewLine())

        val clazz = file.declarations[0] as KtClass
        clazz.getBody()!!.addBefore(function, clazz.getBody()!!.rBrace)

        addImport(file, project, "permissions.dispatcher.OnPermissionDenied")
    }

    private fun addOnShowRationale(file: KtFile, project: Project) {
        if (model.onShowRationaleMethodName.length <= 0) return
        val psiFactory = KtPsiFactory(project)
        val function = psiFactory.createFunction(
                """fun ${model.onShowRationaleMethodName}(request: PermissionRequest) {
                }""".trimMargin()
        )

        val entry = function.addAnnotationEntry(psiFactory.createAnnotationEntry("@OnShowRationale(${model.toListParameter()})"))
        entry.add(psiFactory.createNewLine())

        val clazz = file.declarations[0] as KtClass
        clazz.getBody()!!.addBefore(function, clazz.getBody()!!.rBrace)

        addImport(file, project, "permissions.dispatcher.PermissionRequest")
        addImport(file, project, "permissions.dispatcher.OnShowRationale")
    }

    private fun addOnRequestPermissionsResult(file: KtFile, project: Project) {
        //        val methods = file.classes[0].findMethodsByName("onRequestPermissionsResult", false)

        val clazz = file.declarations[0] as KtClass
        val method = clazz.getBody()!!.children.find { (it as KtNamedFunction).name == "onRequestPermissionsResult" }
        val psiFactory = KtPsiFactory(project)
        if (method == null) {
            val function = psiFactory.createFunction(
                    """override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
                        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                        ${file.classes[0].name}PermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
                    }""".trimMargin()
            )
            clazz.getBody()!!.addBefore(function, clazz.getBody()!!.rBrace)
        } else {
            // TODO insert onRequestPermissionsResult if it is missing.
            //            val statement = "${file.classes[0].name}PermissionsDispatcher.onRequestPermissionsResult(this, ${methods[0].parameterList.parameters[0].name}, ${methods[0].parameterList.parameters[2].name})"
            //            if (((method as KtNamedFunction).bodyExpression as KtBlockExpression).statements.none { it.text == statement }) {
            //                (method.bodyExpression as KtBlockExpression).add(psiFactory.createExpression(statement))
            //            }
        }
    }

    private fun addOnActivityResult(file: KtFile, project: Project) {
        val clazz = file.declarations[0] as KtClass
        val method = clazz.getBody()!!.children.find { (it as KtNamedFunction).name == "onActivityResult" }
        val psiFactory = KtPsiFactory(project)
        if (method == null) {
            val function = psiFactory.createFunction("""override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
                super.onActivityResult(requestCode, resultCode, data);
                ${file.classes[0].name}PermissionsDispatcher.onActivityResult(this, requestCode)
            }""".trimMargin())
            clazz.getBody()!!.addBefore(function, clazz.getBody()!!.rBrace)
            addImport(file, project, "android.content.Intent")
        } else {
            // TODO insert onActivityResult if it is missing.
            //            val statement = "${file.classes[0].name}PermissionsDispatcher.onActivityResult(this, ${methods[0].parameterList.parameters[0].name})"
            //            val hasDelegation = methods[0].body?.text?.contains(statement) ?: false
            //            if (!hasDelegation) {
            //                val expression = JavaPsiFacade.getElementFactory(project).createStatementFromText(statement, file.classes[0])
            //                methods[0].body?.add(expression)
            //            }
        }
    }

    private fun addNeedsPermissionMethod(file: KtFile, project: Project) {
        if (model.needsPermissionMethodName.length <= 0) return
        val psiFactory = KtPsiFactory(project)
        val function = psiFactory.createFunction(
                """
                fun ${model.needsPermissionMethodName}() {
                }
                """.trimMargin()
        )

        val entry = function.addAnnotationEntry(psiFactory.createAnnotationEntry("@NeedsPermission(${model.toListParameter()})"))
        entry.add(psiFactory.createNewLine())

        val clazz = file.declarations[0] as KtClass
        clazz.getBody()!!.addBefore(function, clazz.getBody()!!.rBrace)

        addImport(file, project, "permissions.dispatcher.NeedsPermission")
        addImport(file, project, "android.Manifest")
    }

    private fun addRuntimePermissionAnnotation(file: KtFile, project: Project) {
        if (file.classes[0].modifierList?.findAnnotation("permissions.dispatcher.RuntimePermissions") != null) return
        val psiFactory = KtPsiFactory(project)
        val annotationEntry = psiFactory.createAnnotationEntry("@RuntimePermissions")
        val entry = file.declarations[0].addAnnotationEntry(annotationEntry)
        entry.add(psiFactory.createNewLine())

        addImport(file, project, "permissions.dispatcher.RuntimePermissions")
    }

    private fun addImport(file: KtFile, project: Project, importPath: String) {
        val psiFactory = KtPsiFactory(project)
        val importDirective = psiFactory.createImportDirective(ImportPath(importPath))
        if (file.importDirectives.none { it.importPath == importDirective.importPath }) {
            file.importList?.add(importDirective)
        }
    }

}