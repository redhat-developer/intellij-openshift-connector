/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.sandbox;

import com.intellij.openapi.ui.ValidationInfo;
import org.junit.Test;

import javax.swing.JTextField;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class PhoneNumberValidatorTest {

  JTextField textField = new JTextField();
  PhoneNumberValidator validator = new PhoneNumberValidator(textField);

  @Test
  public void testEmptyPhoneNumber() {
    textField.setText("");
    ValidationInfo result = validator.get();
    assertNotNull(result);
    assertEquals("Please provide a phone number", result.message);
  }

  @Test
  public void testValidPhoneNumber() {
    List<String> data = Arrays.asList("123-456-7890",
      "(123) 456-7890",
      "(123)4567890",
      "123.456.7890",
      "123/456/7890",
      "1234567890",
      "12 34 56 78 90",
      "12.34.56.78.90");
    for (String phoneNumber : data) {
      textField.setText(phoneNumber);
      ValidationInfo result = validator.get();
      assertNull(result);
    }
  }

  @Test
  public void testInValidPhoneNumber() {
    List<String> data = Arrays.asList("123+456+7890",
      "123..456..7890",
      "-123 456 7890",
      "[123] 456 7890",
      "abcdefeghi",
      "123456789a",
      "..--  //",
      "()1234567890",
      "123456",
      "1234567890123456789");
    for (String phoneNumber : data) {
      textField.setText(phoneNumber);
      ValidationInfo result = validator.get();
      assertNotNull(result);
      assertTrue(result.message, result.message.startsWith("Phone number should be"));
    }
  }

}
