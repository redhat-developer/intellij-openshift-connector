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


public class CountryCodeValidatorTest {

  JTextField textField = new JTextField();
  CountryCodeValidator validator = new CountryCodeValidator(textField);

  @Test
  public void testEmptyCountryCode() {
    textField.setText("");
    ValidationInfo result = validator.get();
    assertNotNull(result);
    assertEquals("Please provide a country code", result.message);
  }

  @Test
  public void testValidCountry() {
    List<String> data = Arrays.asList("123",
      "+001",
      "+33");
    for (String code : data) {
      textField.setText(code);
      ValidationInfo result = validator.get();
      assertNull(result);
    }
  }

  @Test
  public void testInValidCountry() {
    List<String> data = Arrays.asList("1234",
      "abc",
      "+1a");
    for (String code : data) {
      textField.setText(code);
      ValidationInfo result = validator.get();
      assertNotNull(result);
      assertTrue(result.message, result.message.startsWith("Country code must be a number"));
    }
  }

}
