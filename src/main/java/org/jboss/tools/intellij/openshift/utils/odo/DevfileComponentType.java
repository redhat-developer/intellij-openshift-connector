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
package org.jboss.tools.intellij.openshift.utils.odo;

import java.util.List;

public final class DevfileComponentType extends AbstractComponentType {

    private final String displayName;
    private final String description;
    private final DevfileRegistry devfileRegistry;
    private final String language;
    private final List<String> tags;

    public DevfileComponentType(String name, String displayName, String description, DevfileRegistry devfileRegistry,
                                String language, List<String> tags) {
        super(name);
        this.displayName = displayName;
        this.description = description;
        this.devfileRegistry = devfileRegistry;
        this.language = language;
        this.tags = tags;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public DevfileRegistry getDevfileRegistry() {
        return devfileRegistry;
    }

    public String getLanguage() {
        return language;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return getDisplayName() + " (from " + devfileRegistry.getName() + ")";
    }
}
