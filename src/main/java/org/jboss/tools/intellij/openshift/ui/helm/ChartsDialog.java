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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.common.ui.UndecoratedDialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.StatusIcon;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jboss.tools.intellij.openshift.ui.TableRowFilterFactory;
import org.jboss.tools.intellij.openshift.utils.helm.Chart;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.tools.intellij.openshift.ui.SwingUtils.EXECUTOR_BACKGROUND;
import static org.jboss.tools.intellij.openshift.ui.SwingUtils.EXECUTOR_UI;
import static org.jboss.tools.intellij.openshift.ui.SwingUtils.setBold;
import static org.jboss.tools.intellij.openshift.ui.helm.ChartVersions.toChartVersions;

public class ChartsDialog extends UndecoratedDialog {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChartsDialog.class);

  @Override
  protected @NotNull List<ValidationInfo> doValidateAll() {
    return super.doValidateAll();
  }

  private static final int INITIAL_HEIGHT = 800;
  public static final int ICON_COLUMN_WIDTH = 50;
  public static final int NAME_COLUMN_WIDTH = 250;
  public static final int DESCRIPTION_COLUMN_WIDTH = 500;

  private static final String OPENSHIFT_REPO_NAME = "openshift";
  private static final String OPENSHIFT_REPO_URL = "https://charts.openshift.io/";

  private final ApplicationsRootNode rootNode;
  private final Helm helm;
  private final Odo odo;
  private final Project project;

  private JBLabel title;
  private ChartsTableModel chartsTableModel;
  private JBTable chartsTable;
  private StatusIcon statusIcon;

  public ChartsDialog(ApplicationsRootNode rootNode, Helm helm, Odo odo, Project project) {
    super(project, null, false, IdeModalityType.MODELESS, false);
    this.rootNode = rootNode;
    this.helm = helm;
    this.odo = odo;
    this.project = project;
    init();
  }

  @Override
  protected void init() {
    super.init();
    setUndecorated(true);
    registerEscapeShortcut(e -> closeImmediately());
    setGlassPaneResizable();
    setMovableUsing(title, statusIcon.get());

    setupTable(chartsTable, chartsTableModel, statusIcon)
      .thenCompose((Void) -> addDefaultRepo(helm))
      .thenCompose((Void) -> load(chartsTable, chartsTableModel, statusIcon, helm));
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    JBPanel<?> panel = new JBPanel<>(new MigLayout(
      "flowx, ins 0, gap 0, fillx, filly, hidemode 3",
      "[100:100:100][left][left][right]"));

    this.title = new JBLabel("Helm charts");
    setBold(title);
    panel.add(title, "gap 0 0 0 10");

    this.statusIcon = new StatusIcon();
    panel.add(statusIcon.get(), "alignx left, aligny top, pushx, growx");

    JBLabel closeIcon = new JBLabel();
    closeIcon.setIcon(AllIcons.Windows.CloseSmall);
    closeIcon.addMouseListener(onClose());
    panel.add(closeIcon, "aligny top, wrap");

    JTextArea filterTextArea = new JBTextArea(1, 60);
    SearchTextArea searchTextArea = new SearchTextArea(filterTextArea, true);
    searchTextArea.setBorder(createSearchTextBorders());
    panel.add(searchTextArea, "spanx, pushx, growx, wrap");

    OnePixelSplitter splitter = new OnePixelSplitter(true, .66f);
    splitter.getDivider().setBackground(OnePixelDivider.BACKGROUND);
    panel.add(splitter, "spanx, pushx, growx, pushy, growy, height " + INITIAL_HEIGHT + ", wrap");

    this.chartsTableModel = new ChartsTableModel();
    this.chartsTable =  new JBTable(chartsTableModel);
    chartsTable.setShowGrid(false);
    chartsTable.setFocusable(false);
    chartsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    chartsTable.putClientProperty(RenderingUtil.ALWAYS_PAINT_SELECTION_AS_FOCUSED, true);
    SwingUtils.disableCellEditors(chartsTable, String.class);
    chartsTable.setBorder(JBUI.Borders.empty(2, 2, 2, 0));
    JBScrollPane tableScrolledPane = SwingUtils.createScrollPane(chartsTable);
    tableScrolledPane.setBorder(JBUI.Borders.empty());
    TableRowFilterFactory
      .createOn(chartsTable)
      .usingInput(filterTextArea.getDocument());
    splitter.setFirstComponent(tableScrolledPane);

    InstallOrDetailsPanels installOrDetailsPanels = new InstallOrDetailsPanels(rootNode, getDisposable(), helm, odo, project);
    installOrDetailsPanels.select(InstallOrDetailsPanels.DETAILS_PANEL,true);
    chartsTable.getSelectionModel().addListSelectionListener(
      onTableItemSelected(installOrDetailsPanels, chartsTable, chartsTableModel));
    splitter.setSecondComponent(installOrDetailsPanels);

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

  private ListSelectionListener onTableItemSelected(final InstallOrDetailsPanels chartDetailsPane, final JTable table, final ChartsTableModel model) {
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

  private CompletableFuture<Void> setupTable(JTable table, ChartsTableModel tableModel, StatusIcon statusIcon) {
    return CompletableFuture
      .runAsync(() -> {
          statusIcon.setLoading();
          tableModel.setupColumns();
          table.getColumn(ChartsTableModel.ICON_COLUMN).setMaxWidth(ICON_COLUMN_WIDTH);
          table.getColumn(ChartsTableModel.NAME_COLUMN).setPreferredWidth(NAME_COLUMN_WIDTH);
          table.getColumn(ChartsTableModel.DESCRIPTION_COLUMN).setPreferredWidth(DESCRIPTION_COLUMN_WIDTH);
        }
        , EXECUTOR_UI);
  }

  private CompletableFuture<Void> addDefaultRepo(Helm helm) {
    return CompletableFuture
      .runAsync(() -> {
        try {
          helm.addRepo(OPENSHIFT_REPO_NAME, OPENSHIFT_REPO_URL, null);
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }, EXECUTOR_BACKGROUND);
  }

  private CompletableFuture<Void> load(
    final JTable table,
    final ChartsTableModel tableModel,
    final StatusIcon statusIcon,
    final Helm helm) {
      return CompletableFuture
        .supplyAsync((Supplier<List<Chart>>) () -> {
          try {
            return helm.search();
          } catch (IOException e) {
            LOGGER.warn("Could not load all helm charts.", e);
            return Collections.emptyList();
          }
        }, EXECUTOR_BACKGROUND)
        .thenAcceptAsync((charts) -> {
          List<ChartVersions> chartVersions = toChartVersions(charts);
          tableModel.setCharts(chartVersions);
          table.setRowSelectionInterval(0, 0);
          table.getParent().doLayout(); // fore repaint
          statusIcon.setEmpty();
        }, EXECUTOR_UI);
  }

  private static Border createSearchTextBorders() {
    return JBUI.Borders.compound(
      JBUI.Borders.customLine(JBUI.CurrentTheme.BigPopup.searchFieldBorderColor(), 1, 0, 1, 0),
      JBUI.Borders.empty(1, 0, 2, 0));
  }

  private static class ChartsTableModel extends DefaultTableModel {

    public static final String ICON_COLUMN = "Icon";
    public static final String NAME_COLUMN = "Name";
    public static final String DESCRIPTION_COLUMN = "Description";
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
      addColumn(ICON_COLUMN);
      addColumn(NAME_COLUMN);
      addColumn(DESCRIPTION_COLUMN);
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
      switch (columnIndex) {
        case 0:
          return ChartIcons.getIcon(chart);
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
