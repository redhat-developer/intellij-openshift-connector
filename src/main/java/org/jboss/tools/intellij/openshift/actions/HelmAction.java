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
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import java.util.Arrays;
import javax.swing.tree.TreePath;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;

public abstract class HelmAction extends TelemetryAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(HelmAction.class);

  protected HelmAction(Class... filters) {
    super(filters);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
    // not invoked
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected) {
    setTelemetrySender(new TelemetrySender(PREFIX_ACTION + getTelemetryActionName()));
    Helm helm = getHelm(anActionEvent);
    if (helm == null) {
      return;
    }
    if (selected.length == 0 || path.length == 0) {
      actionPerformed(anActionEvent, (TreePath) null, null);
    } else if (selected.length == 1) {
      Object selectedElement = getElement(selected[0]);
      actionPerformedOnSelectedObject(anActionEvent, selectedElement, helm);
    } else {
      Object[] selectedElements = Arrays.stream(selected)
        .map(StructureTreeAction::getElement)
        .toArray();
      actionPerformedOnSelectedObjects(anActionEvent, selectedElements, helm);
    }
  }

  protected Helm getHelm(AnActionEvent anActionEvent) {
    try {
      return ActionUtils.getApplicationRootNode(anActionEvent).getHelm();
    } catch (Exception e) {
      LOGGER.warn("Could not get helm: {}", e.getMessage(), e);
      return null;
    }
  }

  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Helm helm) {
    // noop implementation
  }

  public void actionPerformedOnSelectedObjects(AnActionEvent anActionEvent, Object[] selected, @NotNull Helm helm) {
    // noop implementation
  }
}
