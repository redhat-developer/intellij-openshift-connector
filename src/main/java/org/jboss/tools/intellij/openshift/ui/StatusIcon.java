/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui;

import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.EmptyIcon;

import javax.swing.Icon;
import javax.swing.JLabel;

public class StatusIcon {

  private JLabel label;

  public void setEmpty() {
    set(EmptyIcon.ICON_16, null);
  }

  public void setLoading() {
    set(AnimatedIcon.Default.INSTANCE, null);
  }

  public void setSuccess(String message) {
    set(Icons.SUCCESS_ICON, message);
  }

  public void setError(String message) {
    set(Icons.ERROR_ICON, message);
  }

  public JLabel get() {
    if (label == null) {
      this.label = create();
    }
    return label;
  }

  private void set(Icon icon, String message) {
    JLabel label = get();
    label.setIcon(icon);
    label.setText(message);
  }

  private JLabel create() {
    JLabel label = new JBLabel();
    label.setIcon(EmptyIcon.ICON_16);
    return label;
  }
}
