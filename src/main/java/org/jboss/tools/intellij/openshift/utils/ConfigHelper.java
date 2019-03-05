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
package org.jboss.tools.intellij.openshift.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.ConfigBuilder;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import org.jboss.tools.intellij.openshift.utils.odo.OdoConfig;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
        return System.getProperty("user.home") + "/.odo/odo-config.yaml";
    }

    public static OdoConfig loadOdoConfig() throws IOException {
        File f = new File(getOdoConfigPath());
        if (f.exists()) {
            return loadOdoConfig(f.toURI().toURL());
        } else {
            return new OdoConfig();
        }
    }

    public static OdoConfig loadOdoConfig(URL url) throws IOException {
        return mapper.readValue(url, OdoConfig.class);
    }

    public static void saveOdoConfig(OdoConfig config) throws IOException {
        mapper.writeValue(new File(getOdoConfigPath()), config);
    }

    public static ToolsConfig loadToolsConfig() throws IOException {
        return loadToolsConfig(ConfigHelper.class.getResource("/tools.json"));
    }

    public static ToolsConfig loadToolsConfig(URL url) throws IOException {
        return mapper.readValue(url, ToolsConfig.class);
    }
}
