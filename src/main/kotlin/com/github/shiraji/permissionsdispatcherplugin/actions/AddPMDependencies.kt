package com.github.shiraji.permissionsdispatcherplugin.actions

import com.github.shiraji.permissionsdispatcherplugin.data.AndroidGradleVersion
import com.github.shiraji.permissionsdispatcherplugin.data.PdVersion
import com.github.shiraji.permissionsdispatcherplugin.extentions.generateVersionNumberFrom
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import javax.swing.JOptionPane

class AddPMDependencies : CodeInsightAction() {

    override fun getHandler(): CodeInsightActionHandler {
        return AddPMDependenciesHandler()
    }

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val dependenciesBlocks = FilenameIndex.getAllFilesByExt(project, "gradle", GlobalSearchScope.projectScope(project)).map {
            PsiManager.getInstance(project).findFile(it) as? GroovyFile
        }.filterNotNull().filter {
            it.findDescendantOfType<GrCommandArgumentList> { it.text.contains("com.android.application") } != null
        }.flatMap {
            it.collectDescendantsOfType<GrMethodCallExpression> { it.invokedExpression.text == "dependencies" }
        }

        if (dependenciesBlocks.firstOrNull {
            it.findDescendantOfType<GrCommandArgumentList> { it.text.contains("com.github.hotchemi:permissionsdispatcher:") } != null
        } != null) {
            e.presentation.isEnabledAndVisible = false
        }
    }

    class AddPMDependenciesHandler : CodeInsightActionHandler {

        override fun startInWriteAction() = true

        override fun invoke(project: Project, editor: Editor, file: PsiFile) {

            val result = JOptionPane.showOptionDialog(null,
                    "Do you use native Fragment (android.app.Fragment)?",
                    "Add PermissionsDispatcher dependencies",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null)
            if (result == JOptionPane.CANCEL_OPTION) return
            val isUseNativeFragment = result == JOptionPane.OK_OPTION
            var hasAndroidApt = false
            var useKapt = false
            var androidGradleVersion: AndroidGradleVersion? = null
            var targetFile: GroovyFile? = null

            FilenameIndex.getAllFilesByExt(project, "gradle", GlobalSearchScope.projectScope(project)).forEach {
                val groovyFile = PsiManager.getInstance(project).findFile(it) as? GroovyFile ?: return@forEach
                if (groovyFile.findDescendantOfType<GrApplicationStatement> { it.text.contains("\'android-apt\'") } != null) hasAndroidApt = true
                if (groovyFile.findDescendantOfType<GrApplicationStatement> { it.text.contains("\'kotlin-android\'") } != null) useKapt = true
                if (groovyFile.findDescendantOfType<GrCommandArgumentList> { it.text.contains("com.android.application") } != null) targetFile = groovyFile

                val androidGradleBuildLine = groovyFile.findDescendantOfType<GrCommandArgumentList> {
                    it.text.contains("com.android.tools.build:gradle:")
                }

                androidGradleBuildLine?.text?.let {
                    text ->
                    // for now, forget about variables...
                    val versionText = text.generateVersionNumberFrom()
                    androidGradleVersion = AndroidGradleVersion(versionText)
                }
            }

            val version = androidGradleVersion
            when {
                version == null ->
                    Notifications.Bus.notify(Notification(
                            "PermissionsManager Plugin",
                            "[PermissionsManager Plugin] No Android Gradle Version found",
                            "No android gradle version found. To avoid generating wrong dependency, stop 'Add PermissionsDispatcher dependencies'",
                            NotificationType.INFORMATION))
                !useKapt && version.isValid() && !version.isHigherThan2_2() && !hasAndroidApt ->
                    Notifications.Bus.notify(Notification(
                            "PermissionsManager Plugin",
                            "[PermissionsManager Plugin] Missing settings",
                            "No annotation processing settings found. Use 'android gradle plugin version >= 2.2' or 'android-apt'",
                            NotificationType.WARNING))
                else -> {
                    val dependenciesBlock = targetFile?.findDescendantOfType<GrMethodCallExpression> { it.invokedExpression.text == "dependencies" } ?: return
                    val factory = GroovyPsiElementFactory.getInstance(project)
                    val aptRef = when {
                        useKapt -> "kapt"
                        hasAndroidApt -> "apt"
                        else -> "annotationProcessor"
                    }

                    var compileDepTemplate = "compile 'com.github.hotchemi:permissionsdispatcher:${PdVersion.latestVersion}'"
                    if (!isUseNativeFragment) compileDepTemplate += " { exclude module: 'support-v13' }"
                    val compileExpression = factory.createExpressionFromText(compileDepTemplate)
                    val annotationProcessorExpression = factory.createExpressionFromText("$aptRef 'com.github.hotchemi:permissionsdispatcher-processor:${PdVersion.latestVersion}'")
                    dependenciesBlock.closureArguments[0]?.run {
                        val applicationStatement = addBefore(compileExpression, rBrace) as? GrApplicationStatement
                        addBefore(annotationProcessorExpression, rBrace)
                        applicationStatement?.navigate(true)
                    }
                }
            }
        }
    }
}
