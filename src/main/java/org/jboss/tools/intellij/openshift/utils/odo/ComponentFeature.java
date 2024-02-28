/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ComponentFeature {
    public static final ComponentFeature DEV = new ComponentFeature(Mode.DEV_MODE, "dev on Cluster", Constants.WATCHING_FOR_CHANGES_IN_THE_CURRENT_DIRECTORY, Collections.singletonList("dev"));

    public static final ComponentFeature DEV_ON_PODMAN = new ComponentFeature(Mode.DEV_MODE, "dev on Podman", Constants.WATCHING_FOR_CHANGES_IN_THE_CURRENT_DIRECTORY, Collections.singletonList("dev")) {
        @Override
        public PLATFORM getPlatform() {
            return PLATFORM.PODMAN;
        }

        @Override
        public List<String> getExtraArgs() {
            return Collections.singletonList("--forward-localhost");
        }
    };

    public static final ComponentFeature DEPLOY = new ComponentFeature(Mode.DEPLOY_MODE, "deploy on Cluster", Constants.YOUR_DEVFILE_HAS_BEEN_SUCCESSFULLY_DEPLOYED, Collections.singletonList("deploy"), Arrays.asList("delete", "component", "-f"));

    private final Mode mode;
    private final String label;

    private final String output;

    private final List<String> startArgs;

    private final List<String> stopArgs;

    ComponentFeature(Mode mode, String label, String output, List<String> startArgs, List<String> stopArgs) {
        this.mode = mode;
        this.label = label;
        this.output = output;
        this.startArgs = startArgs;
        this.stopArgs = stopArgs;
    }

    ComponentFeature(Mode mode, String label, String output, List<String> startArgs) {
        this(mode, label, output, startArgs, Collections.emptyList());
    }

    public Mode getMode() {
        return mode;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getExtraArgs() {
        return Collections.emptyList();
    }

    public List<String> getStartArgs() {
        List<String> result = new ArrayList<>(startArgs);
        result.addAll(getExtraArgs());
        if (!getPlatform().equals(PLATFORM.NONE)) {
            List<String> platformStartArgs = Arrays.asList("--platform", getPlatform().getLabel());
            result.addAll(platformStartArgs);
        }
        return result;
    }

    public List<String> getStopArgs() {
        return stopArgs;
    }

    public CharSequence getOutput() {
        return output;
    }

    public PLATFORM getPlatform() {
        return PLATFORM.NONE;
    }

    public static class Constants {

        private Constants() {
            //hide constructor.
        }

        public static final String WATCHING_FOR_CHANGES_IN_THE_CURRENT_DIRECTORY = "Watching for changes in the current directory";
        public static final String YOUR_DEVFILE_HAS_BEEN_SUCCESSFULLY_DEPLOYED = "Your Devfile has been successfully deployed";
        public static final String PODMAN = "podman";

        public static final String CLUSTER = "cluster";

    }

    public enum Mode {
        DEV_MODE("dev"),
        DEBUG_MODE("debug"),
        DEPLOY_MODE("deploy");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum PLATFORM {
        PODMAN(Constants.PODMAN),

        NONE("");

        private final String label;

        PLATFORM(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

}
