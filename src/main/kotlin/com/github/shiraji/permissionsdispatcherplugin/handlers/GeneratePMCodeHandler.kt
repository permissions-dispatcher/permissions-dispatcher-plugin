package com.github.shiraji.permissionsdispatcherplugin.handlers

import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

abstract class GeneratePMCodeHandler(val model: GeneratePMCodeModel) : CodeInsightActionHandler {
    lateinit var project: Project
    lateinit var editor: Editor

    override fun startInWriteAction() = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        this.project = project
        this.editor = editor

        addRuntimePermissionAnnotation()
        addNeedsPermissionMethod()
        if (model.isSpecialPermissions()) {
            addOnActivityResult()
        } else {
            addOnRequestPermissionsResult()
        }
        addOnShowRationale()
        addOnPermissionDenied()
        addOnNeverAskAgain()
    }

    fun addRuntimePermissionAnnotation() {
        addAnnotationToClass("permissions.dispatcher.RuntimePermissions", "RuntimePermissions")
        addImport("permissions.dispatcher.RuntimePermissions")
    }

    fun addNeedsPermissionMethod() {
        if (model.needsPermissionMethodName.length <= 0) return
        addPMMethod(createNeedsPermissionMethodTemplate(), "NeedsPermission")
        addImport("permissions.dispatcher.NeedsPermission")
        addImport("android.Manifest")
    }

    fun addOnShowRationale() {
        if (model.onShowRationaleMethodName.length <= 0) return
        addPMMethod(createOnShowRationaleMethodTemplate(), "OnShowRationale")
        addImport("permissions.dispatcher.PermissionRequest")
        addImport("permissions.dispatcher.OnShowRationale")
    }

    fun addOnPermissionDenied() {
        if (model.onPermissionDeniedMethodName.length <= 0) return
        addPMMethod(createOnPermissionDeniedMethodTemplate(), "OnPermissionDenied")
        addImport("permissions.dispatcher.OnPermissionDenied")
    }

    fun addOnNeverAskAgain() {
        if (model.onNeverAskAgainMethodName.length <= 0) return
        addPMMethod(createOnNeverAskAgainMethodTemplate(), "OnNeverAskAgain")
        addImport("permissions.dispatcher.OnNeverAskAgain")
    }

    fun addOnRequestPermissionsResult() {
        if (hasMethod("onRequestPermissionsResult")) {
            addMethod(createOnRequestPermissionsResultMethodTemplate(), "Override")
        } else {
            addStatementToMethod(createOnRequestPermissionsResultStatementTemplate(), "onRequestPermissionsResult")
        }
    }

    fun addOnActivityResult() {
        if (hasMethod("onActivityResult")) {
            addMethod(createOnActivityResultMethodTemplate(), "Override")
        } else {
            addStatementToMethod(createOnActivityResultStatementTemplate(), "onActivityResult")
        }
    }

    abstract fun hasMethod(name: String): Boolean
    abstract fun createNeedsPermissionMethodTemplate(): String
    abstract fun createOnShowRationaleMethodTemplate(): String
    abstract fun createOnPermissionDeniedMethodTemplate(): String
    abstract fun createOnNeverAskAgainMethodTemplate(): String
    abstract fun createOnRequestPermissionsResultMethodTemplate(): String
    abstract fun createOnRequestPermissionsResultStatementTemplate(): String
    abstract fun createOnActivityResultMethodTemplate(): String
    abstract fun createOnActivityResultStatementTemplate(): String
    abstract fun addStatementToMethod(statement: String, methodName: String)
    abstract fun addPMMethod(methodTemplate: String, annotation: String)
    abstract fun addMethod(methodTemplate: String, annotation: String)
    abstract fun addAnnotationToClass(fullName: String, name: String)
    abstract fun addImport(import: String)
}