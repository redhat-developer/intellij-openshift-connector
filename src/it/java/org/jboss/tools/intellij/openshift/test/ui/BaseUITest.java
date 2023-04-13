/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui;

import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

/**
 * @author Ihor Okhrimenko, Ondrej Dockal
 * Base UI test class verifying presence of tested extensions and OpenShift View presence and content
 */
public class BaseUITest extends AbstractBaseTest {

	@Test
	public void openshiftExtensionTest() {
		waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'OpenShift' stripe button is not available.", () -> isStripeButtonAvailable("OpenShift"));
		waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'Kubernetes' stripe button is not available.", () -> isStripeButtonAvailable("Kubernetes"));
	}

	@Test
	public void openshiftViewTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
		view.openView();
		view.waitForTreeItem("https://kubernetes.default.svc/", 10, 1);
		view.waitForTreeItem("Devfile registries", 10, 1);

	}
}