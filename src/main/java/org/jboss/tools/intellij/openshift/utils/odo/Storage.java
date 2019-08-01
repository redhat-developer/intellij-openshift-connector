package org.jboss.tools.intellij.openshift.utils.odo;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import org.jboss.tools.intellij.openshift.KubernetesLabels;

public interface Storage {
    public String getName();
    public String getSize();
    public String getPath();

    static Storage of(String name) {
        return new Storage() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getSize() {
                return "";
            }

            @Override
            public String getPath() {
                return "";
            }
        };
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
