package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.config.GeneratePMCodeConfig
import com.github.shiraji.permissionsdispatcherplugin.data.RebuildType
import com.github.shiraji.permissionsdispatcherplugin.handlers.GeneratePMCodeHandlerJava
import com.github.shiraji.permissionsdispatcherplugin.handlers.GeneratePMCodeHandlerKt
import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.github.shiraji.permissionsdispatcherplugin.views.GeneratePMCodeDialog
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.compiler.actions.CompileProjectAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile
import javax.swing.JOptionPane

class GeneratePMCodeAction : CodeInsightAction() {
    lateinit var model: GeneratePMCodeModel
    var isKotlin: Boolean = false
    override fun getHandler(): CodeInsightActionHandler {
        if (isKotlin) {
            return GeneratePMCodeHandlerKt(model)
        } else {
            return GeneratePMCodeHandlerJava(model)
        }
    }

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)

        val project = e.getData(CommonDataKeys.PROJECT)
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val clazz = when (file) {
            is PsiJavaFile -> file.classes[0]
            is KtFile -> file.classes[0]
            else -> null
        }

        e.presentation.isEnabledAndVisible = project != null && clazz != null && GeneratePMCodeModel(project).isActivityOrFragment(clazz)
    }

    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.getData(CommonDataKeys.PROJECT) ?: return
        isKotlin = e!!.getData(CommonDataKeys.PSI_FILE) is KtFile
        val dialog = GeneratePMCodeDialog(project)
        if (dialog.showAndGet()) {
            model = GeneratePMCodeModel(project)

            model.apply {
                permissions = dialog.selectedPermissions
                if (dialog.needsPermissionCheckBox.isSelected) needsPermissionMethodName = dialog.needsPermissionTextField.text
                if (dialog.onShowRationaleCheckBox.isSelected) onShowRationaleMethodName = dialog.onShowRationaleTextField.text
                if (dialog.onPermissionDeniedCheckBox.isSelected) onPermissionDeniedMethodName = dialog.onPermissionDeniedTextField.text
                if (dialog.onNeverAskAgainCheckBox.isSelected) onNeverAskAgainMethodName = dialog.onNeverAskAgainTextField.text
            }

            super.actionPerformed(e)
            afterActionPerformed(e)
        }
    }

    private fun afterActionPerformed(e: AnActionEvent?) {
        when (RebuildType.fromId(GeneratePMCodeConfig.rebuildTypeId)) {
            RebuildType.ALWAYS -> rebuild(e)
            RebuildType.PROMPT -> {
                val result = JOptionPane.showOptionDialog(null, "Do you want to rebuild this project?", "PermissionsManager Plugin", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, arrayOf("Rebuild", "Cancel"), null)
                if (result == JOptionPane.YES_OPTION) {
                    rebuild(e)
                }
            }
            RebuildType.NOT_ALWAYS -> println("Skip rebuild.")
        }
    }

    private fun rebuild(e: AnActionEvent?) {
        CompileProjectAction().actionPerformed(e)
    }
}