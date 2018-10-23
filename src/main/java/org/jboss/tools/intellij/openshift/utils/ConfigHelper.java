package org.jboss.tools.intellij.openshift.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.ConfigBuilder;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigHelper {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static String getKubeConfigPath() {
        return System.getProperty("user.home") + "/.kube/config";
    }

    public static void saveKubeConfig(Config config) throws IOException {
        mapper.writeValue(new File(getKubeConfigPath()), config);
    }

    public static Config loadKubeConfig() throws IOException {
        File f = new File(getKubeConfigPath());
        if (f.exists()) {
            return KubeConfigUtils.parseConfig(f);
        } else {
            return new ConfigBuilder().build();
        }
    }

    public static String getOdoConfigPath() {
        return System.getProperty("user.home") + "/.kube/odo";
    }

    public static OdoConfig loadOdoConfig() throws IOException {
        File f = new File(getOdoConfigPath());
        if (f.exists()) {
            return mapper.readValue(f, OdoConfig.class);
        } else {
            return new OdoConfig();
        }
    }

    public static void saveOdoConfig(OdoConfig config) throws IOException {
        mapper.writeValue(new File(getOdoConfigPath()), config);
    }
}
