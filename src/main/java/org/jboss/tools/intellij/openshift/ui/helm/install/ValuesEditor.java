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
package org.jboss.tools.intellij.openshift.ui.helm.install;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jetbrains.yaml.YAMLFileType;

class ValuesEditor extends JPanel implements Disposable {

  private static final int EDITOR_HEIGHT = 400;

  private final Project project;
  private Editor editor;
  private DocumentListener listener;

  ValuesEditor(Disposable parentDisposable, Project project) {
    super(new MigLayout(
      "flowy, ins 0, gap 0 0 0 0, fill",
      "[fill, 400:1000]"));
    Disposer.register(parentDisposable, this);
    this.project = project;
    createComponents();
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (editor != null) {
      editor.getComponent().setEnabled(enabled);
    }
    super.setEnabled(enabled);
  }


  public void showValues(final String content) {
    /*
     * Workaround:
     * Create fresh editor each time a new value should be shown.
     * UI thread became blocked for 10s when I was changing file/document for an existing editor.
     */
    disposeExistingEditor(editor);
    this.editor = createYamlEditor(content, project);
  }

  public void setValuesListener(DocumentListener listener) {
    this.listener = listener;
  }

  @Override
  public void dispose() {
    disposeExistingEditor(editor);
  }

  private void createComponents() {
    add(new JBLabel("Values:"), "gapbottom 6");
  }

  private void disposeExistingEditor(Editor editor) {
    if (editor == null) {
      return;
    }
    if (listener != null) {
      editor.getDocument().removeDocumentListener(listener);
    }
    remove(editor.getComponent());
    asyncDeleteFile(editor);
    EditorFactory.getInstance().releaseEditor(editor);
  }

  private void asyncDeleteFile(Editor editor) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
      if (virtualFile != null) {
        try {
          FileUtil.asyncDelete(virtualFile.toNioPath().toFile());
        } catch (UnsupportedOperationException e) {
          // swallow
        }
      }
    });
  }

  private Editor createYamlEditor(String content, Project project) {
    String filename = System.currentTimeMillis() + ".tmp";
    VirtualFile file = new LightVirtualFile(filename, YAMLFileType.YML, content);
    Editor chartValues = createEditor(file, project);
    if (chartValues != null) {
      chartValues.getDocument().addDocumentListener(listener);
      add(chartValues.getComponent(), "pushx, growx, pushy, growy, height " + EDITOR_HEIGHT);
      SwingUtils.layoutParent(4, this);
    }
    return chartValues;
  }

  private Editor createEditor(VirtualFile file, Project project) {
    Document document = ReadAction.compute(() ->
        FileDocumentManager.getInstance().getDocument(file)
    );
    if (document == null) {
      return null;
    }
    Editor editor = EditorFactory.getInstance().createEditor(
      document,
      project,
      file,
      false,
      EditorKind.CONSOLE);
    EditorSettings settings = editor.getSettings();
    settings.setLineNumbersShown(false);
    settings.setIndentGuidesShown(false);
    return editor;
  }

  public String getValues() {
    return ReadAction.compute(() -> {
        String values = null;
        if (editor != null) {
          values = editor.getDocument().getText().trim();
        }
        return values;
      }
    );
  }

}