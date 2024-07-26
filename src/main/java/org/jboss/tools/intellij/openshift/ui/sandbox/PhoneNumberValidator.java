/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
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

import javax.swing.text.JTextComponent;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class PhoneNumberValidator implements Supplier<ValidationInfo> {
  private final JTextComponent component;

  private static final Pattern pattern = Pattern
    .compile("^[(]?\\d{2,3}[)]?[ \\-./]?\\d{2,3}[ \\-./]?\\d{2,4}[ \\-./]?\\d{0,4}[ \\-./]?\\d{0,4}$", Pattern.CASE_INSENSITIVE);

  public PhoneNumberValidator(JTextComponent component) {
    this.component = component;
  }

  @Override
  public ValidationInfo get() {
    String text = component.getText();
    if (text.isEmpty()) {
      return new ValidationInfo("Please provide a phone number", component);
    }
    if (pattern.matcher(text).matches()) {
      if (text.chars().allMatch(Character::isDigit) && text.length() < 7) {
        return new ValidationInfo("Phone number should be at least 7 digits", component);
      }
      return null;
    }
    return new ValidationInfo("Phone number should be digits optionally separated by space or '-' or '.' or '/'", component);
  }
}
