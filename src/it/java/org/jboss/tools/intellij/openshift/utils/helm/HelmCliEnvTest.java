/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.helm;


import org.jboss.tools.intellij.openshift.utils.helm.HelmCli.HelmEnv;

import static org.fest.assertions.Assertions.assertThat;

public class HelmCliEnvTest extends HelmCliTest {

  public void testEnv_should_return_current_namespace() throws Exception {
      // given
      // when
      HelmEnv env = helm.env();
      // then
      assertThat(env.get(HelmEnv.HELM_NAMESPACE)).isNotEmpty();
  }
}
