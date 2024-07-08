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
package org.jboss.tools.intellij.openshift.ui.helm.install;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;

class ResultPanel extends JPanel {
      private JTextArea installResultText;

      public ResultPanel() {
        super(new MigLayout("flowy, ins 0, gap 0 0 0 0, fill",
          "[fill, 400:1000]"));
        createComponents();
      }

      private void createComponents() {
        add(new JBLabel("Installation result:"), "gapbottom 6");
        this.installResultText = new JBTextArea();
        installResultText.setLineWrap(true);
        JBScrollPane scrollPane = new JBScrollPane(installResultText);
        scrollPane.setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 0, 0));
        add(scrollPane, "pushx, growx, pushy, growy, height 400");
      }

      public void showOutput(String content) {
        installResultText.setForeground(getForeground());
        installResultText.setText(content);
      }

      public void showError(String content) {
        installResultText.setForeground(SwingUtils.getErrorForeground());
        installResultText.setText(content);
      }

    }