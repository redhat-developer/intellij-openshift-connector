/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.validation;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.yaml.schema.YamlJsonSchemaHighlightingInspection;

import java.util.Collections;


public class DevfileSchemasTest extends BasePlatformTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    System.setProperty("NO_FS_ROOTS_ACCESS_CHECK", "true");
  }

  @Override
  public void tearDown() throws Exception {
    System.clearProperty("NO_FS_ROOTS_ACCESS_CHECK");
    super.tearDown();
  }

  public void testQuarkusDevfile() {
    myFixture.setTestDataPath("src/test/resources");
    myFixture.enableInspections(Collections.singletonList(YamlJsonSchemaHighlightingInspection.class));
    myFixture.configureByFile("devfiles/java-quarkus-v200.yaml");
    myFixture.checkHighlighting();
  }

  public void testPythonDevfile() {
    myFixture.setTestDataPath("src/test/resources");
    myFixture.enableInspections(Collections.singletonList(YamlJsonSchemaHighlightingInspection.class));
    myFixture.configureByFile("devfiles/sample-python-v220.yaml");
    myFixture.checkHighlighting();
  }

}
