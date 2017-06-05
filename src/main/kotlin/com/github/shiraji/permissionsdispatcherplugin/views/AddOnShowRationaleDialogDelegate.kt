package com.github.shiraji.permissionsdispatcherplugin.views

import com.github.shiraji.permissionsdispatcherplugin.extentions.getValueAttribute
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiAnnotation

class AddOnShowRationaleDialogDelegate(val dialog: AddOnShowRationaleDialog) {
    fun initDialog(psiAnnotations: MutableList<PsiAnnotation>) {
        dialog.apply {
            setTitle("PermissionsDispatcher Plugin")
            addOkAction().setText("Generate")
            setOkOperation({
                if (!isInvalidData()) dialogWrapper.close(DialogWrapper.OK_EXIT_CODE, false)
            })
            addCancelAction()
        }

        psiAnnotations.forEach {
            val value = it.getValueAttribute() ?: return@forEach
            dialog.annotationComboBox.addItem(value.text.removeSurrounding(prefix = "{", suffix = "}"))
        }
    }

    private fun isInvalidData(): Boolean {
        return dialog.annotationComboBox.selectedIndex < 0
                || dialog.methodNameTextField.text.isNullOrBlank()
    }
}