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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.*;
import com.redhat.devtools.intellij.common.utils.MessagesHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class JsonSchemaWidgetTest extends BasePlatformTestCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void testSimpleTextField() throws JsonProcessingException {
        String schema = "{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"name\": {\n" +
                "            \"type\": \"string\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectNode schemaNode = MAPPER.readValue(schema, ObjectNode.class);
        JsonSchemaWidget widget = new JsonSchemaWidget();
        widget.init(schemaNode);
        assertNotNull(widget.getComponents());
        assertEquals(2, widget.getComponents().length);
        assertTrue(widget.getComponents()[0] instanceof JLabel);
        assertEquals("Name", ((JLabel)widget.getComponents()[0]).getText());
        assertTrue(widget.getComponents()[1] instanceof JTextField);
        assertEquals("", ((JTextField)widget.getComponents()[1]).getText());
    }

    public void testSimpleTextFieldWithDefault() throws JsonProcessingException {
        String schema = "{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"name\": {\n" +
                "            \"type\": \"string\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectNode schemaNode = MAPPER.readValue(schema, ObjectNode.class);
        String def = "{\n" +
                "    \"name\": \"the name\"\n" +
                "}";
        ObjectNode defNode = MAPPER.readValue(def, ObjectNode.class);
        JsonSchemaWidget widget = new JsonSchemaWidget();
        widget.init(schemaNode, defNode);
        assertNotNull(widget.getComponents());
        assertEquals(2, widget.getComponents().length);
        assertTrue(widget.getComponents()[0] instanceof JLabel);
        assertEquals("Name", ((JLabel)widget.getComponents()[0]).getText());
        assertTrue(widget.getComponents()[1] instanceof JTextField);
        assertEquals("the name", ((JTextField)widget.getComponents()[1]).getText());
    }

    public void testBooleanField() throws JsonProcessingException {
        String schema = "{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"name\": {\n" +
                "            \"type\": \"boolean\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectNode schemaNode = MAPPER.readValue(schema, ObjectNode.class);
        JsonSchemaWidget widget = new JsonSchemaWidget();
        widget.init(schemaNode);
        assertNotNull(widget.getComponents());
        assertEquals(2, widget.getComponents().length);
        assertTrue(widget.getComponents()[0] instanceof JLabel);
        assertEquals("Name", ((JLabel)widget.getComponents()[0]).getText());
        assertTrue(widget.getComponents()[1] instanceof JCheckBox);
        assertFalse(((JCheckBox)widget.getComponents()[1]).isSelected());
    }

    public void testBooleanWithDefault() throws JsonProcessingException {
        String schema = "{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"name\": {\n" +
                "            \"type\": \"boolean\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectNode schemaNode = MAPPER.readValue(schema, ObjectNode.class);
        String def = "{\n" +
                "    \"name\": true\n" +
                "}";
        ObjectNode defNode = MAPPER.readValue(def, ObjectNode.class);
        JsonSchemaWidget widget = new JsonSchemaWidget();
        widget.init(schemaNode, defNode);
        assertNotNull(widget.getComponents());
        assertEquals(2, widget.getComponents().length);
        assertTrue(widget.getComponents()[0] instanceof JLabel);
        assertEquals("Name", ((JLabel)widget.getComponents()[0]).getText());
        assertTrue(widget.getComponents()[1] instanceof JCheckBox);
        assertTrue(((JCheckBox)widget.getComponents()[1]).isSelected());
    }

    public void testObjectField() throws JsonProcessingException {
        String schema = "{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"sub\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "                \"name\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectNode schemaNode = MAPPER.readValue(schema, ObjectNode.class);
        JsonSchemaWidget widget = new JsonSchemaWidget();
        widget.init(schemaNode);
        assertNotNull(widget.getComponents());
        assertEquals(2, widget.getComponents().length);
        assertTrue(widget.getComponents()[0] instanceof JPanel);
        JPanel panel = (JPanel) widget.getComponents()[0];
        assertTrue(panel.getComponents()[0] instanceof JLabel);
        assertEquals("Sub", ((JLabel)panel.getComponents()[0]).getText());
        assertTrue(panel.getComponents()[1] instanceof JLabel);
        assertTrue(widget.getComponents()[1] instanceof JsonSchemaWidget);
        JsonSchemaWidget widget1 = (JsonSchemaWidget) widget.getComponents()[1];
        assertEquals(2, widget1.getComponents().length);
        assertTrue(widget1.getComponents()[0] instanceof JLabel);
        assertEquals("Name", ((JLabel)widget1.getComponents()[0]).getText());
        assertTrue(widget1.getComponents()[1] instanceof JTextField);
        assertEquals("", ((JTextField)widget1.getComponents()[1]).getText());
    }

    public void testObjectWithDefault() throws JsonProcessingException {
        String schema = "{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"sub\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "                \"name\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectNode schemaNode = MAPPER.readValue(schema, ObjectNode.class);
        String def = "{\n" +
                "    \"sub\": {\n" +
                "        \"name\": \"the name\"\n" +
                "    }\n" +
                "}";
        ObjectNode defNode = MAPPER.readValue(def, ObjectNode.class);
        JsonSchemaWidget widget = new JsonSchemaWidget();
        widget.init(schemaNode, defNode);
        assertNotNull(widget.getComponents());
        assertEquals(2, widget.getComponents().length);
        assertTrue(widget.getComponents()[0] instanceof JPanel);
        JPanel panel = (JPanel) widget.getComponents()[0];
        assertTrue(panel.getComponents()[0] instanceof JLabel);
        assertEquals("Sub", ((JLabel)panel.getComponents()[0]).getText());
        assertTrue(panel.getComponents()[1] instanceof JLabel);
        assertTrue(widget.getComponents()[1] instanceof JsonSchemaWidget);
        JsonSchemaWidget widget1 = (JsonSchemaWidget) widget.getComponents()[1];
        assertEquals(2, widget1.getComponents().length);
        assertTrue(widget1.getComponents()[0] instanceof JLabel);
        assertEquals("Name", ((JLabel)widget1.getComponents()[0]).getText());
        assertTrue(widget1.getComponents()[1] instanceof JTextField);
        assertEquals("the name", ((JTextField)widget1.getComponents()[1]).getText());
    }

    public void testArrayField() throws JsonProcessingException {
        String schema = "{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"names\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": {\n" +
                "                        \"type\": \"string\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectNode schemaNode = MAPPER.readValue(schema, ObjectNode.class);
        JsonSchemaWidget widget = new JsonSchemaWidget();
        widget.init(schemaNode);
        assertNotNull(widget.getComponents());
        assertEquals(2, widget.getComponents().length);
        assertTrue(widget.getComponents()[0] instanceof JPanel);
        JPanel panel = (JPanel) widget.getComponents()[0];
        assertTrue(panel.getComponents()[0] instanceof JLabel);
        assertEquals("Names", ((JLabel)panel.getComponents()[0]).getText());
        assertTrue(panel.getComponents()[1] instanceof JLabel);
        assertTrue(widget.getComponents()[1] instanceof JPanel);
        JPanel panel1 = (JPanel) widget.getComponents()[1];
        assertEquals(1, panel1.getComponents().length);
        assertTrue(panel1.getComponents()[0] instanceof JButton);
        assertEquals("Add Names", ((JButton)panel1.getComponents()[0]).getText());
    }

    public void testArrayFieldWithDefault() throws JsonProcessingException {
        String schema = "{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"names\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": {\n" +
                "                        \"type\": \"string\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ObjectNode schemaNode = MAPPER.readValue(schema, ObjectNode.class);
        String def = "{\n" +
                "    \"names\": [\n" +
                "        \"home\"\n" +
                "    ]\n" +
                "}";
        ObjectNode defNode = MAPPER.readValue(def, ObjectNode.class);
        JsonSchemaWidget widget = new JsonSchemaWidget();
        widget.init(schemaNode, defNode);
        assertNotNull(widget.getComponents());
        assertEquals(2, widget.getComponents().length);
        assertTrue(widget.getComponents()[0] instanceof JPanel);
        JPanel panel = (JPanel) widget.getComponents()[0];
        assertTrue(panel.getComponents()[0] instanceof JLabel);
        assertEquals("Names", ((JLabel)panel.getComponents()[0]).getText());
        assertTrue(panel.getComponents()[1] instanceof JLabel);
        assertTrue(widget.getComponents()[1] instanceof JPanel);
        JPanel panel1 = (JPanel) widget.getComponents()[1];
        assertEquals(2, panel1.getComponents().length);
        assertTrue(panel1.getComponents()[0] instanceof JButton);
        assertEquals("Add Names", ((JButton)panel1.getComponents()[0]).getText());
        assertTrue(panel1.getComponents()[1] instanceof JPanel);
        JPanel panel2 = (JPanel) panel1.getComponents()[1];
        assertTrue(panel2.getComponents()[0] instanceof JButton);
        assertEquals("Remove Names", ((JButton)panel2.getComponents()[0]).getText());
        assertTrue(panel2.getComponents()[1] instanceof JTextField);
        assertEquals("home", ((JTextField)panel2.getComponents()[1]).getText());
    }
}
