package com.github.shiraji.permissionsdispatcherplugin.actions

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.refactoring.rename.PsiElementRenameHandler
import com.intellij.refactoring.rename.RenameHandler
import com.intellij.refactoring.rename.RenameJavaMethodProcessor
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler

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
            val value = it.parameterList.attributes.firstOrNull { it.name == null || it.name == "value" } ?: return@filter false
            onShowRationaleAnnotations.firstOrNull {
                it.parameterList.attributes.firstOrNull {
                    (it.name == null || it.name == "value") && it.value?.text == value.text
                } != null
            } == null
        }.isNotEmpty()
    }

    override fun getHandler(): CodeInsightActionHandler {
        return object : CodeInsightActionHandler {
            override fun startInWriteAction() = true
            override fun invoke(project: Project, editor: Editor, file: PsiFile) {
                file as? PsiJavaFile ?: return
                val (_, editor, _, clazz) = createActionEventCommonData(file, editor) ?: return
                val onShowRationaleAnnotations = clazz.getOnShowRationaleMethods().map { it.getOnShowRationaleAnnotation() }.filterNotNull()
                val needsPermissionWithoutOnShowRationale = clazz.getNeedsPermissionMethods().map { it.getNeedsPermissionAnnotation() }.filterNotNull().filter {
                    val value = it.parameterList.attributes.firstOrNull { it.name == null || it.name == "value" } ?: return@filter false
                    onShowRationaleAnnotations.firstOrNull {
                        it.parameterList.attributes.firstOrNull {
                            (it.name == null || it.name == "value") && it.value?.text == value.text
                        } != null
                    } == null
                }

                when(needsPermissionWithoutOnShowRationale.size) {
                    0 -> return
                    1 -> createOnShowRationaleMethod(project, editor, clazz, needsPermissionWithoutOnShowRationale[0])
                }

            }

            private fun createOnShowRationaleMethod(project: Project, editor: Editor, clazz: PsiClass, psiAnnotation: PsiAnnotation) {
                runWriteAction {
                    val factory = JavaPsiFacade.getElementFactory(project)
                    val onShowRationaleMethod = factory.createMethodFromText("""
                    @OnShowRationale(${psiAnnotation.parameterList.attributes.first { it.name == null || it.name == "value" }.value!!.text})
                    void methodName(final PermissionRequest request) {
                    }
                    """.trimIndent(), clazz)
                    val addedElement = clazz.addAfter(onShowRationaleMethod, clazz.methods.last())

                    // TODO fix these renaming feature!
                    PsiElementRenameHandler.invoke(addedElement, project, clazz.containingFile, editor)

                    VariableInplaceRenameHandler().invoke(project, editor, clazz.containingFile) {
                        addedElement
                    }

                }
            }
        }
    }
}