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
import com.intellij.openapi.util.text.StringUtil;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jboss.tools.intellij.openshift.Constants.HOME_FOLDER;

public class HelmCli implements Helm {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmCli.class);

    private final String command;

    public HelmCli(String command) {
        this.command = command;
    }

    @Override
    public String addRepo(String name, String url) throws IOException {
        LOGGER.info("Adding repo " + name + " at " + url + ".");
        return execute(command, Collections.emptyMap(), "repo", "add", name, url);
    }

    @Override
    public List<Chart> search() throws IOException {
        LOGGER.info("Listing all charts.");
        String charts = execute(command, Collections.emptyMap(), "search", "repo", "-l", "-o=json");
        return Serialization.json().readValue(charts, new TypeReference<>(){});
    }

    @Override
    public List<Chart> search(String regex) throws IOException {
        LOGGER.info("Searching all charts that match " + regex + ".");
        String charts = execute(command, Collections.emptyMap(), "search", "repo", "-r", regex, "-o=json");
        return Serialization.json().readValue(charts, new TypeReference<>(){});
    }

    @Override
    public String install(String name, String chart, String version, String additionalArguments) throws IOException {
        List<String> arguments = new ArrayList<>();
        arguments.add("install");
        arguments.add(name);
        arguments.add(chart);
        arguments.add("--version");
        arguments.add(version);
        if (!StringUtil.isEmptyOrSpaces(additionalArguments)) {
            arguments.addAll(Arrays.stream(additionalArguments.split(" ")).collect(Collectors.toList()));
        }
        LOGGER.info("Installing chart " + chart + " in version " + version + ".");
        return execute(command, Collections.emptyMap(), arguments.toArray(new String[arguments.size()]));
    }

    @Override
    public List<ChartRelease> list() throws IOException {
        LOGGER.info("listing all releases.");
        String charts = execute(command, Collections.emptyMap(), "list", "-o=json");
        return Serialization.json().readValue(charts, new TypeReference<>(){});
    }

    @Override
    public String uninstall(String name) throws IOException {
        return execute(command, Collections.emptyMap(), "uninstall", name);
    }

    private static String execute(String command, Map<String, String> envs, String... args) throws IOException {
        File workingDirectory = new File(HOME_FOLDER);
        ExecHelper.ExecResult output = ExecHelper.executeWithResult(command, true, workingDirectory, envs, args);
        return output.getStdOut();
    }
}