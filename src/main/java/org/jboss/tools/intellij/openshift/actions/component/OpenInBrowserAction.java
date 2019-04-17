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
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.URL;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenInBrowserAction extends OdoAction {
  public OpenInBrowserAction() {
    super(ComponentNode.class);
  }

  private void openURL(List<URL> urls) {
    String url = getURL(urls.get(0));
    BrowserUtil.open(url);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        List<URL> urls = odo.listURLs(projectNode.toString(), applicationNode.toString(), componentNode.toString());
        if (urls.isEmpty()) {
          if (UIHelper.executeInUI(() -> JOptionPane.showConfirmDialog(null, "No URL for component " + componentNode.toString() + ", do you want to create one ?", "Create URL", JOptionPane.OK_CANCEL_OPTION)) == JOptionPane.OK_OPTION) {
            if (CreateURLAction.createURL(odo, projectNode, applicationNode, componentNode)) {
              urls = odo.listURLs(projectNode.toString(), applicationNode.toString(), componentNode.toString());
              openURL(urls);
            }
          }
        } else {
          openURL(urls);
        }
      } catch (KubernetesClientException | IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Open in Brower", JOptionPane.ERROR_MESSAGE));
      }
    });
  }


  protected String getURL(URL url) {
    return url.getProtocol() + "://" + url.getPath();
  }
}
