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
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression

class AddPMDependencies : CodeInsightAction() {

    override fun getHandler(): CodeInsightActionHandler {
        return AddPMDependenciesHandler()
    }

    override fun update(e: AnActionEvent?) {
        e ?: return
        super.update(e)
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        FilenameIndex.getAllFilesByExt(project, "gradle", GlobalSearchScope.projectScope(project)).forEach {
            val groovyFile = PsiManager.getInstance(project).findFile(it) as? GroovyFile ?: return@forEach
            val dependenciesBlock = groovyFile.findDescendantOfType<GrMethodCallExpression> { it.invokedExpression.text == "dependencies" } ?: return@forEach
            if (dependenciesBlock.findDescendantOfType<GrCommandArgumentList> { it.text.contains("com.github.hotchemi:permissionsdispatcher:") } != null) {
                e.presentation.isEnabledAndVisible = false
                return
            }
        }
    }

    class AddPMDependenciesHandler : CodeInsightActionHandler {

        override fun startInWriteAction() = true

        override fun invoke(project: Project, editor: Editor, file: PsiFile) {
            var hasAndroidApt = false
            var useKapt = false
            var androidGradleVersion: AndroidGradleVersion? = null

            FilenameIndex.getAllFilesByExt(project, "gradle", GlobalSearchScope.projectScope(project)).forEach {
                val groovyFile = PsiManager.getInstance(project).findFile(it) as? GroovyFile ?: return@forEach
                if (groovyFile.findDescendantOfType<GrApplicationStatement> { it.text.contains("\'android-apt\'") } != null) hasAndroidApt = true
                if (groovyFile.findDescendantOfType<GrApplicationStatement> { it.text.contains("\'kotlin-android\'") } != null) useKapt = true

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

            if (file !is GroovyFile) return

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
                    val dependenciesBlock = file.findDescendantOfType<GrMethodCallExpression> { it.invokedExpression.text == "dependencies" } ?: return
                    val factory = GroovyPsiElementFactory.getInstance(project)
                    val aptRef = when {
                        useKapt -> "kapt"
                        hasAndroidApt -> "apt"
                        else -> "annotationProcessor"
                    }
                    val compileExpression = factory.createExpressionFromText("compile 'com.github.hotchemi:permissionsdispatcher:${PdVersion.latestVersion}'")
                    val annotationProcessorExpression = factory.createExpressionFromText("$aptRef 'com.github.hotchemi:permissionsdispatcher-processor:${PdVersion.latestVersion}'")
                    dependenciesBlock.closureArguments[0]?.let {
                        it.addBefore(compileExpression, it.rBrace)
                        it.addBefore(annotationProcessorExpression, it.rBrace)
                    }
                }
            }
        }
    }
}

