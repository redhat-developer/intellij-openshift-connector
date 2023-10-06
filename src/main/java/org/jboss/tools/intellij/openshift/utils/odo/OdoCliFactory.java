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

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.DownloadHelper;

import java.util.concurrent.CompletableFuture;

public class OdoCliFactory {

    public static final String TOOLS_JSON = "/tools.json";
    private static OdoCliFactory INSTANCE;

    public static OdoCliFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OdoCliFactory();
        }
        return INSTANCE;
    }

    private CompletableFuture<Odo> future;

    private OdoCliFactory() {
    }

    public CompletableFuture<Odo> getOdo(Project project) {
        if (future == null) {
            future = DownloadHelper.getInstance()
            .downloadIfRequiredAsync("odo", OdoCliFactory.class.getResource(TOOLS_JSON))
            .thenApply(command -> {
                if (command != null) {
                    return new OdoCli(project, command);
                } else {
                    return null;
                }
            });
        }
        return future;
    }

    public void resetOdo() {
        future = null;
    }
}
