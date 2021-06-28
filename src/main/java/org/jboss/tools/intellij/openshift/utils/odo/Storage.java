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

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import org.jboss.tools.intellij.openshift.KubernetesLabels;

public interface Storage {
    public String getName();

    static Storage of(String name) {
        return () -> name;
    }

    public static String getStorageName(PersistentVolumeClaim pvc) {
        String res = null;
        if (pvc.getMetadata().getLabels() != null) {
            res = pvc.getMetadata().getLabels().get(KubernetesLabels.STORAGE_NAME_LABEL);
        }
        if (res == null) {
            res = KubernetesLabels.getComponentName(pvc);
        }
        return res;
    }
}
