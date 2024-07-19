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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.ui.BaseDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Point;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class CreateNewProjectDialog extends BaseDialog {

  private static final int WIDTH = 200;
  private final String kind;
  private final Collection<String> allProjects;
  private JBTextField newProjectTextField;

  private String newProject;

  public CreateNewProjectDialog(@Nullable Project project, Collection<String> allProjects, String kind, Point location) {
    super(project, location);
    this.kind = kind;
    this.allProjects = allProjects;
    init();
  }

  @Override
  protected void init() {
    super.init();
    setOKButtonText("Create");
    setTitle("Create New " + kind);
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    JComponent panel = new JPanel(new MigLayout(
      "flowx, ins 0, gap 0, fillx, filly, hidemode 3",
      "[left]10[" + WIDTH + ",fill]"));
    JLabel newActiveProjectLabel = new JBLabel("New " + kind.toLowerCase() + ":", SwingConstants.LEFT);
    newActiveProjectLabel.setBorder(JBUI.Borders.empty(10, 0));
    panel.add(newActiveProjectLabel, "left, bottom");
    this.newProjectTextField = new JBTextField();
    newProjectTextField.selectAll();
    panel.add(newProjectTextField, "pushx, growx, wrap");
    ComponentValidator activeProjectValidator = new ComponentValidator(myDisposable)
      .withValidator(new ActiveProjectValidator())
      .installOn(newProjectTextField)
      .andRegisterOnDocumentListener(newProjectTextField);
    activeProjectValidator.revalidate();
    return panel;
  }

  @Override
  protected void doOKAction() {
    super.doOKAction();
    this.newProject = newProjectTextField.getText();
  }

  public String getNewProject() {
    return newProject;
  }

  private class ActiveProjectValidator implements Supplier<ValidationInfo> {

    private final Pattern nameRegex = Pattern.compile(
      "^[a-z0-9]+(?:-[a-z0-9]+)*$");

    @Override
    public ValidationInfo get() {
      String activeProject = newProjectTextField.getText();
      ValidationInfo validation = getValidationInfo(activeProject);
      // update OK button
      setOKActionEnabled(validation.okEnabled);
      return validation;
    }

    private ValidationInfo getValidationInfo(String newProject) {
      ValidationInfo validation = new ValidationInfo("").withOKEnabled();
      if (StringUtil.isEmptyOrSpaces(newProject)) {
        validation = new ValidationInfo("Provide name", newProjectTextField).asWarning();
      } else if (newProject.length() > 63) {
        validation = new ValidationInfo("Mustn't have > 63 characters", newProjectTextField).asWarning();
      } else if (newProject.chars().allMatch(Character::isDigit)) {
        validation = new ValidationInfo("Mustn't contain only numeric characters", newProjectTextField).asWarning();
      } else if (allProjects.contains(newProject)) {
        validation = new ValidationInfo("Already exists, choose new name", newProjectTextField).asWarning();
      } else if (!nameRegex.matcher(newProject).matches()) {
        validation = new ValidationInfo("Must be alphanumeric and may contain -", newProjectTextField);
      }
      return validation;
    }
  }
}
