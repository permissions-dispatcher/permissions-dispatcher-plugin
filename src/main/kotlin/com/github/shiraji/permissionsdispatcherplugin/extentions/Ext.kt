package com.github.shiraji.permissionsdispatcherplugin.extentions

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope

private const val NEEDS_PERMISSION = "permissions.dispatcher.NeedsPermission"
private const val ON_SHOW_RATIONALE = "permissions.dispatcher.OnShowRationale"
private const val PERMISSION_REQUEST = "permissions.dispatcher.PermissionRequest"

fun String.generateVersionNumberFrom() = substring(lastIndexOf(":") + 1).replace("\'", "").replace("\"", "")

fun PsiAnnotation.getValueAttribute() = parameterList.attributes.firstOrNull {  it.name == null || it.name == "value" }

fun PsiClass.isAnnotatedRuntimePermissions() =
        modifierList?.findAnnotation("permissions.dispatcher.RuntimePermissions") != null

fun PsiClass.getNeedsPermissionMethods() = methods.filter { it.isAnnotatedWithNeedsPermission() }

fun PsiClass.getOnShowRationaleMethods() = methods.filter { it.isAnnotatedWithOnShowRationale() }

fun PsiMethod.isAnnotatedWithNeedsPermission() = getNeedsPermissionAnnotation() != null

fun PsiMethod.isAnnotatedWithOnShowRationale() = getOnShowRationaleAnnotation() != null

fun PsiMethod.getAnnotationWith(annotation: String) = modifierList.findAnnotation(annotation)

fun PsiMethod.getNeedsPermissionAnnotation() = getAnnotationWith(NEEDS_PERMISSION)

fun PsiMethod.getOnShowRationaleAnnotation() = getAnnotationWith(ON_SHOW_RATIONALE)

fun PsiJavaFile.importClass(import: String) {
    importClass(project.createPsiClass(import) ?: return)
}

fun PsiJavaFile.importForOnRationale() {
    importClass(ON_SHOW_RATIONALE)
    importClass(PERMISSION_REQUEST)
}

fun Project.createPsiClass(qualifiedName: String): PsiClass? {
    val psiFacade = JavaPsiFacade.getInstance(this)
    val searchScope = GlobalSearchScope.allScope(this)
    return psiFacade.findClass(qualifiedName, searchScope)
}