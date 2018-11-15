package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.application.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CreateComponentAction extends OdoAction {
  public CreateComponentAction() {
    super(ApplicationNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) selected;
    CompletableFuture.runAsync(() -> {
      try {
        CreateComponentDialog dialog = UIHelper.executeInUI(() -> {
          try {
            return showDialog(loadComponentTypes(ExecHelper.execute(odo, "catalog", "list", "components")));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
        if (dialog.isOK()) {
          createComponent(odo, applicationNode.toString(), dialog).thenRun(() -> {
            applicationNode.reload();
            //((ApplicationTreeModel)getTree(anActionEvent).getModel()).treeStructureChanged(path, new int[0], new Object[0]);
            if (dialog.getSourceType() == 0) {
              ExecHelper.executeWithTerminal(odo, "push", dialog.getName());
            }
          });
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create component", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  @Nullable
  protected CompletableFuture<Void> createComponent(String odo, String appName, CreateComponentDialog dialog) throws IOException {
      ExecHelper.execute(odo, "app", "set", appName);
      return createComponent(odo, dialog);
  }

  private CompletableFuture<Void> createComponent(String odo, CreateComponentDialog dialog) {
    if (dialog.getSourceType() == 0) {
      return ExecHelper.executeWithTerminal(odo, "create", dialog.getComponentType() + ':' + dialog.getComponentVersion(), dialog.getName(),
        "--local", dialog.getSource());
    } else {
      return ExecHelper.executeWithTerminal(odo, "create", dialog.getComponentType() + ':' + dialog.getComponentVersion(), dialog.getName(),
        "--git", dialog.getSource());
    }
  }

  protected CreateComponentDialog showDialog(List<String[]> types) {
    CreateComponentDialog dialog = new CreateComponentDialog(null);
    dialog.setComponentTypes(types.toArray(new String[types.size()][]));
    dialog.show();
    return dialog;
  }

  private List<String[]> loadComponentTypes(String output) throws IOException {
    try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
      return reader.lines().skip(1).map(s -> s.replaceAll("\\s{1,}", "|"))
        .map(s -> s.split("\\|"))
        .map(s -> new String[] {s[0], s[2]})
        .collect(Collectors.toList());
    }
  }
}
