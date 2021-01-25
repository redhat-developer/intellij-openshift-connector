/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree;

import com.intellij.ui.tree.BaseTreeModel;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClustersTreeModel extends BaseTreeModel<NamedContext> implements ConfigWatcher.Listener {

    private static final Logger LOG = LoggerFactory.getLogger(ClustersTreeModel.class);

    private static final String ROOT = "Clusters";
    private Config config;

    public ClustersTreeModel() {
         try {
             config = ConfigHelper.loadKubeConfig();
             ExecHelper.submit(new ConfigWatcher(Paths.get(ConfigHelper.getKubeConfigPath()), this));
         } catch (IOException e) {
             LOG.error(e.getLocalizedMessage(), e);
         }
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public List<NamedContext> getChildren(Object o) {
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
    public void onUpdate(ConfigWatcher source, Config config) {
        this.config = config;
        this.treeStructureChanged(new TreePath(ROOT), new int[0], new Object[0]);
    }
}
