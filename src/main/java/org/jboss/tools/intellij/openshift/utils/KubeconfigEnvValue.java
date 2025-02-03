package org.jboss.tools.intellij.openshift.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubeconfigEnvValue {

  private static final String KUBECONFIG_KEY = "KUBECONFIG";

  private static final Logger LOGGER = LoggerFactory.getLogger(KubeconfigEnvValue.class);

  private KubeconfigEnvValue() {
    // inhibit instantiation
  }

  public static void copyToSystem() {
    String current = System.getProperty(KUBECONFIG_KEY);
    if (!StringUtil.isEmpty(current)) {
      LOGGER.info("Current KUBECONFIG value is " + current + ".");
      return;
    }
    String shellValue = EnvironmentUtil.getValue(KUBECONFIG_KEY);
    if (StringUtil.isEmpty(shellValue)) {
      return;
    }
    LOGGER.info("Copying KUBECONFIG value " + shellValue+ " from shell to System.");
    System.getProperties().put(KUBECONFIG_KEY, shellValue);
    System.getProperties().put(KUBECONFIG_KEY.toLowerCase(), shellValue);
  }


}
