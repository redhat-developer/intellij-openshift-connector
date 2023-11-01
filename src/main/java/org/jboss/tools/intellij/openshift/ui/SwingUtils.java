/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.impl.IdeGlassPaneEx;
import com.intellij.ui.JBColor;
import com.intellij.ui.WindowMoveListener;
import com.intellij.ui.WindowResizeListener;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SwingUtils {

  public static final Executor EXECUTOR_UI = runnable -> ApplicationManager.getApplication().invokeLater(runnable);
  public static final Executor EXECUTOR_BACKGROUND = runnable -> ApplicationManager.getApplication().executeOnPooledThread(runnable);
  private static final String URL_REGEX = "(https?:\\/\\/[^ )]+)";

  private SwingUtils() {
  }

  public static void setBold(JLabel label) {
    label.setFont(JBFont.create(label.getFont().deriveFont(Font.BOLD)));
  }

  /**
   * Returns the foreground color used for errors. Starting with IC-2023.1 this is provided by
   * {@link com.intellij.util.ui.NamedColorUtil} and can thus be replaced by the platform class.
   *
   * @return the color that should be used for errors
   */
  public static Color getErrorForeground() {
    return JBColor.namedColor("Label.errorForeground", new JBColor(new Color(13050413), JBColor.RED));
  }

  public static String linksToAnchors(String text) {
    int lastIndex = 0;
    StringBuilder builder = new StringBuilder();
    Pattern httpLinkRegex = Pattern.compile(URL_REGEX);
    Matcher matcher = httpLinkRegex.matcher(text);
    while (matcher.find()) {
      builder
        .append(text, lastIndex, matcher.start())
        .append("<a href=\"" + matcher.group(1)+ "\">" + matcher.group(1) + "</a>");
      lastIndex = matcher.end();
    }
    if (lastIndex < text.length()) {
      builder.append(text, lastIndex, text.length());
    }
    return builder.toString();
  }

  public static JBTable createTable(int visibleRows, AbstractTableModel resultsTableModel) {
    return new JBTable(resultsTableModel) {
      @Override
      public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(getWidth(), 1 + getRowHeight() * visibleRows);
      }
    };
  }

  public static void disableCellEditors(JTable table, Class...editorTypes) {
    if (editorTypes == null) {
      return;
    }
    Stream.of(editorTypes).forEach(editorType -> {
      TableCellEditor cellEditor = table.getDefaultEditor(editorType);
      if (cellEditor instanceof DefaultCellEditor) {
        ((DefaultCellEditor) cellEditor).setClickCountToStart(Integer.MAX_VALUE);
      }
    });
  }

  public static JBScrollPane createScrollPane(final JTable table) {

    return new JBScrollPane(table) {
      @Override
      public Dimension getMinimumSize() {
        Dimension size = super.getMinimumSize();
        size.width = table.getPreferredScrollableViewportSize().width;
        size.height = table.getPreferredScrollableViewportSize().height;
        return size;
      }
    };
  }

  public static String embedInHtml(String string) {
    return "<html><body>" + string + "</body></html>";
  }

  private static JBIterable<Component> focusableComponents(@NotNull Component component) {
    return UIUtil.uiTraverser(component)
      .bfsTraversal()
      .filter(c -> c instanceof JComboBox || c instanceof AbstractButton || c instanceof JTextComponent);
  }

  public static void setGlassPaneResizable(JRootPane rootPane, Disposable disposable) {
    WindowResizeListener resizeListener = new WindowResizeListener(rootPane, JBUI.insets(10), null);
    IdeGlassPaneEx glassPane = (IdeGlassPaneEx) rootPane.getGlassPane();
    glassPane.addMousePreprocessor(resizeListener, disposable);
    glassPane.addMouseMotionPreprocessor(resizeListener, disposable);
  }

  public static void setMovable(JRootPane rootPane, JComponent... movableComponents) {
    WindowMoveListener windowMoveListener = new WindowMoveListener(rootPane);
    Stream.of(movableComponents).forEach(
      component -> component.addMouseListener(windowMoveListener));
  }

}
