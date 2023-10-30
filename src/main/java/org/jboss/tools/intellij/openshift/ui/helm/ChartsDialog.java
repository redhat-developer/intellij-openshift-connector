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
package org.jboss.tools.intellij.openshift.ui.helm;

import com.intellij.find.SearchTextArea;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.PopupBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.ui.StatusIcon;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jboss.tools.intellij.openshift.ui.TableRowFilterFactory;
import org.jboss.tools.intellij.openshift.utils.ToolFactory;
import org.jboss.tools.intellij.openshift.utils.helm.Chart;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RootPaneContainer;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.jboss.tools.intellij.openshift.ui.SwingUtils.EXECUTOR_UI;
import static org.jboss.tools.intellij.openshift.ui.SwingUtils.setBold;
import static org.jboss.tools.intellij.openshift.ui.helm.ChartVersions.toChartVersions;

public class ChartsDialog extends DialogWrapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChartsDialog.class);

  private final Project project;

  private ChartsTableModel chartsTableModel;
  private JBTable chartsTable;

  private StatusIcon statusIcon;
  private JBLabel closeIcon;

  public ChartsDialog(Project project) {
    super(project, null, false, IdeModalityType.MODELESS, false);
    this.project = project;
    init();
  }

  @Override
  protected void init() {
    super.init();
    setUndecorated(true);
    Window dialogWindow = getPeer().getWindow();
    setDialogSize(dialogWindow);
    JRootPane rootPane = ((RootPaneContainer) dialogWindow).getRootPane();
    registerShortcuts(rootPane);
    setBorders(rootPane);
    load(chartsTable, chartsTableModel);
    getPeer().getWindow().addWindowListener(onDialogFocusLost());
  }

  private WindowAdapter onDialogFocusLost() {
    return new WindowAdapter() {

      @Override
      public void windowDeactivated(WindowEvent e) {
        //closeImmediately();
      }
    };
  }

  private static void setBorders(JRootPane rootPane) {
    rootPane.setBorder(PopupBorder.Factory.create(true, true));
    rootPane.setWindowDecorationStyle(JRootPane.NONE);
  }

  private void registerShortcuts(JRootPane rootPane) {
    AnAction escape = ActionManager.getInstance().getAction("EditorEscape");
    DumbAwareAction.create(e -> closeImmediately())
      .registerCustomShortcutSet(escape == null ?
        CommonShortcuts.ESCAPE
        : escape.getShortcutSet(), rootPane, myDisposable);
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    JBPanel<?> panel = new JBPanel<>(new MigLayout(
      "flowx, ins 0, gap 0, fillx, filly, hidemode 3",
      "[100:100:100][left][left][right]"));

    JLabel title = new JBLabel("Helm charts");
    setBold(title);
    panel.add(title, "gap 0 0 0 10");

    this.statusIcon = new StatusIcon();
    panel.add(statusIcon.get(), "alignx left, aligny top, pushx");

    this.closeIcon = new JBLabel();
    closeIcon.setIcon(AllIcons.Windows.CloseSmall);
    closeIcon.addMouseListener(onClose());
    panel.add(closeIcon, "aligny top, wrap");

    JTextArea filterTextArea = new JBTextArea(1, 60);
    SearchTextArea searchTextArea = new SearchTextArea(filterTextArea, true);
    searchTextArea.setBorder(createSearchTextBorders());
    panel.add(searchTextArea, "spanx, pushx, growx, wrap");

    OnePixelSplitter splitter = new OnePixelSplitter(true, .66f);
    splitter.getDivider().setBackground(OnePixelDivider.BACKGROUND);
    panel.add(splitter, "spanx, pushx, growx, growy, pushy, wrap");

    this.chartsTableModel = new ChartsTableModel();
    this.chartsTable = SwingUtils.createTable(chartsTableModel);
    chartsTable.setShowGrid(false);
    chartsTable.setFocusable(false);
    chartsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    chartsTable.putClientProperty(RenderingUtil.ALWAYS_PAINT_SELECTION_AS_FOCUSED, true);
    chartsTable.setBorder(JBUI.Borders.empty(2, 2, 2, 0));
    JBScrollPane tableScrolledPane = SwingUtils.createScrollPane(chartsTable);
    tableScrolledPane.setBorder(JBUI.Borders.empty());
    TableRowFilterFactory
      .createOn(chartsTable)
      .usingInput(filterTextArea.getDocument());
    splitter.setFirstComponent(tableScrolledPane);

    ChartPanels chartPanels = new ChartPanels(getDisposable(), project);
    chartPanels.select(ChartPanels.DETAILS_PANEL,true);
    chartsTable.getSelectionModel().addListSelectionListener(
      onTableItemSelected(chartPanels, chartsTable, chartsTableModel));
    splitter.setSecondComponent(chartPanels);

    IdeFocusManager.getInstance(null).requestFocus(searchTextArea, true);

    return panel;
  }

  private MouseAdapter onClose() {
    return new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        close(0);
      }
    };
  }

  private ListSelectionListener onTableItemSelected(final ChartPanels chartDetailsPane, final JTable table, final ChartsTableModel model) {
    return e -> {
      if (e.getValueIsAdjusting()
        || table.getRowCount() == 0) {
        return;
      }
      int[] selectedRows = table.getSelectedRows();
      if (selectedRows.length == 0) {
        return;
      }
      int selectedChart = table.convertRowIndexToModel(selectedRows[0]);
      ChartVersions chart = model.getChart(selectedChart);
      chartDetailsPane.setChart(chart);
    };
  }

  private void load(final JTable table, final ChartsTableModel tableModel) {
    Helm helm = ToolFactory.getInstance().getHelm(project).getNow(null);
    if (helm == null
      || tableModel == null) {
      return;
    }
    CompletableFuture
      .runAsync(() -> {
          statusIcon.setLoading();
          tableModel.setupColumns();
        }
        , EXECUTOR_UI)
      .thenApplyAsync((Function<Void, List<Chart>>) (Void) -> {
        try {
          return helm.listAll();
        } catch (IOException e) {
          LOGGER.warn("Could not load all helm charts.", e);
          return Collections.emptyList();
        }
      }, SwingUtils.EXECUTOR_BACKGROUND)
      .thenAcceptAsync((charts) -> {
        List<ChartVersions> chartVersions = toChartVersions(charts);
        tableModel.setCharts(chartVersions);
        table.getColumn(ChartsTableModel.ICON_COLUMN).setMaxWidth(50);
        table.setRowSelectionInterval(0, 0);
        statusIcon.setEmpty();
      }, EXECUTOR_UI);
  }

  private void closeImmediately() {
    if (isVisible()) {
      doCancelAction();
    }
  }

  private void setDialogSize(Window dialogWindow) {
    Dimension panelSize = getPreferredSize();
    panelSize.width += JBUIScale.scale(24);//hidden 'loading' icon
    panelSize.height *= 2;
    dialogWindow.setMinimumSize(panelSize);
    panelSize.height *= 1.5;
    panelSize.width *= 1.15;
    dialogWindow.setSize(panelSize);
  }

  private static Border createSearchTextBorders() {
    return JBUI.Borders.compound(
      JBUI.Borders.customLine(JBUI.CurrentTheme.BigPopup.searchFieldBorderColor(), 1, 0, 1, 0),
      JBUI.Borders.empty(1, 0, 2, 0));
  }

  private static class ChartsTableModel extends DefaultTableModel {

    public static final String ICON_COLUMN = "Icon";
    private List<ChartVersions> charts;

    public void setCharts(List<ChartVersions> newCharts) {
      getDataVector().clear();
      List<ChartVersions> charts = getCharts();
      charts.clear();
      charts.addAll(newCharts);
    }

    @Override
    public int getRowCount() {
      return getCharts().size();
    }

    public ChartVersions getChart(int index) {
      List<ChartVersions> charts = getCharts();
      if (index >= charts.size()) {
        return null;
      }
      return charts.get(index);
    }

    public void setupColumns() {
      setColumnCount(0);
      addColumn("Icon");
      addColumn("Name");
      addColumn("Description");
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch(columnIndex) {
        case 0:
          return ImageIcon.class;
        case 1:
        case 2:
          return String.class;
        default:
          return null;
      }
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      List<ChartVersions> charts = getCharts();
      if (rowIndex >= charts.size()) {
        return null;
      }
      ChartVersions chart = charts.get(rowIndex);
      switch(columnIndex) {
        case 0:
          return ChartIcons.getIcon(chart.getName());
        case 1:
          return chart.getName();
        case 2:
          return chart.getDescription();
        default:
          return null;
      }
    }

    private List<ChartVersions> getCharts() {
      if (charts == null) {
        this.charts = new ArrayList<>();
      }
      return charts;
    }
  }

}
