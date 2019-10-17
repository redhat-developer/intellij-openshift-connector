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
package org.jboss.tools.intellij.openshift.utils.odo;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class LocalConfigTest {
  private static final URL url = LocalConfigTest.class.getResource("/config-test.yaml");

  @Test
  public void verifyThatConfigCanLoad() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
  }

  @Test
  public void verifyThatConfigReturnsComponentSettings() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
    LocalConfig.ComponentSettings settings = config.getComponentSettings();
    Assert.assertNotNull(settings);
  }

  @Test
  public void verifyThatConfigReturnsComponentSettingsProperties() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
    LocalConfig.ComponentSettings settings = config.getComponentSettings();
    Assert.assertNotNull(settings);
    Assert.assertEquals("java:8", settings.getType());
    Assert.assertEquals("https://github.com/jeffmaury/non_existent", settings.getSourceLocation());
    Assert.assertEquals("git", settings.getSourceType());
    Assert.assertEquals("app", settings.getApplication());
    Assert.assertEquals("springproject", settings.getProject());
    Assert.assertEquals("sbcomp", settings.getName());
  }

  @Test
  public void verifyThatConfigReturnsComponentSettingsStorages() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
    LocalConfig.ComponentSettings settings = config.getComponentSettings();
    Assert.assertNotNull(settings);
    List<LocalConfig.Storage> storages = settings.getStorages();
    Assert.assertNotNull(storages);
  }

  @Test
  public void verifyThatConfigReturnsComponentSettingsStoragesProperties() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
    LocalConfig.ComponentSettings settings = config.getComponentSettings();
    Assert.assertNotNull(settings);
    List<LocalConfig.Storage> storages = settings.getStorages();
    Assert.assertNotNull(storages);
    Assert.assertEquals("storage1", storages.get(0).getName());
    Assert.assertEquals("1gi", storages.get(0).getSize());
    Assert.assertEquals("C:/Program Files/Git/storage1", storages.get(0).getPath());
  }

  @Test
  public void verifyThatConfigReturnsComponentSettingsEnvs() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
    LocalConfig.ComponentSettings settings = config.getComponentSettings();
    Assert.assertNotNull(settings);
    List<LocalConfig.Env> envs = settings.getEnvs();
    Assert.assertNotNull(envs);
  }

  @Test
  public void verifyThatConfigReturnsComponentSettingsEnvsProperties() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
    LocalConfig.ComponentSettings settings = config.getComponentSettings();
    Assert.assertNotNull(settings);
    List<LocalConfig.Env> envs = settings.getEnvs();
    Assert.assertNotNull(envs);
    Assert.assertEquals("JEFF", envs.get(0).getName());
    Assert.assertEquals("jeff", envs.get(0).getValue());
  }

  @Test
  public void verifyThatConfigReturnsComponentSettingsURLs() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
    LocalConfig.ComponentSettings settings = config.getComponentSettings();
    Assert.assertNotNull(settings);
    List<LocalConfig.URL> urls = settings.getUrls();
    Assert.assertNotNull(urls);
  }

  @Test
  public void verifyThatConfigReturnsComponentSettingsURLsProperties() throws IOException {
    LocalConfig config = LocalConfig.load(url);
    Assert.assertNotNull(config);
    LocalConfig.ComponentSettings settings = config.getComponentSettings();
    Assert.assertNotNull(settings);
    List<LocalConfig.URL> urls = settings.getUrls();
    Assert.assertNotNull(urls);
    Assert.assertEquals("url1", urls.get(0).getName());
    Assert.assertEquals("8080", urls.get(0).getPort());
  }
}
