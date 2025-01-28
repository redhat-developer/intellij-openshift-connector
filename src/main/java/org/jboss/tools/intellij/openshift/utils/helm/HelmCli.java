/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.helm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.utils.Cli;
import org.jboss.tools.intellij.openshift.utils.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static org.jboss.tools.intellij.openshift.Constants.HOME_FOLDER;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.asyncSend;

public class HelmCli extends Cli implements Helm {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmCli.class);

    public HelmCli(String command) {
        super(command);
    }

    @Override
    public String addRepo(String name, String url, String flags) throws IOException {
        ActionMessage telemetry = TelemetryService.instance().getBuilder().action(
          TelemetryService.NAME_PREFIX_MISC + "helm-add repo");
        try {
            LOGGER.info("Adding repo {} at {}.", name, url);
            String result = execute(command, Collections.emptyMap(), "repo", "add", name, url, flags);
            asyncSend(telemetry.success());
            return result;
        } catch (IOException e) {
            LOGGER.info("Could not att repo {} at {}.", name, url);
            asyncSend(telemetry.error(e));
            throw e;
        }
    }

    @Override
    public void removeRepos(String... names) throws IOException {
        LOGGER.info("Removing repositories {}.", String.join(", ", names));
        List<String> notRemoved = Arrays.stream(names)
          .map(this::removeRepo)
          .filter(Objects::nonNull)
          .toList();
        if (!notRemoved.isEmpty()) {
            throw new IOException("Could not remove repositories " + String.join(", ", notRemoved));
        }
    }

    private String removeRepo(String name) {
        ActionMessage telemetry = TelemetryService.instance().getBuilder().action(
          TelemetryService.NAME_PREFIX_MISC + "helm-remove repository");
        try {
            execute(command, Collections.emptyMap(), "repo", "remove", name);
            asyncSend(telemetry.success());
            return null;
        } catch (IOException e) {
            LOGGER.info("Could not remove repository " + name, e);
            asyncSend(telemetry.error(e.getMessage()));
            return name;
        }
    }

        @Override
    public List<HelmRepository> listRepos() throws IOException {
        ActionMessage telemetry = TelemetryService.instance().getBuilder().action(
          TelemetryService.NAME_PREFIX_MISC + "helm-list repo");
        try {
            LOGGER.info("Listing repos.");
            String repos = execute(command, Collections.emptyMap(), "repo", "list", "-o=json");
            asyncSend(telemetry.success());
            return Serialization.json().readValue(repos, new TypeReference<>() {});
        } catch (IOException e) {
            asyncSend(telemetry.error(e));
            throw e;
        }
    }

    @Override
    public List<Chart> search() throws IOException {
        ActionMessage telemetry = TelemetryService.instance().getBuilder().action(
          TelemetryService.NAME_PREFIX_MISC + "helm-list charts");
        try {
            LOGGER.info("Listing all charts.");
            String charts = execute(command, Collections.emptyMap(), "search", "repo", "-l", "-o=json");
            asyncSend(telemetry.success());
            return Serialization.json().readValue(charts, new TypeReference<>() {
            });
        } catch (IOException e) {
            asyncSend(telemetry.error(e));
            throw e;
        }
    }

    @Override
    public List<Chart> search(String regex) throws IOException {
        ActionMessage telemetry = TelemetryService.instance().getBuilder().action(
          TelemetryService.NAME_PREFIX_MISC + "helm-search charts");
        try {
            LOGGER.info("Searching all charts that match {}.", regex);
            String charts = execute(command, Collections.emptyMap(), "search", "repo", "-r", regex, "-o=json");
            asyncSend(telemetry.success());
            return Serialization.json().readValue(charts, new TypeReference<>() {});
        } catch (IOException e) {
            asyncSend(telemetry.error(e));
            throw e;
        }
    }

    @Override
    public String install(String name, String chart, String version, File valuesFile) throws IOException {
        // telemetry sent in ChartsDialog
        List<String> arguments = new ArrayList<>();
        arguments.add("install");
        arguments.add(name);
        arguments.add(chart);
        arguments.add("--version");
        arguments.add(version);
        if (valuesFile != null
          && valuesFile.exists()
          && valuesFile.length() > 0) {
            arguments.add("-f");
            arguments.add(valuesFile.getAbsolutePath());
        }
        LOGGER.info("Installing chart {} in version {}.", chart, version);
        return execute(command, Collections.emptyMap(), arguments.toArray(new String[]{}));
    }

    @Override
    public List<ChartRelease> list() throws IOException {
        ActionMessage telemetry = TelemetryService.instance().getBuilder().action(
          TelemetryService.NAME_PREFIX_MISC + "helm-list releases");
        try {
            LOGGER.info("listing all releases.");
            String charts = execute(command, Collections.emptyMap(), "list", "-o=json");
            asyncSend(telemetry.success());
            return Serialization.json().readValue(charts, new TypeReference<>() {});
        } catch (IOException e) {
            asyncSend(telemetry.error(e));
            throw e;
        }
    }

    @Override
    public String uninstall(String... names) throws IOException {
        // telemetry sent in UninstallReleaseAction
        String[] arguments = new String[names.length + 1];
        arguments[0] = "uninstall";
        System.arraycopy(names, 0, arguments,  1, names.length);

        return execute(command,
          Collections.emptyMap(),
          arguments);
    }

    @Override
    public HelmEnv env() throws IOException {
        String result = execute(command, Collections.emptyMap(), "env");
        /*
         * HELM_NAMESPACE="default"
         * HELM_KUBECONTEXT=""
         */
        Map<String, String> env = Stream.of(result.split("\n"))
          .collect(Collectors.toMap(
            (String line) -> line.split("=", 2)[0],
            (String line) -> {
                String[] keyValue = line.split("=", 2);
                if (keyValue.length > 2) {
                    return "";
                } else {
                    return keyValue[1].replaceAll("\"", "");
                }
            }
          ));
        return new HelmEnv(env);
    }

    @Override
    public String showValues(String chart) throws IOException {
        return execute(command, Collections.emptyMap(), "show", "values", chart);
    }

    private static String execute(String command, Map<String, String> envs, String... args) throws IOException {
        File workingDirectory = new File(HOME_FOLDER);
        ExecHelper.ExecResult output = ExecHelper.executeWithResult(command, true, workingDirectory, envs, args);
        return output.getStdOut();
    }

    public static class HelmEnv {

        public static final String HELM_BIN = "HELM_BIN";
        public static final String HELM_BURST_LIMIT = "HELM_BURST_LIMIT";
        public static final String HELM_CACHE_HOME = "HELM_CACHE_HOME";
        public static final String HELM_CONFIG_HOME = "HELM_CONFIG_HOME";
        public static final String HELM_DATA_HOME = "HELM_DATA_HOME";
        public static final String HELM_DEBUG = "HELM_DEBUG";
        public static final String HELM_KUBEAPISERVER = "HELM_KUBEAPISERVER";
        public static final String HELM_KUBEASGROUPS = "HELM_KUBEASGROUPS";
        public static final String HELM_KUBEASUSER = "HELM_KUBEASUSER";
        public static final String HELM_KUBECAFILE = "HELM_KUBECAFILE";
        public static final String HELM_KUBECONTEXT = "HELM_KUBECONTEXT";
        public static final String HELM_KUBEINSECURE_SKIP_TLS_VERIFY = "HELM_KUBEINSECURE_SKIP_TLS_VERIFY";
        public static final String HELM_KUBETLS_SERVER_NAME = "HELM_KUBETLS_SERVER_NAME";
        public static final String HELM_KUBETOKEN = "HELM_KUBETOKEN";
        public static final String HELM_MAX_HISTORY = "HELM_MAX_HISTORY";
        public static final String HELM_NAMESPACE = "HELM_NAMESPACE";
        public static final String HELM_PLUGINS = "HELM_PLUGINS";
        public static final String HELM_QPS = "HELM_QPS";
        public static final String HELM_REGISTRY_CONFIG = "HELM_REGISTRY_CONFIG";
        public static final String HELM_REPOSITORY_CACHE = "HELM_REPOSITORY_CACHE";
        public static final String HELM_REPOSITORY_CONFIG = "HELM_REPOSITORY_CONFIG";

        private final Map<String, String> env;

        public HelmEnv(Map<String, String> env) {
            this.env = env;
        }

        public String get(final String key) {
            return env.get(key);
        }
    }

}