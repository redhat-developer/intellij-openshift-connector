package org.jboss.tools.intellij.openshift.ui.cluster;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.PopupBorder;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Window;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class SelectResourceDialog<RESOURCE> extends DialogWrapper {

    private static final int HEIGHT = 40;
    private static final int WIDTH = 300;
    private final Project project;
    private final String kind;
    private final String currentResource;
    private final Collection<RESOURCE> allResources;
    private final Consumer<String> onOk;
    private final Point location;
    private final Function<RESOURCE, String> toResourceName;

    public SelectResourceDialog(
      @Nullable Project project,
      String kind,
      String currentResource,
      Collection<RESOURCE> allResources,
      Function<RESOURCE, String> toResourceName,
      Consumer<String> onOk, Point location) {
        super(project, false);
        this.project = project;
        this.kind = kind;
        this.currentResource = currentResource;
        this.allResources = allResources;
        this.toResourceName = toResourceName;
        this.onOk = onOk;
        this.location = location;
        init();
    }

    @Override
    protected void init() {
        super.init();
        setUndecorated(true);
        Window dialogWindow = getPeer().getWindow();
        JRootPane rootPane = ((RootPaneContainer) dialogWindow).getRootPane();
        registerShortcuts(rootPane);
        setBorders(rootPane);
        SwingUtils.setGlassPaneResizable(getPeer().getRootPane(), getDisposable());
        SwingUtils.setMovable(getRootPane());
    }

    private void registerShortcuts(JRootPane rootPane) {
        AnAction escape = ActionManager.getInstance().getAction("EditorEscape");
        DumbAwareAction.create(e -> closeImmediately())
          .registerCustomShortcutSet(escape == null ?
            CommonShortcuts.ESCAPE
            : escape.getShortcutSet(), rootPane, myDisposable);
    }

    private static void setBorders(JRootPane rootPane) {
        rootPane.setBorder(PopupBorder.Factory.create(true, true));
        rootPane.setWindowDecorationStyle(JRootPane.NONE);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JComponent panel = new JPanel(new BorderLayout());
        panel.setLayout(new MigLayout(
          "flowx, ins 0, gap 0, fillx, filly, hidemode 3",
          "[left][fill]"));
        JLabel currentLabel = new JBLabel("Current " + kind + ":", SwingConstants.LEFT);
        currentLabel.setBorder(JBUI.Borders.emptyBottom(10));
        panel.add(currentLabel, "");
        JLabel currentValueLabel = new JBLabel(currentResource);
        currentValueLabel.setBorder(JBUI.Borders.emptyBottom(10));
        panel.add(currentValueLabel, "left, wrap");

        JLabel newCurrentLabel = new JBLabel("New current " + kind + ":", SwingConstants.LEFT);
        currentLabel.setBorder(JBUI.Borders.emptyBottom(10));
        panel.add(currentLabel, "left");
        TextFieldWithAutoCompletion<RESOURCE> nameTextField =
          new TextFieldWithAutoCompletion<>(project, onLookup(allResources), false, null);
        panel.add(nameTextField, "pushx, growx, wrap");
        return panel;
    }

    private TextFieldWithAutoCompletionListProvider<RESOURCE> onLookup(Collection<RESOURCE> resources) {
        return new TextFieldWithAutoCompletionListProvider<>(resources) {
            public String getLookupString(RESOURCE item) {
                return toResourceName.apply(item);
            }
        };
    }

    private void closeImmediately() {
        if (isVisible()) {
            doCancelAction();
        }
    }

}
