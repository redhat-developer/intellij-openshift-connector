package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.ui.tree.BaseTreeModel;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import me.snowdrop.servicecatalog.api.client.ServiceCatalogClient;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.RefreshableTreeModel;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.utils.ConfigWatcher;
import org.jboss.tools.intellij.openshift.utils.OdoConfig;
import org.jboss.tools.intellij.openshift.utils.OdoHelper;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ApplicationTreeModel extends BaseTreeModel<Object> implements ConfigWatcher.Listener, RefreshableTreeModel, LazyMutableTreeNode.ChangeListener {
    private DefaultMutableTreeNode ROOT;

    private static final String ERROR = "Please login to cluster first";
    private OpenShiftClient client;

    public ApplicationTreeModel() {
        client = loadClient();
        CompletableFuture.runAsync(new ConfigWatcher(new File(ConfigHelper.getKubeConfigPath()), this));
        ROOT = new ApplicationsRootNode(client);
        ((LazyMutableTreeNode) ROOT).addChangeListener(this);
    }

    protected OpenShiftClient loadClient() {
        return new DefaultOpenShiftClient(new ConfigBuilder().build());
    }

    public OpenShiftClient getClient() {
        return client;
    }

    @Override
    public List<? extends Object> getChildren(Object o) {
        if (o instanceof LazyMutableTreeNode) {
            LazyMutableTreeNode node = (LazyMutableTreeNode) o;
            if (!((LazyMutableTreeNode) o).isLoaded()) {
                load(node);
            }
            return Collections.list(((MutableTreeNode)o).children());

        } else {
            return Collections.emptyList();
        }
    }

    private void load(LazyMutableTreeNode node) {
        node.setLoaded(true);
        if (node == ROOT) {
            loadRoot();
        } else if (node instanceof ProjectNode) {
            loadProject(node);
        } else if (node instanceof ApplicationNode) {
            loadApplication(node);
        } else if (node instanceof ComponentNode) {
            loadComponent(node);
        }
    }

    private void loadProject(LazyMutableTreeNode node) {
        try {
            ConfigHelper.loadOdoConfig().getActiveApplications().stream().filter(a -> a.getProject().equals(((Project)node.getUserObject()).getMetadata().getName())).forEach(a -> node.add(new ApplicationNode(a)));
        } catch (IOException e) {
            node.add(new DefaultMutableTreeNode("Failed to load applications"));
        }
    }

    private void loadApplication(LazyMutableTreeNode node) {
        try {
            loadDCs(node, client);
        } catch (KubernetesClientException e) {
            node.add(new DefaultMutableTreeNode("Failed to load application deployment configs"));
        }
        try {
            loadServiceInstances(node, client);
        } catch (KubernetesClientException e) {}
    }

    protected void loadDCs(LazyMutableTreeNode node, OpenShiftClient client) {
        client.deploymentConfigs().inNamespace(node.getParent().toString()).withLabelSelector(new LabelSelectorBuilder().addToMatchLabels("app", node.toString()).build()).list().getItems().forEach(dc -> node.add(new ComponentNode(dc)));
    }

    protected void loadServiceInstances(LazyMutableTreeNode node, OpenShiftClient client) {
        ServiceCatalogClient sc = client.adapt(ServiceCatalogClient.class);
        sc.serviceInstances().inNamespace(((Project) ((LazyMutableTreeNode) node.getParent()).getUserObject()).getMetadata().getName()).withLabelSelector(new LabelSelectorBuilder().addToMatchLabels("app", this.toString()).build()).list().getItems().forEach(si -> node.add(new ComponentNode(si)));
    }
    protected LabelSelector getLabelSelector(LazyMutableTreeNode node) {
        return new LabelSelectorBuilder().addToMatchLabels(KubernetesLabels.APP_LEGACY_LABEL, node.getParent().toString())
          .addToMatchLabels(KubernetesLabels.COMPONENT_NAME_LABEL, node.toString())
          .build();
    }

    protected void loadComponent(LazyMutableTreeNode node) {
        try {
            client.persistentVolumeClaims().inNamespace(((Project) ((LazyMutableTreeNode) node.getParent().getParent()).getUserObject()).getMetadata().getName()).withLabelSelector(getLabelSelector(node)).list().getItems().forEach(pvc -> node.add(new PersistentVolumeClaimNode(pvc)));
        } catch (KubernetesClientException e) {
            node.add(new DefaultMutableTreeNode("Failed to load persistent volume claims"));
        }
    }


    @Override
    public Object getRoot() {
        return ROOT;
    }

    private void loadRoot() {
        try {
            OdoHelper.INSTANCE.getProjects(client).stream().forEach(p -> ROOT.add(new ProjectNode(p)));
        } catch (Exception e) {
            ROOT.add(new DefaultMutableTreeNode("Failed to load projects"));
        }
    }

    @Override
    public void onUpdate(ConfigWatcher source) {
        refresh();
    }

    @Override
    public void refresh() {
        TreePath path = new TreePath(ROOT);
        client = loadClient();
        this.treeStructureChanged(path, new int[0], new Object[0]);
    }

    @Override
    public void onChildAdded(LazyMutableTreeNode source, Object child, int index) {
        if (child instanceof LazyMutableTreeNode) {
            ((LazyMutableTreeNode)child).addChangeListener(this);
        }
        treeNodesInserted(new TreePath(source.getPath()), new int[] { index }, new Object[] { child });
    }

    @Override
    public void onChildRemoved(LazyMutableTreeNode source, Object child, int index) {
        if (child instanceof LazyMutableTreeNode) {
            ((LazyMutableTreeNode)child).removeChangeListener(this);
        }
        treeNodesRemoved(new TreePath(source.getPath()), new int[] { index }, new Object[] { child });
    }

    @Override
    public void onChildrensRemoved(LazyMutableTreeNode source) {
        treeStructureChanged(new TreePath(source.getPath()), new int[0], new Object[0]);

    }
}
