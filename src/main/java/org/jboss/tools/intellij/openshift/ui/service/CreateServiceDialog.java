/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jboss.tools.intellij.openshift.ui.widgets.JsonSchemaWidget;
import org.jboss.tools.intellij.openshift.utils.odo.OperatorCRD;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"java:S1171","java:S100"})
public class CreateServiceDialog extends DialogWrapper {
    private static final String PROPERTIES = "properties";
    private static final String SPEC = "spec";

    private JPanel contentPane;
    private JTextField nameField;
    private JComboBox serviceTemplatesComboBox;
    private JComboBox serviceCRDComboBox;
    private JTextField applicationTextField;
    private JsonSchemaWidget jsonSchemaWidget;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public CreateServiceDialog() {
        super(null, false, IdeModalityType.IDE);
        init();
        setTitle("Create service");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public void setServiceTemplates(ServiceTemplate[] serviceTemplates) {
        serviceTemplatesComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                value = ((ServiceTemplate) value).getDisplayName();
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        serviceTemplatesComboBox.addItemListener(item -> templateSelected((ServiceTemplate) item.getItem()));
        serviceTemplatesComboBox.setModel(new DefaultComboBoxModel(serviceTemplates));
        if (serviceTemplates.length > 0) {
            templateSelected(serviceTemplates[0]);
        }
    }

    private void templateSelected(ServiceTemplate template) {
        serviceCRDComboBox.setModel(new DefaultComboBoxModel(((ServiceTemplate) template).getCRDs().toArray()));
        serviceCRDComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                value = ((OperatorCRD) value).getKind();
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        serviceCRDComboBox.addItemListener(item -> templateCRDSelected((OperatorCRD) item.getItem()));
        serviceCRDComboBox.setSelectedIndex(-1);
        serviceCRDComboBox.setSelectedIndex(0);
        serviceCRDComboBox.setEnabled(((ServiceTemplate) template).getCRDs().size() >= 2);
    }

    private void templateCRDSelected(OperatorCRD crd) {
        if (crd.getSchema() != null && crd.getSchema().has(PROPERTIES) && crd.getSchema().get(PROPERTIES).has(SPEC)) {
            jsonSchemaWidget.init((ObjectNode) crd.getSchema().get(PROPERTIES).get(SPEC),
                    crd.getSample() != null && crd.getSample().has(SPEC)?crd.getSample().get(SPEC):null);
        }
    }

    public String getName() {
        return nameField.getText();
    }

    public ServiceTemplate getServiceTemplate() {
        return (ServiceTemplate) serviceTemplatesComboBox.getSelectedItem();
    }

    public OperatorCRD getServiceTemplateCRD() {
        return (OperatorCRD) serviceCRDComboBox.getSelectedItem();
    }

    public ObjectNode getSpec() {
        ObjectNode spec = MAPPER.createObjectNode();
        jsonSchemaWidget.dump(spec);
        return spec;
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();
        if (nameField.getText().length() == 0) {
            validations.add(new ValidationInfo("Name must be provided", nameField));
        }
        if (applicationTextField.getText().length() == 0) {
            validations.add(new ValidationInfo("Application must be provided", applicationTextField));
        }
        return validations;
    }

    public void setApplication(String application){
        applicationTextField.setText(application);
        applicationTextField.setEditable(false);
    }

    public String getApplication(){
        return applicationTextField.getText();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        jsonSchemaWidget = new JsonSchemaWidget();
    }
}
