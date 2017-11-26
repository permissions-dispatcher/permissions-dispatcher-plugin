package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.config.GeneratePMCodeConfig
import com.github.shiraji.permissionsdispatcherplugin.data.PdVersion
import com.github.shiraji.permissionsdispatcherplugin.data.RebuildType
import com.github.shiraji.permissionsdispatcherplugin.extentions.generateVersionNumberFrom
import com.github.shiraji.permissionsdispatcherplugin.handlers.GeneratePMCodeHandlerJava
import com.github.shiraji.permissionsdispatcherplugin.handlers.GeneratePMCodeHandlerKt
import com.github.shiraji.permissionsdispatcherplugin.models.GeneratePMCodeModel
import com.github.shiraji.permissionsdispatcherplugin.views.GeneratePMCodeDialog
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.compiler.actions.CompileProjectAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
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

        val clazz = if (file is PsiJavaFile) {
            file.classes[0]
        } else {
            try {
                if (file is KtFile) file.classes[0] else null
            } catch (e: NoClassDefFoundError) {
                null
            }
        }

        e.presentation.isEnabledAndVisible = project != null && clazz != null && GeneratePMCodeModel(project).isActivityOrFragment(clazz)
    }

    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.getData(CommonDataKeys.PROJECT) ?: return

        try {
            isKotlin = e.getData(CommonDataKeys.PSI_FILE) is KtFile
        } catch (e: NoClassDefFoundError) {
            isKotlin = false
        }

        var pdVersion: PdVersion = PdVersion.NOTFOUND

        fun updatePdVersion(dependenciesBlock: GrMethodCallExpression) {
            val pdLine = dependenciesBlock.findDescendantOfType<GrCommandArgumentList> {
                it.text.contains("com.github.hotchemi:permissionsdispatcher:")
            }

            pdLine?.text?.let {
                text ->
                // for now, forget about variables...
                val versionText = text.generateVersionNumberFrom()
                pdVersion = PdVersion.fromText(versionText)
            }
        }

        fun generatePMCode() {
            val dialog = GeneratePMCodeDialog(project, pdVersion)
            if (dialog.showAndGet()) {
                model = GeneratePMCodeModel(project)

                model.apply {
                    permissions = dialog.selectedPermissions
                    if (dialog.needsPermissionCheckBox.isSelected) needsPermissionMethodName = dialog.needsPermissionTextField.text
                    if (dialog.onShowRationaleCheckBox.isSelected) onShowRationaleMethodName = dialog.onShowRationaleTextField.text
                    if (dialog.onPermissionDeniedCheckBox.isSelected) onPermissionDeniedMethodName = dialog.onPermissionDeniedTextField.text
                    if (dialog.onNeverAskAgainCheckBox.isSelected) onNeverAskAgainMethodName = dialog.onNeverAskAgainTextField.text
                    val maxSdkVersionText = dialog.maxSdkVersionTextField.text
                    if (maxSdkVersionText != null && maxSdkVersionText.isNotBlank()) {
                        maxSdkVersion = maxSdkVersionText.toInt()
                    }
                }

                super.actionPerformed(e)
                rebuildAction(e)
            }
        }

        FilenameIndex.getAllFilesByExt(project, "gradle", GlobalSearchScope.projectScope(project)).forEach {
            val groovyFile = PsiManager.getInstance(project).findFile(it) as? GroovyFile ?: return@forEach
            val dependenciesBlock = groovyFile.findDescendantOfType<GrMethodCallExpression> {
                it.invokedExpression.text == "dependencies"
            } ?: return@forEach
            updatePdVersion(dependenciesBlock)
        }

//        comment out until I found the best way to find a dependency version.
//        if (pdVersion == PdVersion.NOTFOUND) {
//            // no dependencies found for PermissionsDispatcher!
//            Notifications.Bus.notify(Notification(
//                    "PermissionsManager Plugin",
//                    "No PermissionsDispatcher dependency",
//                    "Please add PermissionsDispatcher dependency",
//                    NotificationType.WARNING))
//        }

        generatePMCode()
    }

    private fun rebuildAction(e: AnActionEvent?) {
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