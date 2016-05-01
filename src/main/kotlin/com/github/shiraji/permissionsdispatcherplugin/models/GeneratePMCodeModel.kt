package com.github.shiraji.permissionsdispatcherplugin.models

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope

class GeneratePMCodeModel(val project: Project) {

    private val activityPsiClass: PsiClass? = createPsiClass("android.app.Activity")
    private val fragmentPsiClass: PsiClass? = createPsiClass("android.app.Fragment")
    private val supportFragmentPsiClass: PsiClass? = createPsiClass("android.support.v4.app.Fragment")

    var permissions: List<String> = emptyList()

    var needsPermissionMethodName: String = ""
    var onShowRationaleMethodName: String = ""
    var onPermissionDeniedMethodName: String = ""
    var onNeverAskAgainMethodName: String = ""

    fun isActivity(aClass: PsiClass): Boolean {
        activityPsiClass ?: return false
        return aClass.isInheritor(activityPsiClass, true)
    }

    fun isFragment(aClass: PsiClass): Boolean {
        fragmentPsiClass ?: return false
        return aClass.isInheritor(fragmentPsiClass, true)
    }

    fun isSupportFragment(aClass: PsiClass): Boolean {
        supportFragmentPsiClass ?: return false
        return aClass.isInheritor(supportFragmentPsiClass, true)
    }

    fun isActivityOrFragment(aClass: PsiClass) = isActivity(aClass) || isFragment(aClass) || isSupportFragment(aClass)

    fun createPsiClass(qualifiedName: String): PsiClass? {
        val psiFacade = JavaPsiFacade.getInstance(project);
        val searchScope = GlobalSearchScope.allScope(project);
        return psiFacade.findClass(qualifiedName, searchScope);
    }

    fun toPermissionParameter(): String {
        if (permissions.size == 1) {
            return permissions[0]
        } else {
            return "Manifest.permission.CAMERA"
        }
    }
}