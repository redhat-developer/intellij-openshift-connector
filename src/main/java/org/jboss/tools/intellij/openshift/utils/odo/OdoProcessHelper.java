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
package org.jboss.tools.intellij.openshift.utils.odo;

import com.intellij.execution.process.ProcessHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdoProcessHelper {

    /**
     * Map of process launched for feature (dev, debug,...) related.
     * Key is component name
     * Value is map index by the feature and value is the process handler
     */
    private final Map<String, Map<ComponentFeature, ProcessHandler>> componentFeatureProcesses = new HashMap<>();

    /**
     * Map of process launched for log activity.
     * Key is component name
     * Value is list with 2 process handler index 0 is dev; index 1 is deploy
     */
    private final Map<String, List<ProcessHandler>> componentLogProcesses = new HashMap<>();

    public Map<String, Map<ComponentFeature, ProcessHandler>> getComponentFeatureProcesses() {
        return componentFeatureProcesses;
    }

    public Map<String, List<ProcessHandler>> getComponentLogProcesses() {
        return componentLogProcesses;
    }
}
