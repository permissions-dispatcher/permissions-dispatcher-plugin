package com.github.shiraji.permissionsdispatcherplugin.views

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JCheckBox
import javax.swing.JOptionPane

class GeneratePMCodeDialogDelegate(val dialog: GeneratePMCodeDialog) {

    val dangerPermissionsCheckbox = listOf<JCheckBox>(
            dialog.readCalendar, dialog.writeCalendar, dialog.camera, dialog.readContacts,
            dialog.writeContacts, dialog.getAccounts, dialog.accessFineLocation,
            dialog.accessCoarseLocation, dialog.recordAudio, dialog.readPhoneState,
            dialog.callPhone, dialog.readCallLog, dialog.writeCallLog, dialog.addVoicemail,
            dialog.useSip, dialog.processOutgoingCall, dialog.bodySensors, dialog.sendSms,
            dialog.receiveSms, dialog.readSms, dialog.receiveWapPush, dialog.receiveMms,
            dialog.readExternalStorage, dialog.writeExternalStorage)

    val specialPermissionsCheckbox = listOf<JCheckBox>(dialog.systemAlertWindow, dialog.writeSettings)

    val methodNameUI = mapOf(
            dialog.needsPermissionTextField to dialog.needsPermissionCheckBox,
            dialog.onShowRationaleTextField to dialog.onShowRationaleCheckBox,
            dialog.onPermissionDeniedTextField to dialog.onPermissionDeniedCheckBox,
            dialog.onNeverAskAgainTextField to dialog.onNeverAskAgainCheckBox)

    fun initDialog() {
        dialog.apply {
            setTitle("PermissionsDispatcher Plugin")
            addOkAction().setText("Generate")
            setOkOperation({
                if (isValidInfo()) {
                    dialogWrapper.close(DialogWrapper.OK_EXIT_CODE, false)
                }
            })
            addCancelAction()
        }

        dangerPermissionsCheckbox.forEach {
            it.addChangeListener {
                val checkBox: JCheckBox = it.source as JCheckBox
                if (checkBox.isSelected) {
                    specialPermissionsCheckbox.forEach {
                        it.isSelected = false
                    }
                }
            }
        }

        specialPermissionsCheckbox.forEach {
            it.addChangeListener {
                val checkBox = it.source as JCheckBox
                if (checkBox.isSelected) {
                    dangerPermissionsCheckbox.forEach {
                        it.isSelected = false
                    }

                    specialPermissionsCheckbox.filterNot {
                        it.equals(checkBox)
                    }.forEach {
                        it.isSelected = false
                    }
                }
            }
        }

        methodNameUI.forEach {
            val jCheckBox = it.value
            val jTextField = it.key
            jCheckBox.addChangeListener {
                val checkBox = it.source as JCheckBox
                jTextField.isEditable = checkBox.isSelected
                jTextField.isEnabled = checkBox.isSelected
            }
        }
    }

    private fun isValidInfo(): Boolean {
        if (dangerPermissionsCheckbox.none { it.isSelected } && specialPermissionsCheckbox.none { it.isSelected } ) {
            JOptionPane.showMessageDialog(null, "Must select at least one permission.")
            return false
        }
        val methodNames = methodNameUI.filter { it.value.isSelected && it.key.text.length <= 0 }
        if (methodNames.isNotEmpty()) {
            JOptionPane.showMessageDialog(null, "Must provide method name for ${methodNames.map { it.value.text }.joinToString { it }}")
            return false
        }
        return true
    }

    fun getSelectedPermissions(): List<String> {
        return setOf(*dangerPermissionsCheckbox.toTypedArray(), *specialPermissionsCheckbox.toTypedArray()).filter {
            it.isSelected
        }.map {
            it.text
        }
    }
}