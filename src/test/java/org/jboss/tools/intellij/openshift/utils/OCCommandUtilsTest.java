/*************************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for OCCommandUtils for testing correct parsing of oc login command
 * @author Josef Kopriva
 */
public class OCCommandUtilsTest {
    @Test
    public void testCommandWithToken() {
        assertEquals("123456789123456789",
                OCCommandUtils.getToken("oc login https://api.engint.openshift.com --token=123456789123456789"));
        assertEquals("123456789123456789",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=123456789123456789   "));
        assertEquals("1234567891234567..89",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=1234567891234567..89   "));
        assertEquals("12345678912345678_9",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=12345678912345678_9   "));
        assertEquals("1234567891234567.89",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=1234567891234567.89   "));
        assertEquals("1234567891234567-89",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=1234567891234567-89   "));
    }

    @Test
    public void testCommandWithTokenOCP4() {
        assertEquals("sha256~123456789123456789",
                OCCommandUtils.getToken("oc login https://api.engint.openshift.com --token=sha256~123456789123456789"));
        assertEquals("sha256~123456789123456789",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=sha256~123456789123456789   "));
        assertEquals("sha256~1234567891234567..89",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=sha256~1234567891234567..89   "));
        assertEquals("sha256~12345678912345678_9",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=sha256~12345678912345678_9   "));
        assertEquals("sha256~1234567891234567.89",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=sha256~1234567891234567.89   "));
        assertEquals("sha256~1234567891234567-89",
                OCCommandUtils.getToken("oc login https://api.-uengint.openshift.com --token=sha256~1234567891234567-89   "));
    }

    @Test
    public void testCommandWithPassword() {
        assertEquals("developerPassword", OCCommandUtils.getPassword("oc login -u developer -p developerPassword"));
        assertEquals("developerPassword", OCCommandUtils.getPassword("oc login -u developer --password developerPassword"));
        assertEquals("developerPassword", OCCommandUtils.getPassword("oc login -u developer --password=developerPassword"));
        assertNull(OCCommandUtils.getPassword("oc login -u developer "));
        assertNull(OCCommandUtils.getPassword("oc login -u developer --passwod bad"));
        assertNull(OCCommandUtils.getPassword("oc login -u developer --passwod=bad"));
    }

    @Test
    public void testValidCommand() {
        assertFalse(
                OCCommandUtils.isValidCommand("oc loginhttps://api.engint.openshift.com --token=123456789123456789"));
        assertTrue(
                OCCommandUtils.isValidCommand("oc login https://api.engint.openshift.com --token=123456789123456789"));
        assertTrue(OCCommandUtils.isValidCommand("oc login https://12.34.5.6:8443 -u developer -p deve"));
    }

    @Test
    public void testServerAddress() {
        assertEquals("https://api.engint.openshift.com",
                OCCommandUtils.getServer("oc login https://api.engint.openshift.com --token=123456789123456789"));
        assertEquals("https://api.engint.openshift.com",
                OCCommandUtils.getServer("oc login -s=https://api.engint.openshift.com --token=123456789123456789"));
        assertEquals("https://api.engint.openshift.com",
                OCCommandUtils.getServer("oc login -s https://api.engint.openshift.com --token=123456789123456789"));
        assertEquals("https://api.engint.openshift.com",
                OCCommandUtils.getServer("oc login --server=https://api.engint.openshift.com --token=123456789123456789"));
        assertEquals("https://api.engint.openshift.com",
                OCCommandUtils.getServer("oc login --server https://api.engint.openshift.com --token=123456789123456789"));
    }

    @Test
    public void testUsername() {
        assertEquals("developer", OCCommandUtils.getUsername("oc login -u developer -p developerPassword"));
        assertEquals("developer", OCCommandUtils.getUsername("oc login -u=developer -p developerPassword"));
        assertEquals("developer", OCCommandUtils.getUsername("oc login --username developer -p developerPassword"));
        assertEquals("developer", OCCommandUtils.getUsername("oc login --username=developer -p developerPassword"));
    }

    @Test
    public void testCommandValidOnlyUsername() {
        String command = "oc login -u system:admin -n default https://api.starter-us-west-1.openshift.com";
        assertTrue(OCCommandUtils.isValidCommand(command));
        assertEquals("system:admin", OCCommandUtils.getUsername(command));
        assertEquals("https://api.starter-us-west-1.openshift.com",OCCommandUtils.getServer(command));
    }

    @Test
    public void testCommandValidDifferentOrder() {
        String command = "oc login -u system:admin -n default https://api.starter-us-west-1.openshift.com -p 123456";
        assertTrue(OCCommandUtils.isValidCommand(command));
        assertEquals("system:admin", OCCommandUtils.getUsername(command));
        assertEquals("123456", OCCommandUtils.getPassword(command));
        assertEquals("https://api.starter-us-west-1.openshift.com",OCCommandUtils.getServer(command));
    }

}