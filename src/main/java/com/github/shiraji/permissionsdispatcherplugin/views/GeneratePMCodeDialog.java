package com.github.shiraji.permissionsdispatcherplugin.views;

import javax.swing.*;

public class GeneratePMCodeDialog extends JDialog {
    JPanel contentPane;
    JButton buttonOK;
    JButton buttonCancel;
    JCheckBox readCalendar;
    JCheckBox writeCalendar;
    JCheckBox camera;
    JCheckBox readContacts;
    JCheckBox writeContacts;
    JCheckBox getAccounts;
    JCheckBox accessFineLocation;
    JCheckBox accessCoarseLocation;
    JCheckBox recordAudio;
    JCheckBox readPhoneState;
    JCheckBox callPhone;
    JCheckBox readCallLog;
    JCheckBox writeCallLog;
    JCheckBox addVoicemail;
    JCheckBox useSip;
    JCheckBox processOutgoingCall;
    JCheckBox bodySensors;
    JCheckBox sendSms;
    JCheckBox receiveSms;
    JCheckBox readSms;
    JCheckBox receiveWapPush;
    JCheckBox receiveMms;
    JCheckBox readExternalStorage;
    JCheckBox writeExternalStorage;
    JCheckBox systemAlertWindow;
    JCheckBox writeSettings;
    JTextField needsPermissionTextField;
    JCheckBox onShowRationaleCheckBox;
    JTextField onShowRationaleTextField;
    JCheckBox onPermissionDeniedCheckBox;
    JCheckBox onNeverAskAgainCheckBox;
    JTextField onPermissionDeniedTextField;
    JTextField onNeverAskAgainTextField;
    JCheckBox needsPermissionCheckBox;

    public boolean isOk = false;

    public GeneratePMCodeDialog() {
        new GeneratePMCodeDialogDelegate(this).initDialog();
    }

    public static void main(String[] args) {
        GeneratePMCodeDialog dialog = new GeneratePMCodeDialog();
        dialog.pack();
        dialog.setVisible(true);
        if (dialog.isOk) {
            System.out.println("ok!");
        }
        System.exit(0);
    }
}
