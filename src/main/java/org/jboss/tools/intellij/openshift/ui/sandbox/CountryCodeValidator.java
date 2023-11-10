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

public class CountryCodeValidator implements Supplier<ValidationInfo> {
    private final JTextComponent component;

    /*
     * see https://github.com/codeready-toolchain/registration-service/blob/master/pkg/assets/landingpage.js
     */
    private static final Pattern pattern = Pattern
            .compile("^[+]?\\d+$");

    public CountryCodeValidator(JTextComponent component) {
        this.component = component;
    }

    @Override
    public ValidationInfo get() {
        String text = component.getText();
        if (text.isEmpty()) {
            return new ValidationInfo("Please provide a country code", component);
        }
        if (pattern.matcher(text).matches()) {
            return null;
        }
        return new ValidationInfo("Country code must be a number", component);
    }
}
