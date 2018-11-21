package org.jboss.tools.openshift.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ActionTest extends LightPlatformCodeInsightFixtureTestCase {
  public AnActionEvent createEvent(Object selected) {
    AnActionEvent event = mock(AnActionEvent.class);
    Presentation presentation = new Presentation();
    TreeSelectionModel model = mock(TreeSelectionModel.class);
    Tree tree = mock(Tree.class);
    TreePath path = mock(TreePath.class);
    when(path.getLastPathComponent()).thenReturn(selected);
    when(tree.getSelectionModel()).thenReturn(model);
    when(model.getSelectionPath()).thenReturn(path);
    when(event.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
    when(event.getPresentation()).thenReturn(presentation);
    return event;
  }

  public abstract AnAction getAction();

}
