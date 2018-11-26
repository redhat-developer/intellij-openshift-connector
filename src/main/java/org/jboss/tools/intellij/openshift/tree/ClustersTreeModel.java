package org.jboss.tools.intellij.openshift.tree;

import com.intellij.ui.tree.BaseTreeModel;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.utils.ConfigWatcher;

import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ClustersTreeModel extends BaseTreeModel<NamedContext> implements ConfigWatcher.Listener {
    private static final String ROOT = "Clusters";
    private Config config;

    public ClustersTreeModel() {
         try {
             config = ConfigHelper.loadKubeConfig();
             CompletableFuture.runAsync(new ConfigWatcher(new File(ConfigHelper.getKubeConfigPath()), this));
         } catch (IOException e) {
             e.printStackTrace();
         }
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public List<? extends NamedContext> getChildren(Object o) {
        if (o.equals(ROOT)) {
            return config.getContexts().stream().collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public void onUpdate(ConfigWatcher source) {
        try {
            config = ConfigHelper.loadKubeConfig();
            this.treeStructureChanged(new TreePath(ROOT), new int[0], new Object[0]);
        } catch (IOException e) {
        }
    }
}
