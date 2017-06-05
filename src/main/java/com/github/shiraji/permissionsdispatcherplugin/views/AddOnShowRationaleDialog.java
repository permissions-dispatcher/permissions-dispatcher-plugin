package com.github.shiraji.permissionsdispatcherplugin.views;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.psi.PsiAnnotation;

import javax.swing.*;
import java.util.List;

public class AddOnShowRationaleDialog extends DialogBuilder {
    public JComboBox annotationComboBox;
    public JTextField methodNameTextField;
    public JPanel rootPanel;

    public AddOnShowRationaleDialog(List<PsiAnnotation> psiAnnotations) {
        AddOnShowRationaleDialogDelegate delegate = new AddOnShowRationaleDialogDelegate(this);
        delegate.initDialog(psiAnnotations);
        setCenterPanel(rootPanel);
    }
}
