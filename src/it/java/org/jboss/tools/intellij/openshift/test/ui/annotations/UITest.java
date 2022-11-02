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
package org.jboss.tools.intellij.openshift.test.ui.annotations;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;

import org.junit.jupiter.api.Tag;

/**
 * @author Ondrej Dockal
 * Annotation represent UI Test class, using @Tag annotation
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Tag("ui-test")
public @interface UITest {

}
