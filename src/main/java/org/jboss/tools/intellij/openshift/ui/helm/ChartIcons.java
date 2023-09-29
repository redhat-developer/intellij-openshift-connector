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
package org.jboss.tools.intellij.openshift.ui.helm;

import com.intellij.ui.IconManager;

import javax.swing.Icon;

public class ChartIcons {

  private static final String HELM_ICON = "/images/helm/helm.png";

  public static Icon getIcon(String name) {
    return IconManager.getInstance().getIcon(HELM_ICON, ChartIcons.class);
  }


}
