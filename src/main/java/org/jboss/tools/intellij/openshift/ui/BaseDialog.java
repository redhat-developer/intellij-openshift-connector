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
package org.jboss.tools.intellij.openshift.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.PopupBorder;
import org.jetbrains.annotations.Nullable;

import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import java.awt.Point;
import java.awt.Window;

import static org.jboss.tools.intellij.openshift.ui.SwingUtils.locationOrMouseLocation;

public abstract class BaseDialog extends DialogWrapper {

    private final Point location;

    protected BaseDialog(@Nullable Project project, Point location) {
        super(project, false);
        this.location = location;
        init();
    }

    @Override
    protected void init() {
        super.init();
        Window dialogWindow = getPeer().getWindow();
        JRootPane rootPane = ((RootPaneContainer) dialogWindow).getRootPane();
        registerShortcuts(rootPane);
        setBorders(rootPane);
        setLocation(locationOrMouseLocation(location));
    }

    private void registerShortcuts(JRootPane rootPane) {
        AnAction escape = ActionManager.getInstance().getAction(IdeActions.ACTION_EDITOR_ESCAPE);
        DumbAwareAction.create(e -> closeImmediately())
          .registerCustomShortcutSet(
            escape == null ? CommonShortcuts.ESCAPE : escape.getShortcutSet(),
            rootPane,
            myDisposable);
    }

    private void setBorders(JRootPane rootPane) {
        rootPane.setBorder(PopupBorder.Factory.create(true, true));
        rootPane.setWindowDecorationStyle(JRootPane.NONE);
    }

    protected void closeImmediately() {
        if (isVisible()) {
            doCancelAction();
        }
    }
}
