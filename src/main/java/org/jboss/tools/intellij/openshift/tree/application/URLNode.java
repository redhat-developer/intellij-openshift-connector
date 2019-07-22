/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.URL;


public class URLNode extends LazyMutableTreeNode implements IconTreeNode {
  public URLNode(URL url) {
    super(url);
  }


  @Override
  public String toString() {
    URL url = (URL) userObject;
    return url.getName() + " (" + url.getPort() + ')';
  }

  @Override
  public String getIconName() {
    return "/images/url-node.png";
  }
}
