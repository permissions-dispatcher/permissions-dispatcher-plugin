package com.github.shiraji.permissionsdispatcherplugin.views;

import com.github.shiraji.permissionsdispatcherplugin.config.GeneratePMCodeConfig;
import com.github.shiraji.permissionsdispatcherplugin.data.RebuildType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GeneratePMCodeConfiguration implements Configurable {
    private JPanel root;
    private JComboBox rebuildCombobox;

    public GeneratePMCodeConfiguration() {
        super();

        rebuildCombobox.setModel(new DefaultComboBoxModel(RebuildType.values()));
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "PermissionsDispatcher plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return root;
    }

    @Override
    public boolean isModified() {
        RebuildType type = (RebuildType) rebuildCombobox.getSelectedItem();
        return GeneratePMCodeConfig.getRebuildTypeId() != type.getId();
    }

    @Override
    public void apply() throws ConfigurationException {
        RebuildType type = (RebuildType) rebuildCombobox.getSelectedItem();
        GeneratePMCodeConfig.setRebuildTypeId(type.getId());
    }

    @Override
    public void reset() {
        rebuildCombobox.setSelectedItem(RebuildType.fromId(GeneratePMCodeConfig.getRebuildTypeId()));
    }

    @Override
    public void disposeUIResources() {
    }
}
