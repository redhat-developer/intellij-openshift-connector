/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCli;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCliFactory;
import org.jboss.tools.intellij.openshift.utils.odo.OdoProjectDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import static com.intellij.openapi.ui.Messages.CANCEL_BUTTON;
import static com.intellij.openapi.ui.Messages.getWarningIcon;
import static org.jboss.tools.intellij.openshift.Constants.HELP_LABEL;
import static org.jboss.tools.intellij.openshift.Constants.CLUSTER_MIGRATION_ERROR_MESSAGE;
import static org.jboss.tools.intellij.openshift.Constants.CLUSTER_MIGRATION_MESSAGE;
import static org.jboss.tools.intellij.openshift.Constants.CLUSTER_MIGRATION_TITLE;
import static org.jboss.tools.intellij.openshift.Constants.UPDATE_LABEL;

public class ApplicationsRootNode extends LazyMutableTreeNode implements IconTreeNode {
  private boolean logged;
  private final ApplicationTreeModel model;
  private Odo odo;

  private static final String ERROR = "Please log in to the cluster";

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationsRootNode.class);

  public ApplicationsRootNode(ApplicationTreeModel model) {
    odo = new OdoProjectDecorator(OdoCliFactory.getInstance().getOdo(), model);
    setUserObject(odo.getMasterUrl());
    this.model = model;
  }

  public boolean isLogged() {
    return logged;
  }

  public void setLogged(boolean logged) {
    this.logged = logged;
  }

  public Odo getOdo() throws IOException {
    return odo;
  }

  public ApplicationTreeModel getModel() {
    return model;
  }

  @Override
  public void load() {
    super.load();
    try {
      odo.getProjects().stream().forEach(p -> add(new ProjectNode(p)));
      checkMigrate(odo, odo.getPreOdo10Projects());
      setLogged(true);
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      add(new DefaultMutableTreeNode(ERROR));
    }
  }

  private void checkMigrate(Odo odo, List<Project> preOdo10Projects) {
    if (!preOdo10Projects.isEmpty()) {
      int choice = Messages.showDialog(getModel().getProject(), CLUSTER_MIGRATION_MESSAGE, CLUSTER_MIGRATION_TITLE, new String[]{UPDATE_LABEL, HELP_LABEL, CANCEL_BUTTON}, 0, getWarningIcon());
      if (choice == 0) {
        try {
          List<Exception> exceptions = ProgressManager.getInstance().run(
                  new Task.WithResult<List<Exception>, Exception>(getModel().getProject(), CLUSTER_MIGRATION_TITLE, false) {
                    private int counter = 0;

                    @Override
                    protected List<Exception> compute(@NotNull ProgressIndicator indicator) throws Exception {
                      return odo.migrateProjects(preOdo10Projects, (project, kind) -> {
                        indicator.setText("Migrating " + kind + " for project " + project);
                        indicator.setFraction(counter++ / (preOdo10Projects.size() * 8));
                      });
                    }
                  }

          );
          if (!exceptions.isEmpty()) {
            Messages.showErrorDialog(getModel().getProject(), CLUSTER_MIGRATION_ERROR_MESSAGE, CLUSTER_MIGRATION_TITLE);
          }
        } catch (Exception e) {
        }
      } else if (choice == 1) {
        BrowserUtil.browse(Constants.MIGRATION_HELP_PAGE_URL);
      }
    }
  }

  @Override
  public void reload() {
    odo = OdoCliFactory.getInstance().getOdo();
    super.reload();
  }

  @Override
  public String getIconName() {
    return "/images/cluster.png";
  }
}
