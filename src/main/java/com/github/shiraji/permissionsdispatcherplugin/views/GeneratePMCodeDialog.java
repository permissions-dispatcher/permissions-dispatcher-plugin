package com.github.shiraji.permissionsdispatcherplugin.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class GeneratePMCodeDialog extends DialogBuilder {
    JPanel contentPane;
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

    private GeneratePMCodeDialogDelegate generatePMCodeDialogDelegate;

    public GeneratePMCodeDialog(@Nullable Project project) {
        super(project);
        generatePMCodeDialogDelegate = new GeneratePMCodeDialogDelegate(this);
        generatePMCodeDialogDelegate.initDialog();
        setCenterPanel(contentPane);
    }

    public List<String> getSelectedPermissions() {
        return generatePMCodeDialogDelegate.getSelectedPermissions();
    }

}
