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

import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.components.JBLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

class LoadingPanel extends JPanel {

      LoadingPanel() {
        super(new MigLayout("fill, hidemode 3"));
        createComponents();
      }

      private void createComponents() {
        add(new JBLabel(AnimatedIcon.Default.INSTANCE), "pushx, pushy, growx, growy, align 50% 50%, height 200:200:200");
      }
    }