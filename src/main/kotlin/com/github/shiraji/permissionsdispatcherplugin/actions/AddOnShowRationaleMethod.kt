package com.github.shiraji.permissionsdispatcherplugin.actions

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.refactoring.RefactoringFactory
import com.intellij.refactoring.actions.RenameElementAction
import com.intellij.refactoring.rename.*
import com.intellij.refactoring.rename.inplace.MemberInplaceRenameHandler
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import org.jetbrains.kotlin.psi.psiUtil.startOffset

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
                    1 -> createOnShowRationaleMethod(file, project, editor, clazz, needsPermissionWithoutOnShowRationale[0])
                }

            }

            private fun createOnShowRationaleMethod(file: PsiFile, project: Project, editor: Editor, clazz: PsiClass, psiAnnotation: PsiAnnotation) {
                // popup for asking method name
                runWriteAction {
                    val factory = JavaPsiFacade.getElementFactory(project)
                    val onShowRationaleMethod = factory.createMethodFromText("""
                    @OnShowRationale(${psiAnnotation.parameterList.attributes.first { it.name == null || it.name == "value" }.value!!.text})
                    void METHOD_NAME(final PermissionRequest request) {
                    }
                    """.trimIndent(), clazz)

                    val addedElement = clazz.addAfter(onShowRationaleMethod, clazz.methods.last()) as PsiMethod

//                    editor.caretModel.currentCaret.moveToOffset(addedElement.nameIdentifier!!.startOffset)
//
//                    MemberInplaceRenameHandler().invoke(project, arrayOf(addedElement.nameIdentifier)) {
//                        when(it) {
//                            "editor" -> editor
//                            "psi.Element" -> addedElement
//                            "psi.Element.array" -> arrayOf(addedElement)
//                            "psi.File" -> file
//                            "project" -> project
//                            "caret" -> editor.caretModel.currentCaret
//                            else -> null
//                        }
//                    }
                }
            }
        }
    }
}