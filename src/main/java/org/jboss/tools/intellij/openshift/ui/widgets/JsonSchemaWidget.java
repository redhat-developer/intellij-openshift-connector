/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.widgets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;

/**
 * A widget whose content is driven by JSON schema. The widget is initially created empty but then calling init function
 * will create the sub widgets according to the schema.
 *
 * {@link #init(ObjectNode, JsonNode)}
 * {@link #init(ObjectNode)}
 */
public class JsonSchemaWidget extends JPanel {
    private static final String PROPERTIES = "properties";
    private static final String REQUIRED = "required";
    private static final String TYPE = "type";
    private static final String ENUM = "enum";
    private static final String DESCRIPTION = "description";
    private static final String DISPLAY_NAME = "displayName";

    private static final String TYPE_PROPERTY = JsonSchemaWidget.class.getName() + ".type";
    private static final String NAME_PROPERTY = JsonSchemaWidget.class.getName() + ".name";
    private static final String REQUIRED_PROPERTY = JsonSchemaWidget.class.getName() + ".required";
    private static final String LAYOUT_CONSTRAINTS = "insets 0 0 0 0,hidemode 1";

    private static final Icon PLUS_ICON = new ImageIcon(JsonSchemaWidget.class.getResource(UIHelper.isDarkMode()?"/images/plus-solid-dark.png":"/images/plus-solid-light.png"));
    private static final Icon CHEVRON_DOWN_ICON = new ImageIcon(JsonSchemaWidget.class.getResource(UIHelper.isDarkMode()?"/images/chevron-down-solid-dark.png":"/images/chevron-down-solid-light.png"));

    public JsonSchemaWidget() {
        super();
        setLayout(new MigLayout(LAYOUT_CONSTRAINTS, "[grow]"));
    }

    private static boolean isRequired(String name, ArrayNode required) {
        if (required != null) {
            for(JsonNode el : required) {
                if (el.asText().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String normalize(String s) {
        StringBuilder builder = new StringBuilder();
        builder.append(Character.toUpperCase(s.charAt(0)));
        for(int i=1; i < s.length();++i) {
            if (Character.isUpperCase(s.charAt(i))) {
                builder.append(' ');
            }
            builder.append(s.charAt(i));
        }
        return builder.toString();
    }

    private static String getDisplayName(String name, JsonNode node) {
        return node.has(DISPLAY_NAME)?node.get(DISPLAY_NAME).asText():normalize(name);
    }

    private JLabel createNameLabel(String name, JsonNode node) {
        JLabel label = new JLabel(getDisplayName(name, node));
        if (node.has(DESCRIPTION)) {
            label.setToolTipText(node.get(DESCRIPTION).asText());
        }
        return label;
    }

    private String getConstraints() {
        return "wrap 1px,grow";
    }

    private void createArrayItemWidget(String name, JsonNode node, JsonNode def, JPanel panel) {
        JPanel itemPanel = new JPanel(new MigLayout(LAYOUT_CONSTRAINTS));
        JButton removeButton = new JButton("Remove " + getDisplayName(name, node));
        removeButton.addActionListener(e1 -> {
            panel.remove(itemPanel);
            panel.revalidate();
        });
        itemPanel.add(removeButton, getConstraints());
        panel.add(itemPanel, getConstraints());
        ArrayNode required = node.has(REQUIRED) && node.get(REQUIRED).isArray()?node.withArray(REQUIRED):null;
        createWidget(null, node.get("items"), def, isRequired(name, required), itemPanel);
        panel.revalidate();
    }

    private JPanel getHeaderPanel(String name, JComponent sub) {
        JLabel label;
        JPanel header = new JPanel(new MigLayout(LAYOUT_CONSTRAINTS, "[][grow]","[fill]"));
        label = new JLabel(name);
        header.add(label, "grow");
        JLabel sign = new JLabel(PLUS_ICON);
        sign.setToolTipText("Click to expand");
        sign.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (sign.getIcon() == PLUS_ICON) {
                    sign.setIcon(CHEVRON_DOWN_ICON);
                    sign.setToolTipText("Click to collapse");
                    sub.setVisible(true);
                } else {
                    sign.setIcon(PLUS_ICON);
                    sign.setToolTipText("Click to expand");
                    sub.setVisible(false);
                }
            }
        });
        header.add(sign, "wrap 1px,alignx right");
        return header;
    }

    private void setMetadata(JComponent comp, String name, String type, boolean required) {
        comp.putClientProperty(TYPE_PROPERTY, type);
        if (name != null) {
            comp.putClientProperty(NAME_PROPERTY, name);
        }
        comp.putClientProperty(REQUIRED_PROPERTY, required);
    }

    private void createWidget(String name, JsonNode node, JsonNode def, boolean required, Container parent) {
        switch (node.get(TYPE).asText()) {
            case "string":
            case "integer":
            case "number":
                if (name != null) {
                    parent.add(createNameLabel(name, node), getConstraints());
                }
                JComponent field;
                if (node.has(ENUM) && node.get(ENUM).isArray()) {
                    field = new JComboBox<String>();
                    for(JsonNode child : node.get(ENUM)) {
                        ((JComboBox)field).addItem(child.asText());
                    }
                    if (def != null && def.has(name)) {
                        ((JComboBox)field).setSelectedItem(def.get(name).asText());
                    }
                    else {
                        ((JComboBox)field).setSelectedItem(null);
                    }
                    if (def != null) {
                        if (name != null && def.has(name)) {
                            ((JComboBox)field).setSelectedItem(def.get(name).asText());
                        } else if (name == null) {
                            ((JComboBox)field).setSelectedItem(def.asText());
                        }
                    }
                } else {
                    field = new JTextField();
                    if (def != null) {
                        if (name != null && def.has(name)) {
                            ((JTextField)field).setText(def.get(name).asText());
                        } else if (name == null) {
                            ((JTextField)field).setText(def.asText());
                        }
                    }
                }
                parent.add(field, getConstraints());
                setMetadata(field, name, node.get(TYPE).asText(), required);
                break;
            case "boolean":
                if (name != null) {
                    parent.add(createNameLabel(name, node), getConstraints());
                }
                JCheckBox checkbox = new JCheckBox();
                if (def != null && def.has(name)) {
                    checkbox.setSelected(def.get(name).asBoolean());
                }
                parent.add(checkbox, getConstraints());
                setMetadata(checkbox, name, node.get(TYPE).asText(), required);
                break;
            case "object":
                JsonSchemaWidget sub = new JsonSchemaWidget();
                sub.init((ObjectNode) node, def!=null && name!=null?def.get(name):def);
                if (name != null) {
                    JPanel header = getHeaderPanel(getDisplayName(name, node), sub);
                    parent.add(header, getConstraints());
                    sub.setVisible(false);
                }
                parent.add(sub, getConstraints());
                setMetadata(sub, name, node.get(TYPE).asText(), required);
                break;
            case "array":
                if (node.has("items")) {
                    JPanel panel = new JPanel(new MigLayout(LAYOUT_CONSTRAINTS));
                    JButton button = new JButton("Add " + getDisplayName(name, node));
                    panel.add(button, getConstraints());
                    button.addActionListener(e -> createArrayItemWidget(name, node, null, panel));
                    if (name != null) {
                        JPanel header = getHeaderPanel(getDisplayName(name, node), panel);
                        parent.add(header, getConstraints());
                        panel.setVisible(false);
                    }
                    parent.add(panel, getConstraints());
                    setMetadata(panel, name, node.get(TYPE).asText(), required);
                    if (def != null && def.has(name) && def.get(name).isArray()) {
                        for(JsonNode item : def.withArray(name)) {
                            createArrayItemWidget(name, node, item, panel);
                        }
                    }
                }
        }
    }

    /**
     * Reset the content for this widget from the schema and default values.
     *
     * @param schema the object representing the JSON schema
     * @param def the object with default values
     */
    public void init(ObjectNode schema, JsonNode def) {
        removeAll();
        if (schema.has(PROPERTIES) && schema.get(PROPERTIES).isObject()) {
            ObjectNode properties = (ObjectNode) schema.get(PROPERTIES);
            ArrayNode required = schema.has(REQUIRED) && schema.get(REQUIRED).isArray()?schema.withArray(REQUIRED):null;
            for (Iterator<Map.Entry<String, JsonNode>> it = properties.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                String name = entry.getKey();
                JsonNode node = entry.getValue();
                if (node.isObject() && node.has(TYPE)) {
                    createWidget(name, node, def, isRequired(name, required), this);
                }
            }
        }
        revalidate();
    }

    /**
     * Reset the content for this widget from the schema.
     *
     * @param schema the object representing the JSON schema
     */
    public void init(ObjectNode schema) {
        init(schema, null);
    }

    private void dump(Container container, JsonNode node) {
        for(Component comp : container.getComponents()) {
            if (comp instanceof JComponent && ((JComponent) comp).getClientProperty(TYPE_PROPERTY) != null) {
                String name = (String) ((JComponent) comp).getClientProperty(NAME_PROPERTY);
                String type = (String) ((JComponent) comp).getClientProperty(TYPE_PROPERTY);
                if ("array".equals(type)) {
                    ArrayNode array = ((ObjectNode) node).arrayNode();
                    dump((Container) comp, array);
                    if (!array.isEmpty()) {
                        if (name == null) {
                            ((ArrayNode)node).add(array);
                        } else {
                            ((ObjectNode) node).set(name, array);
                        }
                    }
                } else if ("object".equals(type)) {
                    ObjectNode sub = ((ContainerNode)node).objectNode();
                    dump((Container) comp, sub);
                    if (!sub.isEmpty()) {
                        if (name == null) {
                            ((ArrayNode)node).add(sub);
                        } else {
                            ((ObjectNode) node).set(name, sub);
                        }
                    }
                } else if ("boolean".equals(type)) {
                    boolean val = ((JCheckBox) comp).isSelected();
                    if (val || (boolean)((JCheckBox) comp).getClientProperty(REQUIRED_PROPERTY)) {
                        if (name == null) {
                            ((ArrayNode)node).add(val);
                        } else {
                            ((ObjectNode) node).put(name, val);
                        }
                    }
                } else {
                    String val = (comp instanceof JTextField)?((JTextField) comp).getText(): (String) ((JComboBox)comp).getSelectedItem();
                    if (val != null && !val.isEmpty()) {
                        if (name == null) {
                            ((ArrayNode)node).add(val);
                        } else if ("string".equals(type)) {
                            ((ObjectNode) node).put(name, val);
                        } else if ("integer".equals(type)) {
                            ((ObjectNode)node).put(name, Integer.parseInt(val));
                        } else {
                            ((ObjectNode)node).put(name, Double.parseDouble(val));
                        }
                    }
                }
            } else {
                dump((Container) comp, node);
            }
        }
    }

    /**
     * Dump the content of the widget in a JSON object.
     *
     * @param node the JSON object to dump into
     */
    public void dump(JsonNode node) {
        dump(this, node);
    }
}
