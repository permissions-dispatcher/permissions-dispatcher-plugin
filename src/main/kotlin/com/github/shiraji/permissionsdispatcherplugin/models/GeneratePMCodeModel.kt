package com.github.shiraji.permissionsdispatcherplugin.models

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope

class GeneratePMCodeModel(project: Project) {

    private val activityPsiClass: PsiClass? = createPsiClass("android.app.Activity", project)
    private val fragmentPsiClass: PsiClass? = createPsiClass("android.app.Fragment", project)
    private val supportFragmentPsiClass: PsiClass? = createPsiClass("android.support.v4.app.Fragment", project)

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

    private fun createPsiClass(qualifiedName: String, project: Project): PsiClass? {
        val psiFacade = JavaPsiFacade.getInstance(project);
        val searchScope = GlobalSearchScope.allScope(project);
        return psiFacade.findClass(qualifiedName, searchScope);
    }
}