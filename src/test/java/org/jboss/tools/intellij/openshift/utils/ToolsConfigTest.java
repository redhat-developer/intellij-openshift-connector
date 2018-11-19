package org.jboss.tools.intellij.openshift.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class ToolsConfigTest {
  private static final URL url = ToolsConfigTest.class.getResource("/tools-test.json");
  @Test
  public void verifyThatConfigCanLoad() throws IOException {
    ToolsConfig config = ConfigHelper.loadToolsConfig(url);
    Assert.assertNotNull(config);
  }

  @Test
  public void verifyThatConfigReturnsTools() throws IOException {
    ToolsConfig config = ConfigHelper.loadToolsConfig(url);
    Assert.assertNotNull(config);
    ToolsConfig.Tool odo = config.getTools().get("odo");
    Assert.assertNotNull(odo);
  }

  @Test
  public void verifyThatConfigReturnsVersion() throws IOException {
    ToolsConfig config = ConfigHelper.loadToolsConfig(url);
    Assert.assertNotNull(config);
    ToolsConfig.Tool odo = config.getTools().get("odo");
    Assert.assertNotNull(odo);
    Assert.assertEquals("0.0.14", odo.getVersion());
  }
}
