package org.jboss.tools.intellij.openshift.tree.application;

import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.OdoConfig;


public class ApplicationNode extends LazyMutableTreeNode implements IconTreeNode {
  public ApplicationNode(OdoConfig.Application application) {
    super(application);
  }

  @Override
  public String toString() {
    return ((OdoConfig.Application) userObject).getName();
  }

  @Override
  public String getIconName() {
    return "/images/application.png";
  }
}
