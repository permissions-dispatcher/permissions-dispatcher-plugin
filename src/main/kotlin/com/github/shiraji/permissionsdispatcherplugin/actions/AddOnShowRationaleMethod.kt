package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.extentions.*
import com.github.shiraji.permissionsdispatcherplugin.views.AddOnShowRationaleDialog
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*

class AddOnShowRationaleMethod : CodeInsightAction() {

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)

        val data = createActionEventCommonData(e)
        if (data == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val clazz = data.clazz
        val onShowRationaleAnnotations = clazz.getOnShowRationaleMethods().map { it.getOnShowRationaleAnnotation() }.filterNotNull()
        e.presentation.isEnabledAndVisible = clazz.getNeedsPermissionMethods().map { it.getNeedsPermissionAnnotation() }.filterNotNull().filter {
            val value = it.getValueAttribute() ?: return@filter false
            onShowRationaleAnnotations.firstOrNull {
                it.getValueAttribute()?.text == value.text
            } == null
        }.isNotEmpty()
    }

    override fun getHandler(): CodeInsightActionHandler {
        return object : CodeInsightActionHandler {
            override fun startInWriteAction() = true
            override fun invoke(project: Project, editor: Editor, file: PsiFile) {
                file as? PsiJavaFile ?: return
                val (_, _, _, clazz) = createActionEventCommonData(file, editor) ?: return
                val onShowRationaleAnnotations = clazz.getOnShowRationaleMethods().map { it.getOnShowRationaleAnnotation() }.filterNotNull()
                val needsPermissionWithoutOnShowRationale = clazz.getNeedsPermissionMethods().map { it.getNeedsPermissionAnnotation() }.filterNotNull().filter {
                    val value = it.getValueAttribute() ?: return@filter false
                    onShowRationaleAnnotations.firstOrNull {
                        it.getValueAttribute()?.text == value.text
                    } == null
                }

                if(needsPermissionWithoutOnShowRationale.isEmpty()) return
                createOnShowRationaleMethod(project, file, clazz, needsPermissionWithoutOnShowRationale)
            }

            private fun createOnShowRationaleMethod(project: Project, file: PsiJavaFile, clazz: PsiClass, psiAnnotations: List<PsiAnnotation>) {
                val dialog = AddOnShowRationaleDialog(psiAnnotations)
                if (!dialog.showAndGet()) return
                val annotation = psiAnnotations[dialog.annotationComboBox.selectedIndex].getValueAttribute()?.value?.text
                val methodName = dialog.methodNameTextField.text

                runWriteAction {
                    val factory = JavaPsiFacade.getElementFactory(project)
                    val onShowRationaleMethod = factory.createMethodFromText("""
                    @OnShowRationale($annotation)
                    void $methodName(final PermissionRequest request) {
                    }
                    """.trimIndent(), clazz)

                    clazz.addAfter(onShowRationaleMethod, clazz.methods.last())
                    file.importForOnRationale()
                }
            }
        }
    }
}