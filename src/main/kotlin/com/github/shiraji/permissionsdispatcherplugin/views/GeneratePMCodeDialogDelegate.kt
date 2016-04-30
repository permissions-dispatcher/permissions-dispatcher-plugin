package com.github.shiraji.permissionsdispatcherplugin.views

import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.WindowConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class GeneratePMCodeDialogDelegate(val dialog: GeneratePMCodeDialog) {

    val dengarPermissionsCheckbox = listOf<JCheckBox>(
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
            setContentPane(contentPane)
            isModal = true
            defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
            getRootPane().defaultButton = buttonOK
            buttonOK.addActionListener({
                isOk = true
                isVisible = false
            })
            buttonCancel.addActionListener({ onCancel() })
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    onCancel()
                }
            })

            // call onCancel() on ESCAPE
            contentPane.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        }

        dengarPermissionsCheckbox.forEach {
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
                    dengarPermissionsCheckbox.forEach {
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
            jTextField.document.addDocumentListener(object : DocumentListener {
                // TODO This does not help if a user leaves multiple method names blank
                override fun changedUpdate(e: DocumentEvent?) {
                    validateMethodName()
                }

                override fun insertUpdate(e: DocumentEvent?) {
                    validateMethodName()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    validateMethodName()
                }

                private fun validateMethodName() {
                    if (!jCheckBox.isSelected) {
                        return
                    }

                    if (jTextField.text.length <= 0) {
                        dialog.buttonOK.isEnabled = false
                    } else {
                        dialog.buttonOK.isEnabled = true
                    }
                }
            })

            jCheckBox.addChangeListener {
                val checkBox = it.source as JCheckBox
                jTextField.isEditable = checkBox.isSelected
                jTextField.isEnabled = checkBox.isSelected
            }
        }
    }

    private fun onCancel() {
        dialog.isVisible = false
    }
}