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
package org.jboss.tools.intellij.openshift.utils.odo;

import java.util.Collections;
import java.util.List;

public class DebugComponentFeature extends ComponentFeature {

    public DebugComponentFeature(ComponentFeature parent) {
        super(Mode.DEBUG_MODE, "debug", ComponentFeature.Constants.WATCHING_FOR_CHANGES_IN_THE_CURRENT_DIRECTORY, parent.getStartArgs());
    }

    @Override
    public List<String> getExtraArgs() {
        return Collections.singletonList("--debug");
    }

}
