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
package org.jboss.tools.intellij.openshift.ui.project;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.PopupBorder;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.function.Supplier;

public class ChangeActiveProjectDialog extends DialogWrapper {

    private static final String WIDTH = "300";
    private final Project project;
    private final String kind;
    private final String currentProject;
    private final Collection<String> allProjects;
    private final Point location;
    private TextFieldWithCompletion activeProjectTextField;

    private String activeProject;

    private boolean createNewProject;

    public ChangeActiveProjectDialog(
      @Nullable Project project,
      String kind,
      String currentProject,
      Collection<String> allProjects,
      Point location) {
        super(project, false);
        this.project = project;
        this.kind = kind;
        this.currentProject = currentProject;
        this.allProjects = allProjects;
        this.location = location;
        init();
    }

    @Override
    protected void init() {
        super.init();
        Window dialogWindow = getPeer().getWindow();
        JRootPane rootPane = ((RootPaneContainer) dialogWindow).getRootPane();
        registerShortcuts(rootPane);
        setOKButtonText("Change");
        setBorders(rootPane);
        setLocation(location);
        setTitle("Change Active " + kind);
    }

    @Override
    public void setLocation(Point location) {
        if (location == null) {
            location = MouseInfo.getPointerInfo().getLocation();
        }
        super.setLocation(location);
    }

    private void registerShortcuts(JRootPane rootPane) {
        AnAction escape = ActionManager.getInstance().getAction(IdeActions.ACTION_EDITOR_ESCAPE);
        DumbAwareAction.create(e -> closeImmediately())
          .registerCustomShortcutSet(escape == null ?
            CommonShortcuts.ESCAPE
            : escape.getShortcutSet(), rootPane, myDisposable);
    }

    private void setBorders(JRootPane rootPane) {
        rootPane.setBorder(PopupBorder.Factory.create(true, true));
        rootPane.setWindowDecorationStyle(JRootPane.NONE);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JComponent panel = new JPanel(new MigLayout(
          "flowx, ins 0, gap 0, fillx, filly, hidemode 3",
          "[left]10[" + WIDTH +",fill]"));
        JLabel activeProjectLabel = new JBLabel("Change Active " + kind + " '" + currentProject + "'", SwingConstants.LEFT);
        SwingUtils.setBold(activeProjectLabel);
        panel.add(activeProjectLabel, "spanx");
        activeProjectLabel.setBorder(JBUI.Borders.emptyBottom(10));

        JLabel newActiveProjectLabel = new JBLabel("To " + kind + ":", SwingConstants.LEFT);
        newActiveProjectLabel.setBorder(JBUI.Borders.empty(10, 0));
        panel.add(newActiveProjectLabel, "left, bottom");
        this.activeProjectTextField = new TextFieldWithAutoCompletion<>(
          project, onLookup(allProjects), false, true, null);
        activeProjectTextField.selectAll();
        panel.add(activeProjectTextField, "pushx, growx, wrap");
        ComponentValidator activeProjectValidator = new ComponentValidator(myDisposable)
          .withValidator(new ActiveProjectValidator())
          .installOn(activeProjectTextField)
          .andRegisterOnDocumentListener(activeProjectTextField);
        activeProjectValidator.revalidate();
        JLabel createProjectLabel = new JBLabel("<html>You can <a href=\"\">create a new project</a> instead.</html>");
        createProjectLabel.setBorder(JBUI.Borders.emptyTop(20));
        createProjectLabel.addMouseListener(onClicked());
        panel.add(createProjectLabel, "spanx");
        return panel;
    }

    private MouseListener onClicked() {
        return new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                ChangeActiveProjectDialog.this.createNewProject = true;
                closeImmediately();
            }
        };
    }

    private TextFieldWithAutoCompletionListProvider<String> onLookup(Collection<String> projects) {
        return new TextFieldWithAutoCompletionListProvider<>(projects) {
            public @NotNull String getLookupString(@NotNull String item) {
                return item;
            }
        };
    }

    private void closeImmediately() {
        if (isVisible()) {
            doCancelAction();
        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        this.activeProject = activeProjectTextField.getText();
    }

    public String getActiveProject() {
        return activeProject;
    }

    public boolean isCreateNewProject() {
        return createNewProject;
    }

    private class ActiveProjectValidator implements Supplier<ValidationInfo> {

        @Override
        public ValidationInfo get() {
            String activeProject = activeProjectTextField.getText();
            ValidationInfo validation = getValidationInfo(activeProject);
            // update OK button
            setOKActionEnabled(validation.okEnabled);
            return validation;
        }

        private ValidationInfo getValidationInfo(String project) {
            ValidationInfo validation = new ValidationInfo("").withOKEnabled();
            if (StringUtil.isEmptyOrSpaces(project)) {
                validation = new ValidationInfo("Provide active Project").forComponent(activeProjectTextField).asWarning();
            } else if (project.equals(currentProject)) {
                validation = new ValidationInfo("Choose different Project").forComponent(activeProjectTextField).asWarning();
            }
            return validation;
        }
    }
}
