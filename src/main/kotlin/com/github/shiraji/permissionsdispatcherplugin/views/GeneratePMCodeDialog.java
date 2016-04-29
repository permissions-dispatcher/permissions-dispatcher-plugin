package com.github.shiraji.permissionsdispatcherplugin.views;

import javax.swing.*;
import java.awt.event.*;

public class GeneratePMCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    public boolean isOk = false;

    public GeneratePMCodeDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
// add your code here
        isOk = true;
        setVisible(false);
    }

    private void onCancel() {
// add your code here if necessary
        setVisible(false);
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
