package com.github.shiraji.permissionsdispatcherplugin.views;

import javax.swing.*;
import java.util.List;

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

    public JTextField needsPermissionTextField;
    public JCheckBox needsPermissionCheckBox;

    public JTextField onShowRationaleTextField;
    public JCheckBox onShowRationaleCheckBox;

    public JTextField onPermissionDeniedTextField;
    public JCheckBox onPermissionDeniedCheckBox;

    public JTextField onNeverAskAgainTextField;
    public JCheckBox onNeverAskAgainCheckBox;

    public boolean isOk = false;

    private GeneratePMCodeDialogDelegate generatePMCodeDialogDelegate;

    public GeneratePMCodeDialog() {
        generatePMCodeDialogDelegate = new GeneratePMCodeDialogDelegate(this);
        generatePMCodeDialogDelegate.initDialog();
    }

    public List<String> getSelectedPermissions() {
        return generatePMCodeDialogDelegate.getSelectedPermissions();
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
