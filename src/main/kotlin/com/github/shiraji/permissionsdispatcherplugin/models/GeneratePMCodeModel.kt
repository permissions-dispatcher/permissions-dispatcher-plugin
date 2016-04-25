package com.github.shiraji.permissionsdispatcherplugin.models

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope

class GeneratePMCodeModel {

    fun isActivity(aClass: PsiClass, project: Project): Boolean {
        val activity = createPsiClass("android.app.Activity", project) ?: return false
        return aClass.isInheritor(activity, true)
    }

    fun isFragment(aClass: PsiClass, project: Project): Boolean {
        val fragment = createPsiClass("android.app.Fragment", project) ?: return false
        return aClass.isInheritor(fragment, true)
    }

    fun isSupportFragment(aClass: PsiClass, project: Project): Boolean {
        val fragment = createPsiClass("android.support.v4.app.Fragment", project) ?: return false
        return aClass.isInheritor(fragment, true)
    }

    fun createPsiClass(qualifiedName: String, project: Project): PsiClass? {
        val psiFacade = JavaPsiFacade.getInstance(project);
        val searchScope = GlobalSearchScope.allScope(project);
        return psiFacade.findClass(qualifiedName, searchScope);
    }
}