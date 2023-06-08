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

import java.util.HashSet;
import java.util.Set;

public class ComponentFeatures {

    private final Set<ComponentFeature> features = new HashSet<>();

    public ComponentFeatures(ComponentFeature feature) {
        addFeature(feature);
    }

    public ComponentFeatures() {}

    public void addFeature(ComponentFeature feature) {
        features.add(feature);
    }

    public void removeFeature(ComponentFeature feature) {
        features.remove(feature);
    }

    public boolean is(ComponentFeature feature) {
        return features.contains(feature);
    }

    public boolean isDev() {
        return is(ComponentFeature.DEV) || is(ComponentFeature.DEV_ON_PODMAN);
    }

    public boolean isDeploy() {
        return is(ComponentFeature.DEPLOY);
    }

    public boolean isDebug() {
        return is(ComponentFeature.DEBUG);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        if (isDev()) {
            first = append(builder, true, ComponentFeature.DEV.getKind().getLabel());
        }
        if (isDebug()) {
            first = append(builder, first, ComponentFeature.DEBUG.getKind().getLabel());
        }
        if (isDeploy()) {
            append(builder, first, ComponentFeature.DEPLOY.getKind().getLabel());
        }
        return builder.toString();
    }

    private boolean append(StringBuilder builder, boolean first, String label) {
        if (!first) {
            builder.append(',');
        }
        builder.append(label);
        return false;
    }

    public boolean isOnCluster() {
        return isDev() || isDebug() || isDeploy();
    }
}
